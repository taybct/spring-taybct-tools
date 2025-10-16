package io.github.mangocrisp.spring.taybct.tool.core.doc;

import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.ServerVariables;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * swagger 属性配置
 *
 * @author xijieyin <br> 2022/8/5 22:56
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "springdoc.info")
public class DocProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -330497982508649803L;

    /**
     * 标题
     **/
    private String title = "应用名称";

    /**
     * 描述
     **/
    private String description = "基于 SpringDoc OpenAPI 自动生成的接口文档，包含所有可用接口的详细信息和测试功能";

    /**
     * 版本
     **/
    private String version = "v3.5.0";

    /**
     * 联系人信息
     */
    private Contact contact = new Contact();

    /**
     * 许可证
     **/
    private License license = new License();

    /**
     * 服务器配置（可配置多环境，如开发、测试、生产）
     **/
    private List<Server> servers = new ArrayList<>();

    /**
     * 全局统一鉴权配置
     **/
    private Authorization authorization = new Authorization();

    /**
     * 扩展属性
     */
    private Map<String, Object> extensions = new HashMap<>();

    /**
     * 联系人信息
     */
    @Data
    @NoArgsConstructor
    public static class Contact {
        /**
         * 联系人
         **/
        private String name = "开发团队";
        /**
         * 联系人email
         **/
        private String email = "dev@example.com";
        /**
         * 联系人url
         **/
        private String url = "https://example.com";
        /**
         * 扩展属性
         */
        private Map<String, Object> extensions = new HashMap<>();
    }

    /**
     * 许可 License
     */
    @Data
    @NoArgsConstructor
    public static class License {
        /**
         * License
         **/
        private String name = "Apache 2.0";
        /**
         * 链接
         **/
        private String url = "https://www.apache.org/licenses/LICENSE-2.0.html";
        /**
         * 扩展属性
         */
        private Map<String, Object> extensions = new HashMap<>();
    }


    /**
     * 服务器配置（可配置多环境，如开发、测试、生产）
     */
    @Data
    @NoArgsConstructor
    public static class Server {
        /**
         * 服务器地址
         */
        private String url;
        /**
         * 服务器描述
         */
        private String description;
        /**
         * 服务器变量
         */
        private ServerVariables variables = null;
        /**
         * 扩展属性
         */
        private Map<String, Object> extensions = new HashMap<>();
    }

    /**
     * 鉴权配置
     */
    @Data
    @NoArgsConstructor
    public static class Authorization {
        /**
         * 鉴权类型
         */
        private SecurityScheme.Type type = SecurityScheme.Type.HTTP;
        /**
         * 安全方案 Key
         */
        private String securitySchemesKey = "JWT";
        /**
         * 鉴权方案
         */
        private String scheme = "bearer";
        /**
         * 鉴权格式
         */
        private String bearerFormat = "JWT";
        /**
         * 鉴权字符串存放的位置
         */
        private SecurityScheme.In in = SecurityScheme.In.HEADER;
        /**
         * 鉴权字符串的名称
         */
        private String name = "Authorization";
        /**
         * 扩展属性
         */
        private Map<String, Object> extensions = new ConcurrentHashMap<>();
    }

}