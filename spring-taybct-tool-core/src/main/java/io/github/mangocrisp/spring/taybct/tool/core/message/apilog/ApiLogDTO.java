package io.github.mangocrisp.spring.taybct.tool.core.message.apilog;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.mangocrisp.spring.taybct.tool.core.bean.BaseEntity;
import io.github.mangocrisp.spring.taybct.tool.core.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 接口日志数据传输<br>
 *
 * @author xijieyin <br> 2022/8/5 9:47
 * @since 2.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogDTO extends BaseEntity<Long, Long> implements Message {

    @Serial
    private static final long serialVersionUID = -1429609118210258177L;
    /**
     * 模块标题
     */
    private String title;
    /**
     * 接口描述
     */
    private String description;
    /**
     * 操作人员
     */
    private String username;
    /**
     * 客户端类型
     */
    private String client;
    /**
     * 主机地址
     */
    private String module;
    /**
     * 主机地址
     */
    private String ip;
    /**
     * 业务类型
     */
    private String type;
    /**
     * 请求方式
     */
    private String method;
    /**
     * 请求URL
     */
    private String url;
    /**
     * 请求参数
     */
    private String params;
    /**
     * 返回参数
     */
    private String result;
    /**
     * 状态码
     */
    private String code;
    /**
     * 租户 id
     */
    private String tenantId;

    @Override
    @JsonIgnore
    public String getPayload() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteMapNullValue);
    }
}
