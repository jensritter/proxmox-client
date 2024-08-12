package de.mitegro.netbox.proxmox.config;

import de.mitegro.netbox.proxmox.config.properties.ProxmoxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jens Ritter on 12.08.2024.
 */
@Configuration
@EnableConfigurationProperties(ProxmoxProperties.class)
public class AutoConfigureProxmox {

}
