package io.github.mangocrisp.spring.taybct.tool.core.websocket.support;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.mangocrisp.spring.taybct.tool.core.result.IResultCode;
import io.github.mangocrisp.spring.taybct.tool.core.result.ResultCode;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.constant.MessageTopic;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.enums.MessageUserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * <pre>
 * websocket消息
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/3/14 16:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@Schema(description = "websocket消息体")
@Builder
public class WSR<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -8550725582200274152L;
    /**
     * 消息 id
     */
    @Schema(description = "消息 id")
    private String messageId = IdUtil.randomUUID();
    /**
     * 发送人
     */
    @Schema(description = "发送人")
    private MessageUser fromUser;
    /**
     * 发送人名称
     */
    @Schema(description = "发送人名称")
    private String name;
    /**
     * 发送人头像
     */
    @Schema(description = "发送人头像")
    private String avatar;
    /**
     * 接收人，可以是多人，如果不传则发送给所有人
     */
    @Schema(description = "接收人")
    private LinkedHashSet<MessageUser> toUser;
    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime = LocalDateTime.now();
    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private String code = ResultCode.OK.getCode();
    /**
     * 消息
     */
    @Schema(description = "消息")
    private String message = ResultCode.OK.getMessage();
    /**
     * 消息标题
     */
    @Schema(description = "消息标题")
    private String title = "消息通知";
    /**
     * 消息主题
     */
    @Schema(description = "消息主题")
    private String topic = MessageTopic.SIMPLE_MESSAGE;
    /**
     * 消息类型（子类型）
     */
    @Schema(description = "消息类型（子类型）")
    private String subType;
    /**
     * 返回的数据
     */
    @Schema(description = "返回的数据")
    private T data;
    /**
     * 二进制数据，如果指定了这个值，就只发送二进制数据，不发送消息内容
     */
    @Schema(description = "二进制数据")
    @JsonIgnore
    private byte[] bytes;
    /**
     * 额外的数据
     */
    @Schema(description = "额外数据")
    private JSONObject meta = new JSONObject();

    /**
     * 错误信息，仅在调试阶段使用
     */
    @Schema(description = "错误信息")
    private Throwable error;

    public WSR(String code, String message, T data, JSONObject meta) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.meta = meta;
    }

    public WSR<T> setFromUserId(Long fromUserId) {
        return setFromUser(new MessageUser(MessageUserType.USER, fromUserId, null));
    }

    public WSR<T> setFromUser(MessageUser fromUser) {
        this.fromUser = fromUser;
        return this;
    }

    public WSR<T> setToUserId(Long... toUserId) {
        return setToUser(new LinkedHashSet<>(Arrays.stream(toUserId).map(id -> new MessageUser(MessageUserType.USER, id, null)).toList()));
    }

    public WSR<T> setToMessageUser(MessageUser... toUser) {
        return setToUser(new LinkedHashSet<>(Arrays.asList(toUser)));
    }

    public WSR<T> setToUser(LinkedHashSet<MessageUser> toUser) {
        this.toUser = toUser;
        return this;
    }

    public static <S> WSR<S> ok() {
        return result(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), null);
    }

    public static <S> WSR<S> ok(String message) {
        return result(ResultCode.OK.getCode(), message, null);
    }

    public static <S> WSR<S> ok(String message, S data) {
        return result(ResultCode.OK.getCode(), message, data);
    }

    public static <S> WSR<S> fail() {
        return result(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage(), null);
    }

    public static <S> WSR<S> fail(IResultCode resultCode) {
        return result(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <S> WSR<S> fail(String code, String message) {
        return result(code, message, null);
    }

    public static <S> WSR<S> fail(String message) {
        return result(ResultCode.ERROR.getCode(), message, null);
    }

    public static <S> WSR<S> status(String code, String message) {
        return result(code, message, null);
    }

    public static <S, U extends S> WSR<S> status(String code, String message, U data) {
        return result(code, message, data);
    }

    public static <S, U extends S> WSR<S> data(U data) {
        return result(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), data);
    }

    private static <S, U extends S> WSR<S> result(String code, String message, U data) {
        return new WSR<>(code, message, data, new JSONObject());
    }

    public WSR<T> put(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }

    /**
     * 是否成功
     *
     * @return Boolean
     * @author xijieyin <br> 2022/8/5 18:46
     * @since 1.0.0
     */
    @Schema(description = "是否成功")
    public Boolean isOk() {
        return this.code.equalsIgnoreCase(ResultCode.OK.getCode());
    }

    /**
     * 是否有返回数据
     *
     * @return Boolean
     * @author xijieyin <br> 2022/8/5 18:46
     * @since 1.0.0
     */
    @Schema(description = "是否有返回数据")
    public Boolean hasData() {
        return Optional.ofNullable(this.data).isPresent();
    }

}
