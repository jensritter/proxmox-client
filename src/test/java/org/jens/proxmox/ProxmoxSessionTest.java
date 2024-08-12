package org.jens.proxmox;

import org.jens.MySpringRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jens Ritter on 12.08.2024.
 */
class ProxmoxSessionTest extends MySpringRunner {

    private static final String TEST_VM = "vm17";

    private final Logger logger = LoggerFactory.getLogger(ProxmoxSessionTest.class);

    @Autowired
    private ProxmoxClient proxmoxClient;

    private static ProxmoxSession client;

    private static final ReentrantLock LOCK = new ReentrantLock();

    @BeforeEach
    void setUp() {
        LOCK.lock();
        try {
            if (client == null) {
                //noinspection NonThreadSafeLazyInitialization
                client = proxmoxClient.login();
            }
        } finally {
            LOCK.unlock();
        }

    }

    @Test
    void testClustername() {
        assertThat(client.getClustername()).isNotBlank();
    }

    @Test
    void testqueryVersion() throws IOException {
        assertThat(client).isNotNull();
        Object version = client.queryVersion();
        assertThat(version).isNotNull();
        logger.info("Version : {}", version);
    }

    @Test
    void testqueryNodes() {
        List<ProxmoxSession.Node> nodes = client.queryNodes();
        assertThat(nodes).isNotEmpty();

        for (ProxmoxSession.Node node : nodes) {
            logger.debug("node: {}", node.node());
        }
    }

    @Test
    void testqueryVms() {
        var nodes = client.queryVms(TEST_VM);
        assertThat(nodes).isNotEmpty();

        for (ProxmoxSession.VmStatus vm : nodes) {
            logger.debug("vmid : {}", vm.vmid());

        }
    }

    @Test
    void queryConfig() {
        var vm = client.queryVms(TEST_VM).stream().limit(1).findAny().orElseThrow(()->new RuntimeException("Missing some VM on " + TEST_VM));
        VmConfig vmConfig = client.queryConfig(TEST_VM, vm.vmid());
        assertThat(vmConfig).isNotNull();
        assertThat(vmConfig.getVmid()).isEqualTo(vm.vmid());
        assertThat(vmConfig.getNode()).isEqualTo(TEST_VM);

        assertThat(vmConfig.keySet()).contains("vmgenid");
        assertThat(vmConfig.get("vmgenid")).isNotBlank();
    }

    @Test
    @EnabledIfEnvironmentVariable(named="COMPUTERNAME", matches="ACHT")
    void itValidConfigTest() {
        Set<String> allConfigKeys = new HashSet<>();
        for (ProxmoxSession.Node node : client.queryNodes()) {
            for (ProxmoxSession.VmStatus vm : client.queryVms(node.node())) {
                VmConfig vmConfig = client.queryConfig(node.node(), vm.vmid());
                allConfigKeys.addAll(vmConfig.keySet());

                Map<String, VmConfig.DiskInfo> stringDiskInfoMap = vmConfig.listDiskConfig();
                assertThat(stringDiskInfoMap).isNotEmpty();
            }
        }


        var reducedSet = allConfigKeys.stream()
            .map(it->it.replaceAll("\\d$", "")) // löschen die letzte nummer
            .collect(Collectors.toSet());
        assertThat(reducedSet).containsExactlyInAnyOrder(
            "parent",
            "agent",
            "memory",
            "bios",
            "description",
            "protection",
            "cpulimit",
            "tpmstate",
            "sata", /* X */
            "scsi", /* X */
            "scsihw",
            "cores",
            "startup",
            "digest",
            "sockets",
            "net",
            "boot",
            "efidisk", /* X? */
            "balloon",
            "numa",
            "cpu",
            "ostype",
            "ide", /* X */
            "smbios",
            "vmgenid",
            "tags",
            "meta",
            "machine",
            "onboot",
            "name",
            "unused"
        );
        assertThat(allConfigKeys).hasSizeGreaterThan(reducedSet.size());

    }
}
