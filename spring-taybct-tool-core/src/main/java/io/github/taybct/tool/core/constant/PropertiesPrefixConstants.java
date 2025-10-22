package io.github.taybct.tool.core.constant;

/**
 * 配置前缀常量
 *
 * @author xijieyin <br> 2022/8/5 18:19
 * @since 1.0.0
 */
public interface PropertiesPrefixConstants {
    /**
     * 统一所有的 properties 的前缀
     */
    String TAYBCT = "taybct";
    /**
     * 属性配置
     */
    String PROP_CONFIG = TAYBCT + ".prop-config";
    /**
     * 全局异常配置
     */
    String GLOBAL_EXCEPTION = TAYBCT + ".global-exception";
    /**
     * 安全相关
     */
    String SECURE = TAYBCT + ".secure";
    /**
     * 安全 api
     */
    String SECURE_API = SECURE + ".api";
    /**
     * 鉴权
     */
    String AUTH = SECURE + ".auth";
    /**
     * 文件相关
     */
    String FILE = TAYBCT + ".file";
    /**
     * 消息
     */
    String MESSAGE = TAYBCT + ".message";
    /**
     * 多租户
     */
    String TENANT = TAYBCT + ".tenant";
    /**
     * RSA 配置
     */
    String RSA = TAYBCT + ".rsa";
    /**
     * RSA 配置
     */
    String SM2 = TAYBCT + ".sm2";
    /**
     * 数据相关
     */
    String DATA = TAYBCT + ".data";
    /**
     * 运维管理
     */
    String OAM = TAYBCT + ".oam";
    /**
     * 任务调度
     */
    String SCHEDULED = TAYBCT + ".scheduled";
    /**
     * 参数配置
     */
    String PARAMS = TAYBCT + ".params";
    /**
     * 数据权限
     */
    String DATA_SCOPE = TAYBCT + ".data-scope";

}
