package io.github.mangocrisp.spring.taybct.tool.core.handle;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.github.mangocrisp.spring.taybct.tool.core.config.TableFieldDefaultHandler;

import java.io.Serializable;

/**
 * 默认主键值设置
 *
 * @author XiJieYin <br> 2024/4/18 14:49
 */
public interface TableFieldDefaultPKHandler extends TableFieldDefaultHandler<Serializable> {

    @Override
    default Serializable get(Object entity) {
        return IdWorker.getId();
    }
}
