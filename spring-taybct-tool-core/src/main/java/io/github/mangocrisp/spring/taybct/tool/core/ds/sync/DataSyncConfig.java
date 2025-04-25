package io.github.mangocrisp.spring.taybct.tool.core.ds.sync;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 *
 * <pre>
 * 数据同步配置
 * </pre>
 *
 * @author xijieyin
 * @since 2025/4/14 16:26
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "数据同步配置")
@Builder
public class DataSyncConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 源表
     */
    @Schema(description = "源表")
    private String sourceTable;

    /**
     * 目标表
     */
    @Schema(description = "目标表")
    private String targetTable;

    /**
     * 同步用的sql
     */
    @Schema(description = "同步用的sql")
    private String sqlSelect;

    /**
     * 查询上一次同步时间的sql
     */
    @Schema(description = "查询上一次同步时间的sql")
    private String sqlLastSyncTime;

    /**
     * 查询上一次同步时间的字段名
     */
    @Schema(description = "查询上一次同步时间的字段名")
    private String fieldLastSyncTime;

    /**
     * 唯一键（可以是多个组合的唯一键），用来合并数据，如果没有重复的就插入，如果有重复的就更新
     */
    @Schema(description = "唯一键（可以是多个组合的唯一键），用来合并数据，如果没有重复的就插入，如果有重复的就更新")
    private String fieldUniqueKye;

    /**
     * 源数据库驱动
     */
    @Schema(description = "源数据库驱动")
    private String sourceDriver;

    /**
     * 源数据库连接
     */
    @Schema(description = "源数据库连接")
    private String sourceUrl;

    /**
     * 源数据库连接 schema
     */
    @Schema(description = "源数据库连接 schema")
    private String sourceSchema;

    /**
     * 源数据库用户
     */
    @Schema(description = "源数据库用户")
    private String sourceUser;

    /**
     * 源数据库密码
     */
    @Schema(description = "源数据库密码")
    private String sourcePass;

    /**
     * 目标数据库驱动
     */
    @Schema(description = "目标数据库驱动")
    private String targetDriver;

    /**
     * 目标数据库连接
     */
    @Schema(description = "目标数据库连接")
    private String targetUrl;

    /**
     * 目标数据库连接 schema
     */
    @Schema(description = "目标数据库连接 schema")
    private String targetSchema;

    /**
     * 目标数据库用户
     */
    @Schema(description = "目标数据库用户")
    private String targetUser;

    /**
     * 目标数据库密码
     */
    @Schema(description = "目标数据库密码")
    private String targetPass;

    /**
     * 其他配置参数
     */
    @Schema(description = "其他配置参数")
    private JSONObject properties = new JSONObject();

}
