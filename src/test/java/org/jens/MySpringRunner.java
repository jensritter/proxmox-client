package org.jens;

import org.jens.proxmox.config.properties.ProxmoxProperties;
import org.jens.shorthand.spring.test.annotation.ShorthandTestSpring;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jens Ritter on 12.08.2024.
 */
@ShorthandTestSpring(classes=MySpringRunner.MyTestConfig.class)
@ExtendWith(SpringExtension.class)
public class MySpringRunner {

    @Configuration
    @EnableAutoConfiguration
    public static class MyTestConfig {

    }

    @Autowired
    private ProxmoxProperties properties;

    @Test
    void testDi() {
        assertThat(properties).isNotNull();
        assertThat(properties.getHostname())
            .describedAs("create application.properties with login-data in " + new File(".").getAbsoluteFile())
            .isNotBlank();
    }
}
