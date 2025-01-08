package io.github.mangocrisp.spring.taybct.tool.core.bean;

import io.github.mangocrisp.spring.taybct.tool.core.annotation.TableFieldDefault;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.TableLogicUnique;
import io.github.mangocrisp.spring.taybct.tool.core.handle.TableFieldDefaultUKHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 带唯一索引的逻辑删除<br>
 * 这个实体类的作用，要和数据库结合起来发挥。如果我们设计的表是逻辑删除的表，但是表里面有一些的唯一索引，对应的字段是唯一的
 * 但是删除是逻辑删除，这样，如果删除后，再建一个同名字段的就会报唯一约束的错了，所以这个字段是为了在逻辑的删除的时候把一个
 * 可以确定唯一的值放入，比如这条数据的主键，然后所有这个表折唯一索引，不管是有几个字段，都把这个字段加上，这样的话，就算是
 * 逻辑删除也能确保他是唯一的，删除后是唯一的，再加个同名的也不会有影响了
 *
 * @author xijieyin <br> 2022/9/22 9:03
 * @since 1.0.4
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableLogicUnique
public class UniqueDeleteLogic<P extends Serializable, U extends Serializable> extends DeleteLogicEntity<P, U> {

    @Serial
    private static final long serialVersionUID = -1733913054435978014L;
    /**
     * 唯一键
     */
    @Schema(description = "唯一键")
    @TableFieldDefault(handler = TableFieldDefaultUKHandler.class)
    private P uniqueKey;


}
