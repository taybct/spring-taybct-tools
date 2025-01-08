package io.github.mangocrisp.spring.taybct.tool.core.util.sm;

import io.github.mangocrisp.spring.taybct.tool.core.config.PropConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 * SMX Coder 配置
 *
 * @author xijieyin <br> 2022/10/12 15:01
 * @since 1.0.5
 */
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(SM2Properties.class)
public class SMXCoderIniRunner implements ApplicationRunner {

    final PropConfig propConfig;

    final SM2Properties sm2Properties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        SM2Coder.ini(sm2Properties);
        SM3Coder.ini(propConfig);
        SM4Coder.ini(propConfig);
    }

}
