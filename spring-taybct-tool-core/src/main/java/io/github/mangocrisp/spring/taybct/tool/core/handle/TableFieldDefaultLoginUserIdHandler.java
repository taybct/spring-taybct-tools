package io.github.mangocrisp.spring.taybct.tool.core.handle;

import io.github.mangocrisp.spring.taybct.tool.core.bean.ISecurityUtil;
import io.github.mangocrisp.spring.taybct.tool.core.config.TableFieldDefaultHandler;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;

import java.io.Serializable;

/**
 * 登录用户 id 处理器
 *
 * @author XiJieYin <br> 2023/7/3 19:35
 */
public interface TableFieldDefaultLoginUserIdHandler extends TableFieldDefaultHandler<Serializable> {

    @Override
    default Serializable get(Object entity) {
        try {
            ISecurityUtil securityUtil = SpringUtil.getBean(ISecurityUtil.class);
            return (Long) securityUtil.getLoginUser().getUserId();
        } catch (Exception e) {
            return null;
        }
    }

}
