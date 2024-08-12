package org.jens.proxmox.config;

import org.jens.proxmox.ProxmoxClient;
import org.jens.proxmox.config.properties.ProxmoxProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jens Ritter on 12.08.2024.
 */
@Configuration
@EnableConfigurationProperties(ProxmoxProperties.class)
public class AutoConfigureProxmox {
    private final Logger logger = LoggerFactory.getLogger(AutoConfigureProxmox.class);

    @ConditionalOnMissingBean
    @Bean
    public ProxmoxClient proxmoxClient(ProxmoxProperties properties) {
        if (logger.isInfoEnabled()) {
            logger.info("Using connection-properties : {} {}",
                properties.getSchema() + "://" + properties.getHostname() + ":" + properties.getHostname(),
                properties.getUsername() + "@" + properties.getRealm());
        }
        return new ProxmoxClient(properties);
    }

}
