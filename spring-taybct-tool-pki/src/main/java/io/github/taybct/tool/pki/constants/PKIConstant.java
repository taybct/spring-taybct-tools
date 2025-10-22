package io.github.taybct.tool.pki.constants;

/**
 * PKI 配置常量
 *
 * @author XiJieYin <br> 2023/8/3 16:51
 */
public class PKIConstant {

    /**
     * 配置常量
     *
     * @author weichang_ding
     */
    public static class ConfigConstant {
        // 认证地址
        public static final String KEY_AUTH_URL = "authURL";

        // 应用标识
        public static final String KEY_APP_ID = "appId";

        // 是否开启开维码认证
        public static final String KEY_QRCODE_AUTH = "QRCodeAuth";

        // 调用应用服务器或网关生成原文
        public static final String KEY_RANDOM_FROM = "randomFrom";

        // 是否检查访问控制状态
        public static final String KEY_ACCESS_CONTROL = "accessControl";

        // 生成二维码地址
        public static final String KEY_GENERATEQRCODE_URL = "generateQRCodeURL";

        // 查询二维码状态地址
        public static final String KEY_QUERYQRCODESTATE_URL = "queryQRCodeStateURL";

        // 配置文件加载成功标记
        public static final String KEY_CONFIG_SUCCESS = "configSuccess";
    }

    /**
     * 报文公共常量
     *
     * @author weichang_ding
     */
    static class CommonConstant {
        // 报文根结点
        public static final String MSG_ROOT = "message";

        // 报文头结点
        public static final String MSG_HEAD = "head";

        // 报文体结点
        public static final String MSG_BODY = "body";

        // 服务版本号
        public static final String MSG_VSERSION = "version";

        // 服务版本值
        public static final String MSG_VSERSION_VALUE_10 = "1.0";
        public static final String MSG_VSERSION_VALUE_11 = "1.1";

        // 服务类型
        public static final String MSG_SERVICE_TYPE = "serviceType";

        // 报文体 应用ID
        public static final String MSG_APPID = "appId";

        // 响应报文状态
        public static final String MSG_MESSAGE_STATE = "messageState";

        // 错误代码
        public static final String MSG_MESSAGE_CODE = "messageCode";

        // 错误描述
        public static final String MSG_MESSAGE_DESC = "messageDesc";

        // 认证原文
        public static final String MSG_ORIGINAL = "original";
    }

    /**
     * 随机数报文常量
     *
     * @author weichang_ding
     */
    static class RandomConstant {
        // 服务类型值
        public static final String MSG_SERVICE_TYPE_VALUE = "OriginalService";
    }

    /**
     * 认证报文常量
     *
     * @author weichang_ding
     */
    public static class AuthConstant {
        // 服务类型值
        public static final String MSG_SERVICE_TYPE_VALUE = "AuthenService";

        // 报文体 认证方式
        public static final String MSG_AUTH_MODE = "authMode";

        // 报文体 证书认证方式
        public static final String MSG_AUTH_MODE_CERT_VALUE = "cert";

        // 报文体 口令认证方式
        public static final String MSG_AUTH_MODE_PASSWORD_VALUE = "password";

        // 报文体 二维码认证方式
        public static final String MSG_AUTH_MODE_QRCODE_VALUE = "qrcode";

        // 报文体 属性集
        public static final String MSG_ATTRIBUTES = "attributes";

        // 报文体 自定义属性集
        public static final String MSG_CUSTOM_ATTRIBUTES = "customAttributes";

        // 报文体 属性
        public static final String MSG_ATTRIBUTE = "attr";

        // 报文体 属性名
        public static final String MSG_NAME = "name";

        // 报文父级节点
        public static final String MSG_PARENT_NAME = "parentName";

        // 报文体 属性空间
        public static final String MSG_NAMESPACE = "namespace";

        // 访问控制
        public static final String MSG_ACCESS_CONTROL = "accessControl";

        // 访问控制 ture
        public static final String MSG_ACCESS_CONTROL_TRUE = "true";

        // 访问控制 false
        public static final String MSG_ACCESS_CONTROL_FALSE = "false";

        // 报文体 认证结点
        public static final String MSG_AUTH = "authen";

        // 报文体 认证凭据
        public static final String MSG_AUTHCREDENTIAL = "authCredential";

        // 报文体 客户端结点
        public static final String MSG_CLIENT_INFO = "clientInfo";

        // 报文体 公钥证书
        public static final String MSG_CERT_INFO = "certInfo";

        // 报文体 客户端结点
        public static final String MSG_CLIENT_IP = "clientIP";

        // 报文体 detach认证请求包
        public static final String MSG_DETACH = "detach";

        // 报文体 证书类型，PM 证书为：PM
        public static final String MSG_CERTTYPE = "certType";

        // 报文体 用户名
        public static final String MSG_USERNAME = "username";

        // 报文体 口令
        public static final String MSG_PASSWORD = "password";

        // 报文体 Token
        public static final String MSG_TOKEN = "token";

        // QRCode
        public static final String MSG_QRCODE = "QRCode";

        // 报文体 属性类型
        public static final String MSG_ATTRIBUTE_TYPE = "attributeType";

        // 指定属性 portion
        public static final String MSG_ATTRIBUTE_TYPE_PORTION = "portion";

        // 指定属性 all
        public static final String MSG_ATTRIBUTE_TYPE_ALL = "all";

        // 指定属性列表控制项 attrType
        public static final String MSG_ATTR_TYPE = "attrType";

        // 报文体 认证结果集
        public static final String MSG_AUTH_RESULT_SET = "authResultSet";

        // 报文体 认证结果
        public static final String MSG_AUTH_RESULT = "authResult";

        // 报文体 认证结果状态
        public static final String MSG_SUCCESS = "success";

        // 报文体 认证错误码
        public static final String MSG_AUTH_MESSSAGE_CODE = "authMessageCode";

        // 报文体 认证错误描述
        public static final String MSG_AUTH_MESSSAGE_DESC = "authMessageDesc";

        // 二维码随机数
        public static final String KEY_JIT_QRCODE = "jit_qrcode";

        // session中原文
        public static final String KEY_ORIGINAL_DATA = "original_data";

        // 客户端返回的认证原文，request中原文
        public static final String KEY_ORIGINAL = "original";

        // 签名结果
        public static final String KEY_SIGNED_DATA = "signed_data";

        // namespace 证书信息
        public static final String KEY_NAMESPACE_CINAS = "http://www.jit.com.cn/cinas/ias/ns/saml/saml11/X.509";

        // namespace ums信息
        public static final String KEY_NAMESPACE_UMS = "http://www.jit.com.cn/ums/ns/user";

        // namespace pms信息
        public static final String KEY_NAMESPACE_PMS = "http://www.jit.com.cn/pmi/pms";

        // namespace 自定义信息
        public static final String KEY_NAMESPACE_CUSTOM = "http://www.jit.com.cn/gw/custom/attribute";
    }

    /**
     * 二维码相关常量
     *
     * @author weichang_ding
     */
    public static class QRConstant {
        // jit_qrcode
        public static final String KEY_JIT_QRCODE = "jit_qrcode";

        // appFlag
        public static final String KEY_APP_FLAG = "appFlag";

        // 服务类型
        public static final String KEY_SERVICE_TYPE = "Service_Type";

        // 生成二维码
        public static final String KEY_QRCODE_GENERATE = "qrcode_generate";

    }

}
