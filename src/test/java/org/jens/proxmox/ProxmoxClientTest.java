package org.jens.proxmox;

import org.jens.MySpringRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jens Ritter on 03.07.2024.
 */
public class ProxmoxClientTest extends MySpringRunner {

    private final Logger logger = LoggerFactory.getLogger(ProxmoxClientTest.class);

    @Autowired
    private ProxmoxClient proxmoxClient;

    @BeforeEach
    void setUp() {client = proxmoxClient.login();}

    ProxmoxSession client;

    @Test
    void testLogin() {
        assertThat(client).isNotNull();
        ProxmoxSession.Version version = client.queryVersion();
        assertThat(version).isNotNull();
        assertThat(version.release()).isNotBlank();
        assertThat(version.repoid()).isNotBlank();
        assertThat(version.version()).isNotBlank();
        logger.info("Version : {}", version);
    }
}
