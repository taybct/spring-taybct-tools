package io.github.mangocrisp.spring.taybct.tool.core.bean.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.ApiLog;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.WebLog;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ModelConvertible;
import io.github.mangocrisp.spring.taybct.tool.core.bean.UpdateModel;
import io.github.mangocrisp.spring.taybct.tool.core.constant.OperateType;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.support.SqlPageParams;
import io.github.mangocrisp.spring.taybct.tool.core.mybatisUtil.Condition;
import io.github.mangocrisp.spring.taybct.tool.core.result.R;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.MyBatisUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 请求模型转换 Controller
 *
 * @param <T>  实体类类型
 * @param <S>  Service 类型
 * @param <P>  实体类主键类型
 * @param <QM> 查询类型
 * @param <AM> 新增类型
 * @param <UM> 修改类型
 */
public interface ModelConvertibleController<T
        , S extends IService<T>
        , P extends Serializable
        , QM extends ModelConvertible<? extends T>
        , AM extends ModelConvertible<? extends T>
        , UM extends ModelConvertible<? extends T>> {

    default S getBaseService() {
        Class<Object> interfaceT = BeanUtil.getInterfaceT(this, ModelConvertibleController.class, 1);
        return (S) SpringUtil.getBean(interfaceT);
    }

    default Class<T> getEntityClass() {
        return BeanUtil.getInterfaceT(this, ModelConvertibleController.class, 0);
    }

    /**
     * 自定义查询条件，这里默认是使用 {@link MyBatisUtil} 根据查询参数生成的条件,
     * 这个查询条件是适用于分页和不分页的，就是说，分页不分页都是使用这个查询条件来查询，
     * 除非，你自己继承之后再做判断
     * 你可以继承他来修改你自己定义的查询条件
     *
     * @param dto        查询参数
     * @param pageParams 分页参数
     * @return Wrapper&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:25
     * @since 1.0.2
     */
    default Wrapper<T> customizeQueryWrapper(T dto, SqlPageParams pageParams) {
        return pageParams.genQueryWrapper(dto);
    }

    /**
     * 自定义查询条件，这里默认是使用 {@link MyBatisUtil} 根据查询参数生成的条件,
     * 这个查询条件是适用于分页和不分页的，就是说，分页不分页都是使用这个查询条件来查询，
     * 除非，你自己继承之后再做判断
     * 你可以继承他来修改你自己定义的查询条件
     *
     * @param params 所有参数
     * @return Wrapper&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:25
     * @since 1.0.2
     */
    default Wrapper<T> customizeQueryWrapper(Map<String, Object> params) {
        return MyBatisUtil.genQueryWrapper(params, getEntityClass());
    }

    /**
     * 自定义查询分页，这里默认是使用 {@link MyBatisUtil} 根据查询参数生成的分页
     * 你可以继承他来修改你自己定义的分页
     *
     * @param pageParams 分页参数
     * @return IPage&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:38
     * @since 1.0.2
     */
    default IPage<T> customizeQueryPage(SqlPageParams pageParams) {
        return pageParams.genPage();
    }

    /**
     * 自定义查询分页，这里默认是使用 {@link MyBatisUtil} 根据查询参数生成的分页
     * 你可以继承他来修改你自己定义的分页
     *
     * @param params 所有参数
     * @return IPage&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:38
     * @since 1.0.2
     */
    default IPage<T> customizeQueryPage(Map<String, Object> params) {
        return MyBatisUtil.genPage(params);
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
    @WebLog
    //@GetMapping("list")
    default R<? extends List<? extends T>> list(@RequestParam(required = false) Map<String, Object> params) {
        Page<T> page = (Page<T>) customizeQueryPage(params);
        // 这里不查询页数
        page.setSearchCount(false);
        return R.data(getBaseService().page(page, customizeQueryWrapper(params)).getRecords());
    }

    /**
     * 获取列表信息
     *
     * @param dto           {@literal 请求参数}
     * @param SqlPageParams SQL 查询参数
     * @return {@code R<List < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:34
     * @since 1.0.0
     */
    @Operation(summary = "获取列表")
    @WebLog
    //@PostMapping("list")
    default R<? extends List<? extends T>> list(@RequestBody QM dto, SqlPageParams SqlPageParams) {
        Page<T> page = (Page<T>) customizeQueryPage(SqlPageParams);
        // 这里不查询页数
        page.setSearchCount(false);
        return R.data(getBaseService().page(page, customizeQueryWrapper(dto.bean(), SqlPageParams)).getRecords());
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
    @WebLog
    //@GetMapping("page")
    default R<? extends IPage<? extends T>> page(@RequestParam(required = false) Map<String, Object> params) {
        return R.data(getBaseService().page(customizeQueryPage(params), customizeQueryWrapper(params)));
    }

    /**
     * 查询总数
     *
     * @param dto {@literal 请求参数}
     * @return {@code R<IPage < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:36
     * @since 1.0.0
     */
    @Operation(summary = "查询总数")
    @WebLog
    //@PostMapping("total")
    default R<Long> total(@RequestBody QM dto) {
        return R.data(getBaseService().count(MyBatisUtil.genQueryWrapper(dto.bean(), null)));
    }

    /**
     * 获取分页
     *
     * @param dto           {@literal 请求参数}
     * @param SqlPageParams {@literal 分页参数}
     * @return {@code R<IPage < ? extends T>>}
     * @author xijieyin <br> 2022/8/4 18:36
     * @since 1.0.0
     */
    @Operation(summary = "获取分页")
    @WebLog
    //@PostMapping("page")
    default R<? extends IPage<? extends T>> page(@RequestBody QM dto, SqlPageParams SqlPageParams) {
        return R.data(getBaseService().page(customizeQueryPage(SqlPageParams), customizeQueryWrapper(dto.bean(), SqlPageParams)));
    }

    /**
     * @param params 所有参数
     * @return {@code R<IPage < ? extends T>>}
     * @author jopson <br> 2024-05-17 14:51:00
     */
    @Operation(summary = "获取分页")
    @WebLog
    //@RequestMapping(value = "listPage", method = {RequestMethod.GET, RequestMethod.POST})
    default R<? extends IPage<? extends T>> listPage(@RequestParam(required = false) Map<String, Object> params) {
        return R.data(getBaseService().page(Condition.getPage(params), Condition.getQueryWrapper(params, getEntityClass())));
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
    @WebLog
    //@PostMapping
    @ApiLog(title = "新增对象", description = "新增一条记录，并且在新增成功后返回这个新增的对象，这个对象会带着生成的 id 一起返回", type = OperateType.INSERT)
    default R<? extends T> add(@Valid @NotNull @RequestBody AM domain) {
        return getBaseService().save(domain.bean()) ? R.data(domain.getConvertedBean()) : R.fail(String.format("新增%s失败！", getResource()));
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
    @WebLog
    //@PostMapping("batch")
    @ApiLog(title = "批量保存对象", description = "批量保存对象，并且在新增成功后一起返回", type = OperateType.INSERT, isSaveRequestData = false, isSaveResultData = false)
    default R<? extends Collection<? extends T>> saveBatch(@Valid @NotNull @RequestBody Collection<AM> domains) {
        List<? extends T> list = domains.stream().map(ModelConvertible::bean).toList();
        return getBaseService().saveBatch((Collection<T>) list) ? R.data(list) : R.fail(String.format("批量保存%s失败！", getResource()));
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
    @WebLog
    //@GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键 id", required = true, in = ParameterIn.PATH)
    })
    default R<? extends T> detail(@NotNull @PathVariable P id) {
        return R.data(getBaseService().getById(id));
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
    @WebLog
    @ApiLog(title = "根据 id 更新全部字段", description = "根据 id 更新全部字段", type = OperateType.UPDATE)
    //@PutMapping
    default R<? extends T> updateAllField(@Valid @NotNull @RequestBody UM domain) {
        return getBaseService().updateById(domain.bean()) ? R.data(domain.getConvertedBean()) : R.fail(String.format("更新%s失败！", getResource()));
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
    @WebLog
    //@PutMapping("batch")
    @ApiLog(title = "根据 id 批量更新全部字段", description = "根据 id 批量更新全部字段", type = OperateType.UPDATE, isSaveRequestData = false, isSaveResultData = false)
    default R<? extends Collection<? extends T>> updateAllFieldBatch(@Valid @NotNull @RequestBody Collection<UM> domains) {
        List<? extends T> list = domains.stream().map(ModelConvertible::bean).toList();
        return getBaseService().updateBatchById((Collection<T>) list) ? R.data(list) : R.fail(String.format("批量%s更新失败！", getResource()));
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
    @WebLog
    //@PatchMapping
    @ApiLog(title = "根据 id 更新指定字段", description = "根据 id 更新指定字段", type = OperateType.UPDATE)
    default R<? extends T> updateAssignField(@Valid @NotNull @RequestBody UM domain) {
        return getBaseService().updateById(domain.bean()) ? R.data(domain.getConvertedBean()) : R.fail(String.format("更新%s失败！", getResource()));
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
    @WebLog
    //@PatchMapping("batch")
    @ApiLog(title = "根据 id 批量更新指定字段", description = "根据 id 批量更新指定字段", type = OperateType.UPDATE, isSaveRequestData = false, isSaveResultData = false)
    default R<? extends Collection<? extends T>> updateAssignFieldBatch(@Valid @NotNull @RequestBody Collection<UM> domains) {
        List<? extends T> list = domains.stream().map(ModelConvertible::bean).toList();
        return getBaseService().updateBatchById((Collection<T>) list) ? R.data(list) : R.fail(String.format("批量更新%s失败！", getResource()));
    }

    /**
     * 修改数据
     *
     * @param model 更新数据模型
     * @return 修改是否成功
     */
    @Operation(summary = "根据 条件 批量更新数据")
    @WebLog
    // @PutMapping("condition")
    @ApiLog(title = "根据 条件 批量更新数据", description = "根据 条件 批量更新数据", type = OperateType.UPDATE)
    default R<?> update(@Valid @NotNull @RequestBody UpdateModel<T, UM, QM> model) {
        // TODO 这个需要自行写实现
        throw new BaseException("接口未实现！").setHttpStatus(HttpStatus.BAD_REQUEST);
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
    @WebLog
    //@DeleteMapping("/{id}")
    @ApiLog(title = "根据 id 删除记录", description = "根据 id 删除记录", type = OperateType.DELETE)
    @Parameters({
            @Parameter(name = "id", description = "主键 id", required = true, in = ParameterIn.PATH)
    })
    default R<? extends T> delete(@PathVariable P id) {
        return getBaseService().removeById(id) ? R.ok(String.format("删除%s成功！", getResource())) : R.fail(String.format("删除%s失败！", getResource()));
    }

    /**
     * 根据 id 批量删除记录
     *
     * @param ids 主键 id集合
     * @return {@code R<? extends T>}
     * @author xijieyin
     * @since 2.2.3
     */
    @Operation(summary = "根据 id 批量删除记录")
    @WebLog
    //@DeleteMapping("batch")
    @ApiLog(title = "根据 id 批量删除记录", description = "根据 id 批量删除记录", type = OperateType.DELETE)
    default R<? extends T> deleteBatch(@RequestBody Set<P> ids) {
        return getBaseService().removeByIds(ids) ? R.ok(String.format("批量删除%s成功！", getResource())) : R.fail(String.format("批量删除%s失败！", getResource()));
    }

}
