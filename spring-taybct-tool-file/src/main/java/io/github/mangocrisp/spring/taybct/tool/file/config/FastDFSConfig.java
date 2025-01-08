package io.github.mangocrisp.spring.taybct.tool.file.config;

import com.github.tobato.fastdfs.FdfsClientConfig;
import com.github.tobato.fastdfs.service.DefaultFastFileStorageClient;
import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;

/**
 * FastDFS 自定义配置文件
 *
 * @author xijieyin <br> 2022/8/4 16:09
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@RefreshScope
@ToString
@ConfigurationProperties(prefix = PropertiesPrefixConstants.FILE + ".fdfs")
@Import(FdfsClientConfig.class)
// 解决jmx重复注册bean的问题
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class FastDFSConfig {
    /**
     * 域名或本机访问地址
     */
    private String url;
    /**
     * 分组名
     */
    private String group;

    @Bean
    public FastDFSClient fastDFSClient(@Autowired DefaultFastFileStorageClient client) {
        return new FastDFSClient(client);
    }
}
