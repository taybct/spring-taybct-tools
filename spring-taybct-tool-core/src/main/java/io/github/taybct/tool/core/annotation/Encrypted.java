package io.github.taybct.tool.core.annotation;

import io.github.taybct.tool.core.enums.EncryptedType;

import java.lang.annotation.*;

/**
 * 加密传输
 *
 * @author xijieyin <br> 2022/10/12 15:24
 * @since 1.0.5
 */
@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Encrypted {
    /**
     * 解密输入数据
     */
    boolean decryptInput() default true;

    /**
     * 需要解密的输入数据的 key
     */
    String key() default "data";

    /**
     * 是否要加密输出数据
     */
    boolean encryptOutput() default true;

    /**
     * 加密输出数据的公钥的 key，这个需要前端传 public key 过来
     */
    String outputEncryptPublicKey() default "rsaPublicKey";

    /**
     * 加密类型
     *
     * @return 接口加密类型
     * @see EncryptedType
     */
    EncryptedType type() default EncryptedType.RSA;

    /**
     * 加密的内容的 JSON 键 (POST 请求的时候需要从请求的 JSON 里面获取内容)
     *
     * @return 加密的内容的 JSON 键
     */
    String content() default "content";

    /**
     * 加密的 AES Key 的 JSON 键 (POST 请求的时候需要从请求的 JSON 里面获取 AES Key) (type 为 {@linkplain EncryptedType#AES_RSA AES} 模式时使用)
     *
     * @return 加密的 AES Key 的 JSON 键
     */
    String aesKey() default "aesKey";
}
