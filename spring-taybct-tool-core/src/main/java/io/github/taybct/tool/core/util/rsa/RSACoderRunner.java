package io.github.taybct.tool.core.util.rsa;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * RSA Coder 配置
 *
 * @author xijieyin <br> 2022/10/12 15:01
 * @since 1.0.5
 */
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(RSAProperties.class)
public class RSACoderRunner implements ApplicationRunner {

    final RSAProperties rsaProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        RSACoder.ini(rsaProperties);
    }
}