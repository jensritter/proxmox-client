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
//
//    @Test
//    void nodes() {
//        List<ProxmoxSession.Node> nodes = client.queryNodes();
//        for (ProxmoxSession.Node node : nodes) {
//            logger.info("{}", node.node());
//        }
//    }
//
//    @Test
//    void vmStatus() {
//        var nodes = client.queryVms("vm17");
//        for (ProxmoxSession.VmStatus vm : nodes) {
//            logger.info("{}", vm.vmid());
//
//        }
//    }
//
//    @Test
//    void vmconfigs() {
//        List<ProxmoxSession.Node> nodes = client.queryNodes();
//        for (ProxmoxSession.Node node : nodes) {
//            var vms = client.queryVms(node.node());
//            for (ProxmoxSession.VmStatus vm : vms) {
//                ProxmoxSession.Config config = client.queryConfig(node.node(), vm.vmid());
//                Map<String, ProxmoxSession.DiskInfo> stringDiskInfoMap = config.listDiskConfig();
//                logger.info("{}", stringDiskInfoMap);
//            }
//        }
//
//    }
//
//    @Test
//    void diskSum() throws ExecutionException, InterruptedException {
//        for (ProxmoxSession.Node node : client.queryNodes()) {
//            int sum = 0;
//            for (ProxmoxSession.VmStatus vm17 : client.queryVms(node.node())) {
//
//                ProxmoxSession.Config qemu = client.queryConfig(node.node(), vm17.vmid());
//                Map<String, ProxmoxSession.DiskInfo> stringDiskInfoMap = qemu.listDiskConfig();
//
//                LongSummaryStatistics collect = stringDiskInfoMap.values().stream().map(ProxmoxSession.DiskInfo::sizeG).collect(Collectors.summarizingLong(Long::longValue));
//
//                logger.info("{} {}:{}", node.node(), qemu.get("name"), collect.getSum());
//                sum += collect.getSum();
//            }
//            logger.info("{}: {}", node.node(), sum);
//        }
//    }
//
//    @Test
//    void diskSumStream() {
//        logger.info("start");
//        List<ProxmoxSession.Config> list = client.queryNodes().parallelStream().map(it->client.queryVms(it.node())).flatMap(it->it.stream())
//        .map(it->client.queryConfig(it.node(), it.vmid())).toList();
//
//        logger.info("done");
//
//
//    }

}
