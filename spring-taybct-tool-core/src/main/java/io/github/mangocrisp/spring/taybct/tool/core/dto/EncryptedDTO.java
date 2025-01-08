package io.github.mangocrisp.spring.taybct.tool.core.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 加密数据传输对象
 *
 * @author XiJieYin <br> 2024/1/9 11:45
 */
@Data
public class EncryptedDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3831589250828550337L;
    /**
     * 加密内容
     */
    private String content;
    /**
     * AES key（RSA 加密后的）
     */
    private String aesKey;
    /**
     * 输出内容加密用的 rsa 公钥（由客户端提供非服务端公钥）
     */
    private String rsaPublicKey;

}
