package io.github.taybct.tool.core.bean;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.taybct.tool.core.annotation.TableFieldDefault;
import io.github.taybct.tool.core.bean.service.IBaseService;
import io.github.taybct.tool.core.constant.DateConstants;
import io.github.taybct.tool.core.handle.TableFieldDefaultLoginUserIdHandler;
import io.github.taybct.tool.core.handle.TableFieldDefaultPKHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础实体类
 *
 * @author xijieyin <br> 2022/8/5 16:57
 * @since 1.0.0
 */
@Data
public class BaseEntity<P extends Serializable, U extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = 249313760315994482L;
    /**
     * 主键id
     */
    @TableId
    @Schema(description = "主键")
    @JsonSerialize(using = ToStringSerializer.class)
    @TableFieldDefault(handler = TableFieldDefaultPKHandler.class)
    private P id;

    /**
     * 创建人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "创建人")
    @TableFieldDefault(handler = TableFieldDefaultLoginUserIdHandler.class)
    private U createUser;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @JsonFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @TableFieldDefault(isTimeNow = true)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "更新人")
    @TableFieldDefault(handler = TableFieldDefaultLoginUserIdHandler.class, fill = {SqlCommandType.INSERT, SqlCommandType.UPDATE})
    private U updateUser;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @JsonFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @Schema(description = "更新时间")
    @TableFieldDefault(isTimeNow = true, fill = {SqlCommandType.INSERT, SqlCommandType.UPDATE})
    private LocalDateTime updateTime;

    /**
     * 扩展字段 <br>
     * 这里考虑到，如果业务要求要加字段，可以在这个 expansion 里面添加进去，然后再做一些其他的操作，这个不属于任何的实体 <br>
     * 这一般会用在需要添加字段的表的操作里面，你可以在 IBaseService 看一下这几个接口，你可以实现这几个接口来实现扩展字段的一些操作
     *
     * @see IBaseService#queryExpansion(List)
     * @see IBaseService#saveExpansion(Collection)
     * @see IBaseService#removeExpansion(Collection)
     * @since 1.0.4
     */
    @Schema(description = "扩展字段")
    @TableField(exist = false)
    private Map<String, Object> expansion = new LinkedHashMap<>();

}
