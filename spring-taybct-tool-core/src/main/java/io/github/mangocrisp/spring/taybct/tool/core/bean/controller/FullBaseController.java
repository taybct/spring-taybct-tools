package io.github.mangocrisp.spring.taybct.tool.core.bean.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.ApiLog;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.WebLog;
import io.github.mangocrisp.spring.taybct.tool.core.aop.WebLogAspect;
import io.github.mangocrisp.spring.taybct.tool.core.bean.BaseEntity;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ITypeConvert;
import io.github.mangocrisp.spring.taybct.tool.core.bean.service.IBaseService;
import io.github.mangocrisp.spring.taybct.tool.core.constant.OperateType;
import io.github.mangocrisp.spring.taybct.tool.core.constant.PageRequestConstants;
import io.github.mangocrisp.spring.taybct.tool.core.request.SqlQueryParams;
import io.github.mangocrisp.spring.taybct.tool.core.result.R;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.MyBatisUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;
import io.swagger.v3.oas.annotations.Operation;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 请求基础类<br>
 * 基础 Controller 按照 RESTful 来实现的:
 * <p>
 * GET         /prefix/list：列出所有资源<br>
 * GET         /prefix/page：分页获取资源<br>
 * POST        /prefix：新建一个资源<br>
 * GET         /prefix/{ID}：获取某个指定资源的信息<br>
 * PUT         /prefix/{ID}：更新某个指定资源的信息（提供该资源的全部信息）<br>
 * PATCH       /prefix/{ID}：更新某个指定资源的信息（提供该资源的部分信息）<br>
 * DELETE      /prefix/{ID}：删除某个资源<br>
 * GET         /prefix/{ID}/children：列出某个指定资源的所有子资源<br>
 * DELETE      /prefix/{ID}/children/{animalsID}：删除某个指定资源的指定子资源
 *
 * <br>
 * 泛型类型
 * <br>
 * T:实体类
 * <br>
 * S:Service
 * <br>
 * P:主键类型
 * <br>
 * Q:查询请求参数类型
 * <br>
 * A:新增对象类型
 * <br>
 * U:修改对象类型
 * <br>
 *
 * @author xijieyin <br> 2022/8/4 18:29
 * @see PageRequestConstants
 * @see WebLogAspect
 * @see R
 * @see IPage
 * @see com.baomidou.mybatisplus.extension.plugins.pagination.Page
 * @see MyBatisUtil
 * @since 1.0.0
 */
