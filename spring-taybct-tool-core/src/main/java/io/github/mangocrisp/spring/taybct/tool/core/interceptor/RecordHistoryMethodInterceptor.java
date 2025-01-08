package io.github.mangocrisp.spring.taybct.tool.core.interceptor;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.RecordHistory;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ILoginUser;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ISecurityUtil;
import io.github.mangocrisp.spring.taybct.tool.core.domain.HistoryEntity;
import io.github.mangocrisp.spring.taybct.tool.core.enums.DataOperateType;
import io.github.mangocrisp.spring.taybct.tool.core.result.R;
import io.github.mangocrisp.spring.taybct.tool.core.service.IHistoryService;
import io.github.mangocrisp.spring.taybct.tool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据库操作历史记录拦截器
 *
 * @author xijieyin <br> 2023/4/6 下午4:31
 */
@RequiredArgsConstructor
public class RecordHistoryMethodInterceptor implements MethodInterceptor {
    final IHistoryService historyService;

    final ISecurityUtil securityUtil;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (ObjectUtil.isEmpty(invocation.getArguments())
                || invocation.getMethod().getReturnType().equals(Void.class)) {
            // 如果没有参数，或者是返回类型是空，那就没必要做记录了
            return invocation.proceed();
        }

        // 获取到所有的参数
        Object[] args = invocation.getArguments();
        Method method = invocation.getMethod();
        // 获取到注解
        RecordHistory recordHistory = method.getAnnotation(RecordHistory.class);
        //获取参数名对应数组下标
        int index = ArrayUtil.indexOf(new DefaultParameterNameDiscoverer().getParameterNames(method)
                , recordHistory.value());
        if (index < 0) {
            index = 0;
        }
        // 数据源
        String dataSource = recordHistory.dataSource();
        // 在操作之前先把数据读出来，考虑到，有修改也有删除操作，如果是删除之后再去保存，会读不到数据，因为数据已经被删除了，得事先把数据读出来
        List<HistoryEntity> recordList = new ArrayList<>();
        // 登录用户信息
        ILoginUser loginUser = securityUtil.getLoginUser();
        // 操作数据类型
        int dataOperateType = recordHistory.operateType();
        final Class<?> finalClazz = recordHistory.clazz();
        // 要操作的对象实体类
        Class<?> clazz = recordHistory.clazz();
        // 表名，这里可以直接从类上面获取 Annotation
        String tableName = Optional.ofNullable(clazz.getAnnotation(TableName.class))
                .map(TableName::value)
                .orElse(recordHistory.tableName());
        // 主键名
        String primaryKey = recordHistory.primaryKey();
        int pkTypes = recordHistory.pkTypes();
        // 这里要注意一下，有可能这个实体类是有多层父级的，所以要往上面找一直找到主键为止
        for (boolean foundId = false; !foundId && clazz != Object.class; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(TableId.class)) {
                    primaryKey = field.getName();
                    foundId = true;
                    break;
                }
            }
        }
        List<Object> argList = new ArrayList<>();
        // 获取到操作对象
        Object arg = args[index];
        if (arg instanceof Collection) {
            // 如果是批量操作
            argList.addAll((Collection<?>) arg);
        } else if (arg instanceof String) {
            // 如果是字符串形式的 id 按 逗号隔开
            argList.addAll(Arrays.asList(((String) arg).split(",")));
        } else {
            // 不管是不是批量操作，都按批量的来
            argList.add(arg);
        }
        String finalPrimaryKey = primaryKey;
        argList.forEach(a -> {
            // 主键值
            String primaryKeyValue = null;
            // 这里就是可能是更新/批量更新传的是对象
            if (finalClazz.isAssignableFrom(a.getClass())) {
                try {
                    Field field = null;
                    Class<?> aClass = a.getClass();
                    do {
                        try {
                            field = aClass.getDeclaredField(finalPrimaryKey);
                            break;
                        } catch (NoSuchFieldException e) {
                            aClass = aClass.getSuperclass();
                        }
                    } while (!aClass.equals(Object.class));
                    if (field == null) {
                        throw new NoSuchFieldException("没有找到对应的主键：[" + finalPrimaryKey + "]");
                    }
                    field.setAccessible(true);
                    primaryKeyValue = field.get(a).toString();
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            // 这里就是可能是删除操作的时候只传一个主键/主键集合过来
            else if (a instanceof Serializable) {
                primaryKeyValue = a.toString();
            }
            if (ObjectUtil.isNotEmpty(primaryKeyValue)) {
                recordList.add(record(dataSource
                        , dataOperateType
                        , loginUser
                        , tableName
                        , finalPrimaryKey
                        , primaryKeyValue
                        , pkTypes));
            }
        });
        Object proceed = invocation.proceed();
        // 如果是直接返回是否成功
        if ((proceed instanceof Boolean && ((Boolean) proceed))
                // 如果是返回的处理了几条数据（数据库操作）
                || (proceed instanceof Integer && ((Integer) proceed) > 0)
                // 如果是返回了 R，也就是放在 Controller 方法上的这里有返回是否操作成功
                || (proceed instanceof R && ((R) proceed).isOk())) {
            recordList.forEach(historyEntity ->
                    historyService.recordingHistory(dataSource
                            , recordHistory.historyTableName()
                            , historyEntity));
        }
        return proceed;
    }

    /**
     * 开始记录历史记录
     *
     * @param dataSource      数据源
     * @param dataOperateType 操作类型
     * @param loginUser       登录用户
     * @param tableName       表名
     * @param primaryKey      主键名
     * @param primaryKeyValue 主键值
     */
    private HistoryEntity record(String dataSource
            , int dataOperateType
            , ILoginUser loginUser
            , String tableName
            , String primaryKey
            , String primaryKeyValue
            , int pkTypes) {
        // 先从数据库把记录查询回来，
        Map<String, Object> recordByPrimaryKey = historyService.getRecordByPrimaryKey(dataSource, tableName, primaryKey, primaryKeyValue, pkTypes);
        // 然后转换成 json
        String jsonData = JSONObject.toJSONString(recordByPrimaryKey, JSONWriter.Feature.WriteMapNullValue);
        HistoryEntity historyEntity = new HistoryEntity();
        historyEntity.setId(IdWorker.getId());
        historyEntity.setCreateUser(loginUser.getUsername());
        historyEntity.setCreateTime(LocalDateTime.now());
        historyEntity.setTableName(tableName);
        historyEntity.setPrimaryKey(primaryKey);
        historyEntity.setPrimaryValue(primaryKeyValue);
        historyEntity.setJsonData(jsonData);
        historyEntity.setOperateType(dataOperateType);
        return historyEntity;
    }

}
