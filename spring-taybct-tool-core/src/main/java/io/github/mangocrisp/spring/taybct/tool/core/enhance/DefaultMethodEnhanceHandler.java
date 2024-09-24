package io.github.mangocrisp.spring.taybct.tool.core.enhance;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 默认的方法增强处理器
 *
 * @author XiJieYin <br> 2024/4/19 15:17
 */
@Getter
public class DefaultMethodEnhanceHandler implements Serializable {

    private static final long serialVersionUID = -1136397121651304340L;

    List<IMethodEnhanceHandler> handlerList = new ArrayList<>();

    public List<IMethodEnhanceHandler> add(IMethodEnhanceHandler methodEnhanceHandler) {
        this.handlerList.add(methodEnhanceHandler);
        return this.handlerList;
    }

    public List<IMethodEnhanceHandler> add(int index, IMethodEnhanceHandler methodEnhanceHandler) {
        this.handlerList.add(index, methodEnhanceHandler);
        return this.handlerList;
    }

    public List<IMethodEnhanceHandler> addAll(Collection<IMethodEnhanceHandler> methodEnhanceHandler) {
        this.handlerList.addAll(methodEnhanceHandler);
        return this.handlerList;
    }

    public List<IMethodEnhanceHandler> addAll(int index, Collection<IMethodEnhanceHandler> methodEnhanceHandler) {
        this.handlerList.addAll(index, methodEnhanceHandler);
        return this.handlerList;
    }


}