public interface FullBaseController<T extends BaseEntity
        , S extends IBaseService<T>
        , P extends Serializable
        , Q extends T
        , A extends T
        , U extends T> extends ITypeConvert {
    /**
     * 基础 service
     *
     * @return service
     */
    default S getBaseService() {
        Class<Object> interfaceT = BeanUtil.getInterfaceT(this, FullBaseController.class, 1);
        return (S) SpringUtil.getBean(interfaceT);
    }

    /**
     * 获取资源名，主要用于记录日志用，如果不写也没关系
     *
     * @author xijieyin <br> 2022/8/4 18:32
     * @since 1.0.0
     */
    default String getResource() {
        return "";
    }

    /**
     * 获取列表信息
     *
     * @param params 所有参数
     * @return {@code R<List < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:34
     * @since 1.0.0
     */
    @Operation(summary = "获取列表")
    @GetMapping("list")
    @WebLog
    default R<List<? extends T>> list(@RequestParam(required = false) Map<String, Object> params) {
        return R.data(getBaseService().customizeList(params));
    }

    /**
     * 获取列表信息
     *
     * @param dto            {@literal 请求参数}
     * @param sqlQueryParams SQL 查询参数
     * @return {@code R<List < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:34
     * @since 1.0.0
     */
    @Operation(summary = "获取列表")
    @PostMapping("list")
    @WebLog
    default R<List<? extends T>> list(@RequestBody Q dto, SqlQueryParams sqlQueryParams) {
        return R.data(getBaseService().customizeList(dto, sqlQueryParams));
    }

    /**
     * 获取分页
     *
     * @param params 所有参数
     * @return {@code R<IPage < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:36
     * @since 1.0.0
     */
    @Operation(summary = "获取分页")
    @GetMapping("page")
    @WebLog
    default R<IPage<? extends T>> page(@RequestParam(required = false) Map<String, Object> params) {
        return R.data(getBaseService().customizePage(params));
    }

    /**
     * 获取分页
     *
     * @param dto            {@literal 请求参数}
     * @param sqlQueryParams {@literal 分页参数}
     * @return {@code R<IPage < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:36
     * @since 1.0.0
     */
    @Operation(summary = "获取分页")
    @PostMapping("page")
    @WebLog
    default R<IPage<? extends T>> page(@RequestBody Q dto, SqlQueryParams sqlQueryParams) {
        return R.data(getBaseService().customizePage(dto, sqlQueryParams));
    }

    /**
     * 新增对象
     *
     * @param domain 请求实体类，{key:value}
     * @return {@code R<? extends T>}
     * @author xijieyin <br> 2022/8/4 18:36
     * @since 1.0.0
     */
    @Operation(summary = "新增对象")
    @PostMapping
    @WebLog
    @ApiLog(title = "新增对象", description = "新增一条记录，并且在新增成功后返回这个新增的对象，这个对象会带着生成的 id 一起返回", type = OperateType.INSERT)
    default R<? extends T> add(@Valid @NotNull @RequestBody A domain) {
        return getBaseService().customizeSave(domain) ? R.data(domain) : R.fail(String.format("新增%s失败！", getResource()));
    }

    /**
     * 批量保存对象
     *
     * @param domains 请求实体类，[{key:value},]
     * @return {@code R<Collection < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:37
     * @since 1.0.0
     */
    @Operation(summary = "批量保存对象")
    @PostMapping("batch")
    @WebLog
    @ApiLog(title = "批量保存对象", description = "批量保存对象，并且在新增成功后一起返回", type = OperateType.INSERT, isSaveRequestData = false, isSaveResultData = false)
    default R<Collection<? extends T>> saveBatch(@Valid @NotNull @RequestBody Collection<A> domains) {
        return getBaseService().customizeSaveBatch(domains) ? R.data(domains) : R.fail(String.format("批量保存%s失败！", getResource()));
    }

    /**
     * 根据主键 id 查看详情
     *
     * @param id 主键 id
     * @return {@code R<? extends T>}
     * @author xijieyin <br> 2022/8/4 18:38
     * @since 1.0.0
     */
    @Operation(summary = "根据 id 查看详情")
    @GetMapping("{id}")
    @WebLog
    default R<? extends T> detail(@NotNull @PathVariable P id) {
        return R.data(getBaseService().customizeGetById(id));
    }

    /**
     * 根据 id 更新全部字段
     *
     * @param domain 请求实体，{key:value}，实体需要有主键 id
     * @return {@code R<? extends T>}
     * @author xijieyin <br> 2022/8/4 18:39
     * @since 1.0.0
     */
    @Operation(summary = "根据 id 更新全部字段")
    @PutMapping
    @WebLog
    @ApiLog(title = "根据 id 更新全部字段", description = "根据 id 更新全部字段", type = OperateType.UPDATE)
    default R<? extends T> updateAllField(@Valid @NotNull @RequestBody U domain) {
        return getBaseService().customizeUpdateById(domain) ? R.data(domain) : R.fail(String.format("更新%s失败！", getResource()));
    }

    /**
     * 根据 id 批量更新全部字段
     *
     * @param domains 请求实体类集合，[{key:value},]，这个集合里面的所有的实体类都需要有主键 id
     * @return {@code R<Collection < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:39
     * @since 1.0.0
     */
    @Operation(summary = "根据 id 批量更新全部字段")
    @PutMapping("batch")
    @WebLog
    @ApiLog(title = "根据 id 批量更新全部字段", description = "根据 id 批量更新全部字段", type = OperateType.UPDATE, isSaveRequestData = false, isSaveResultData = false)
    default R<Collection<? extends T>> updateAllFieldBatch(@Valid @NotNull @RequestBody Collection<U> domains) {
        return getBaseService().customizeUpdateBatchById(domains) ? R.data(domains) : R.fail(String.format("批量%s更新失败！", getResource()));
    }

    /**
     * 根据 id 更新指定字段
     *
     * @param domain 请求实体，{key:value}，实体需要有主键 id
     * @return {@code R<? extends T>}
     * @author xijieyin <br> 2022/8/4 18:40
     * @since 1.0.0
     */
    @Operation(summary = "根据 id 更新指定字段")
    @PatchMapping
    @WebLog
    @ApiLog(title = "根据 id 更新指定字段", description = "根据 id 更新指定字段", type = OperateType.UPDATE)
    default R<? extends T> updateAssignField(@Valid @NotNull @RequestBody U domain) {
        return getBaseService().customizeUpdateById(domain) ? R.data(domain) : R.fail(String.format("更新%s失败！", getResource()));
    }

    /**
     * 根据 id 批量更新指定字段
     *
     * @param domains 请求实体类集合，[{key:value},]，这个集合里面的所有的实体类都需要有主键 id
     * @return {@code R<Collection < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:42
     * @since 1.0.0
     */
    @Operation(summary = "根据 id 批量更新指定字段")
    @PatchMapping("batch")
    @WebLog
    @ApiLog(title = "根据 id 批量更新指定字段", description = "根据 id 批量更新指定字段", type = OperateType.UPDATE, isSaveRequestData = false, isSaveResultData = false)
    default R<Collection<? extends T>> updateAssignFieldBatch(@Valid @NotNull @RequestBody Collection<U> domains) {
        return getBaseService().customizeUpdateBatchById(domains) ? R.data(domains) : R.fail(String.format("批量更新%s失败！", getResource()));
    }

    /**
     * 根据 id 删除记录
     *
     * @param id 主键 id
     * @return {@code R<? extends T>}
     * @author xijieyin <br> 2022/8/4 18:43
     * @since 1.0.0
     */
    @Operation(summary = "根据 id 删除记录")
    @DeleteMapping("{id}")
    @WebLog
    @ApiLog(title = "根据 id 删除记录", description = "根据 id 删除记录", type = OperateType.DELETE)
    default R<? extends T> delete(@PathVariable P id) {
        return getBaseService().customizeRemoveById(id) ? R.ok(String.format("删除%s成功！", getResource())) : R.fail(String.format("删除%s失败！", getResource()));
    }

    /**
     * 根据 id 批量删除记录
     *
     * @param id 主键 id集合
     * @return {@code R<? extends T>}
     * @author xijieyin
     * @since 2.2.3
     */
    @Operation(summary = "根据 id 批量删除记录")
    @DeleteMapping("batch")
    @WebLog
    @ApiLog(title = "根据 id 批量删除记录", description = "根据 id 批量删除记录", type = OperateType.DELETE)
    default R<? extends T> deleteBatch(@RequestParam Set<P> id) {
        return getBaseService().customizeRemoveByIds(id) ? R.ok(String.format("批量删除%s成功！", getResource())) : R.fail(String.format("批量删除%s失败！", getResource()));
    }

}
