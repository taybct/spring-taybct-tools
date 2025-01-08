package io.github.mangocrisp.spring.taybct.tool.core.enums;

/**
 * 接口加密类型
 *
 * @author XiJieYin <br> 2023/5/30 11:21
 */
public enum EncryptedType {
    /**
     * 单纯 非对称 加密，数据较少时推荐，如仅加密身份证，手机号，密码等
     */
    RSA,
    /**
     * 对称加密内容，非对称加密 对称加密的 Key，数据较多的时推荐，如需要加密大量的 JSON 数据，字符串等
     */
    AES_RSA
}
