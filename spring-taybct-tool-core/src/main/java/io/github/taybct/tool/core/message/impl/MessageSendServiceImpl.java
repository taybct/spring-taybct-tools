package io.github.taybct.tool.core.message.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ZipUtil;
import io.github.taybct.tool.core.constant.DateConstants;
import io.github.taybct.tool.core.message.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Api 日志发送服务
 *
 * @author xijieyin <br> 2022/8/5 20:22
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class MessageSendServiceImpl implements IMessageSendService, ApplicationRunner {
    /**
     * 配置信息
     */
    final MessageProperties messageProperties;
    /**
     * 所有的处理器
     */
    @Getter
    final ConcurrentHashMap<MessageType, IMessageSendHandler> messageSendHandlerList;

    @Override
    public void addHandler(IMessageSendHandler handler) {
        messageSendHandlerList.put(handler.getMessageType(), handler);
    }

    /**
     * 定义发送锁对象
     */
    private final ReentrantLock sendLock = new ReentrantLock();
    /**
     * 定义清理锁对象
     */
    private final ReentrantLock cleanLock = new ReentrantLock();

    ExecutorService sendThreadExecutor = Executors.newSingleThreadExecutor();

    ExecutorService cleanThreadExecutor = Executors.newSingleThreadExecutor();

    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    public void send(Message message) {
        if (!messageProperties.getEnable()) {
            return;
        }
        // 循环所有的处理器
        messageSendHandlerList.keySet().stream()
                .filter(type -> type.supports(message.getClass()))
                .forEach(type -> {
                    // 这里再开个线程，可能会发的时间比较长
                    cachedThreadPool.execute(() -> {
                        // 防止在写文件时候写一半被删除了
                        sendLock.lock();
                        try {
                            String folder = messageProperties.getFolder();
                            File tempLogFolder = new File(folder);
                            if (!tempLogFolder.exists()) {
                                tempLogFolder.mkdirs();
                            }
                            // 生成一个唯一的 id 用来存成文件到本地
                            File logFile = new File(String.format("%s/%s%s%s"
                                    , folder
                                    , type.prefix()
                                    , UUID.fastUUID()
                                    , type.suffix()));
                            // 消息
                            FileUtil.writeUtf8String(message.getPayload(), logFile);
                            log.debug("生成本地日志文件{}", logFile.getPath());
                            File[] files = tempLogFolder.listFiles();
                            if (files != null && files.length > messageProperties.getBuffer()) {
                                // 如果超出了 buff 数量的文件，这时就自动触发一次发消息
                                sendLock.unlock();
                                send();
                            }
                        } finally {
                            sendLock.unlock();
                        }
                    });
                });
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!messageProperties.getEnable()) {
            return;
        }
        sendThreadExecutor.execute(() -> {
            log.debug("日志检查启动");
            try {
                do {
                    Thread.sleep(messageProperties.getCheckDelay());
                    send();
                } while (true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        cleanThreadExecutor.execute(() -> {
            log.debug("日志清理启动");
            try {
                do {
                    // 启动就检查
                    clean();
                    // 然后睡眠一天，第二天的时候再清理
                    Thread.sleep(1000L * 60 * 60 * 24);
                } while (true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * <pre>
     * 发送消息
     * </pre>
     *
     * @author xijieyin
     * @since 2024/9/1 22:42
     */
    private synchronized void send() {
        // 加锁
        sendLock.lock();
        try {
            String folder = messageProperties.getFolder();
            File tempLogFolder = new File(folder);
            if (tempLogFolder.exists()) {
                messageSendHandlerList.forEach((messageType, handler) -> {
                    File[] files = tempLogFolder.listFiles(file -> file.getName().startsWith(messageType.prefix())
                            && file.getName().endsWith(messageType.suffix()));
                    // 将会报错的文件路径
                    AtomicReference<String> errorPath = new AtomicReference<>(folder);
                    try {
                        Optional.ofNullable(files).ifPresent(fs -> {
                            for (File file : fs) {
                                // 读取到日志内容
                                String path = file.getPath();
                                log.debug("检查到日志文件：{}", path);
                                errorPath.set(path);
                                if (handler.send(FileUtil.readUtf8String(file)) && file.exists()) {
                                    file.delete();
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("发送日志失败！", e);
                        log.error(String.format("发送日志失败！请手动查看原因！[%s]", errorPath.get()));
                    }
                });
            }
        } finally {
            sendLock.unlock();
        }
    }

    /**
     * 清理文件
     */
    private synchronized void clean() {
        // 加锁
        cleanLock.lock();
        try {
            String folder = messageProperties.getFolder();
            File tempLogFolder = new File(folder);
            if (tempLogFolder.exists()) {
                // 多少天前的时间限制
                LocalDate historyCondition = LocalDateTime.now().minusDays(messageProperties.getMaxHistory()).toLocalDate();
                // 最后检查文件超过存储时间限制没清理的文件
                // 如果创建时间比最大历史存储限制要前，就把这个文件删除
                File[] messageFiles = tempLogFolder.listFiles(file ->
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), DateConstants.Zone.CHINA.toZoneId())
                                .toLocalDate()
                                .isBefore(historyCondition)
                                && !file.getName().endsWith(".zip"));
                if (ArrayUtil.isNotEmpty(messageFiles)) {
                    // 把超时的文件压缩起来
                    ZipUtil.zip(FileUtil.file(String.format("%s/%s.zip"
                                    , tempLogFolder.getPath()
                                    , historyCondition.format(DateTimeFormatter.ofPattern(DateConstants.format.YYYYMMDD, Locale.CHINA))))
                            , false, messageFiles);
                }
                File[] zipFiles = tempLogFolder.listFiles(file ->
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), DateConstants.Zone.CHINA.toZoneId())
                                .toLocalDate()
                                .isBefore(historyCondition));
                if (zipFiles != null) {
                    // 把超时的文件删除
                    for (File file : zipFiles) {
                        file.delete();
                    }
                }
            }
        } finally {
            cleanLock.unlock();
        }
    }

}
