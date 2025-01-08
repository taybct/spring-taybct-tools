package io.github.mangocrisp.spring.taybct.tool.core.poi.easyexcel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ModelConvertible;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 模型转换监听器
 *
 * @param <T> 需要转换成哪种数据类型
 */
public class ModelConvertibleListener<T> implements ReadListener<ModelConvertible<T>> {
    /**
     * 缓存数据
     */
    private final List<ModelConvertible<T>> cachedDataList;
    /**
     * 数据获取
     */
    private final Consumer<List<T>> consumer;

    /**
     * 单次读取的大小
     */
    private final int size;

    /**
     * 默认是读取全部数据
     *
     * @param consumer 获取最后读取的结果
     */
    public ModelConvertibleListener(Consumer<List<T>> consumer) {
        this(consumer, 0);
    }

    /**
     * 分页读取数据
     *
     * @param consumer 获取最后读取的结果
     * @param size     分页大小
     */
    public ModelConvertibleListener(Consumer<List<T>> consumer, int size) {
        this.consumer = consumer;
        this.size = size;
        if (size > 0) {
            this.cachedDataList = ListUtils.newArrayListWithExpectedSize(this.size);
        } else {
            this.cachedDataList = new ArrayList<>();
        }
    }

    @Override
    public void invoke(ModelConvertible<T> data, AnalysisContext context) {
        cachedDataList.add(data);
        if (size > 0 && cachedDataList.size() >= size) {
            accept(cachedDataList);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (CollectionUtils.isNotEmpty(cachedDataList)) {
            accept(cachedDataList);
        }
    }

    /**
     * 把数据异步导出
     *
     * @param list 读取到的数据
     */
    public void accept(List<ModelConvertible<T>> list) {
        consumer.accept(list.stream().map(ModelConvertible::bean).toList());
        list.clear();
    }

}
