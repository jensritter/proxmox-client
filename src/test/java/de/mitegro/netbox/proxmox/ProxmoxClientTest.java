package de.mitegro.netbox.proxmox;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jens Ritter on 03.07.2024.
 */
public class ProxmoxClientTest {

    private final Logger logger = LoggerFactory.getLogger(ProxmoxClientTest.class);

    @BeforeAll
    static void setUp() throws IOException {
        Properties properties = new Properties();
        try (var fis = new FileInputStream(new File("c:\\daten\\login.xml"))) {
            properties.loadFromXML(fis);
        }

        ProxmoxClient myClient = new ProxmoxClient(properties.getProperty("HOST"), Integer.valueOf(properties.getProperty("PORT")));
        client = myClient.login(
            properties.getProperty("USERNAME"),
            properties.getProperty("PASSWORD"),
            properties.getProperty("REALM")
        );
    }

    static ProxmoxClient.MyClientSession client;

    @Test
    void testLogin() throws IOException {
        assertThat(client).isNotNull();
        Object version = client.version();
        assertThat(version).isNotNull();
        logger.info("Version : {}", version);
    }

    @Test
    void nodes() {
        List<ProxmoxClient.MyClientSession.Node> nodes = client.nodes();
        for (ProxmoxClient.MyClientSession.Node node : nodes) {
            logger.info("{}", node.node());
        }
    }

    @Test
    void vmStatus() {
        var nodes = client.vms("vm17");
        for (ProxmoxClient.MyClientSession.VmStatus vm : nodes) {
            logger.info("{}", vm.vmid());

        }
    }

    @Test
    void vmconfigs() {
        List<ProxmoxClient.MyClientSession.Node> nodes = client.nodes();
        for (ProxmoxClient.MyClientSession.Node node : nodes) {
            var vms = client.vms(node.node());
            for (ProxmoxClient.MyClientSession.VmStatus vm : vms) {
                ProxmoxClient.MyClientSession.Config config = client.config(node.node(), vm.vmid());
                Map<String, ProxmoxClient.MyClientSession.DiskInfo> stringDiskInfoMap = config.listDiskConfig();
                logger.info("{}", stringDiskInfoMap);
            }

        }

    }

    @Test
    void diskSum() throws ExecutionException, InterruptedException {
        for (ProxmoxClient.MyClientSession.Node node : client.nodes()) {
            int sum = 0;
            for (ProxmoxClient.MyClientSession.VmStatus vm17 : client.vms(node.node())) {

                ProxmoxClient.MyClientSession.Config qemu = client.config(node.node(), vm17.vmid());
                Map<String, ProxmoxClient.MyClientSession.DiskInfo> stringDiskInfoMap = qemu.listDiskConfig();

                LongSummaryStatistics collect = stringDiskInfoMap.values().stream()
                    .map(ProxmoxClient.MyClientSession.DiskInfo::sizeG)
                    .collect(Collectors.summarizingLong(Long::longValue));

                logger.info("{} {}:{}", node.node(), qemu.get("name"), collect.getSum());
                sum += collect.getSum();
            }
            logger.info("{}: {}", node.node(), sum);
        }
    }

    @Test
    void diskSumStream() {
        logger.info("start");
        List<ProxmoxClient.MyClientSession.Config> list = client.nodes().parallelStream()
            .map(it->client.vms(it.node()))
            .flatMap(it->it.stream())
            .map(it->client.config(it.node(), it.vmid()))
            .toList();

        logger.info("done");


    }

}
