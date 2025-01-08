package io.github.mangocrisp.spring.taybct.tool.core.doc;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * swagger 属性配置
 *
 * @author xijieyin <br> 2022/8/5 22:56
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("swagger")
public class SwaggerProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -330497982508649803L;
    /**
     * 是否开启swagger
     */
    @Deprecated
    private Boolean enabled = true;

    /**
     * swagger会解析的包路径
     **/
    private String basePackage = "";

    /**
     * swagger会解析的url规则
     **/
    private List<String> basePath = new ArrayList<>();

    /**
     * 在basePath基础上需要排除的url规则
     **/
    private List<String> excludePath = new ArrayList<>();

    /**
     * 需要排除的服务
     */
    private List<String> ignoreProviders = new ArrayList<>();

    /**
     * 标题
     **/
    private String title = "";

    /**
     * 描述
     **/
    private String description = "";

    /**
     * 版本
     **/
    private String version = "";

    /**
     * 许可证
     **/
    private String license = "";

    /**
     * 许可证URL
     **/
    private String licenseUrl = "";

    /**
     * 服务条款URL
     **/
    private String termsOfServiceUrl = "";

    /**
     * host信息
     **/
    private String host = "";

    /**
     * 联系人信息
     */
    @Deprecated
    private Contact contact = new Contact();

    /**
     * 扩展属性
     */
    @Deprecated
    private Map<String, Object> extensions = new ConcurrentHashMap<>();

    /**
     * 全局统一鉴权配置
     **/
    @Deprecated
    private Authorization authorization = new Authorization();

    @Data
    @Deprecated
    @NoArgsConstructor
    public static class Contact {
        /**
         * 联系人
         **/
        private String name = "";
        /**
         * 联系人url
         **/
        private String url = "";
        /**
         * 联系人email
         **/
        private String email = "";
    }

    @Data
    @Deprecated
    @NoArgsConstructor
    public static class Authorization {
        /**
         * 鉴权策略ID，需要和SecurityReferences ID保持一致
         */
        private String name = "oauth2";

        /**
         * 需要开启鉴权URL的正则 "/.*"
         */
        private String authRegex = "^.*$";

        /**
         * 鉴权作用域列表
         */
        private List<AuthorizationScope> authorizationScopes = new ArrayList<>();

        /**
         * 鉴权接口的请求方法
         */
        private HttpMethodSelector httpMethodList = new HttpMethodSelector();

        /**
         * 获取授权码地址
         */
        private String authorizeUrl = "http://localhost:9000/oauth/authorize";

        /**
         * 客户端 id
         */
        private String clientId = "app1";

        /**
         * 客户端密钥
         */
        private String clientSecretName = "123456";

        /**
         * 鉴权请求地址
         */
        private String tokenUrl = "http://localhost:8080/auth/oauth/token";

        /**
         * token 名，默认 access_token
         */
        private String tokenName = "access_token";
    }

    /**
     * 作用域
     */
    @Data
    @Deprecated
    @NoArgsConstructor
    public static class AuthorizationScope {
        /**
         * 作用域名称 read | write | reads | writes | all | global
         */
        private String scope = "";

        /**
         * 作用域描述
         */
        private String description = "";
    }

    @Data
    @Deprecated
    @NoArgsConstructor
    public static class HttpMethodSelector<HttpMethod> implements Predicate<HttpMethod> {

        /**
         * 鉴权接口的请求方法，即什么请求才鉴权
         */
        private List<String> names = new ArrayList<>();

        @Override
        public boolean test(HttpMethod httpMethod) {
            return names.contains(httpMethod.toString());
        }

    }
}