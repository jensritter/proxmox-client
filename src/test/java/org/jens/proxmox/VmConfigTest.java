package org.jens.proxmox;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jens Ritter on 12.08.2024.
 */
class VmConfigTest {
    VmConfig buildDiskLine(String diskType, String line) {
        Map<String, String> values = new HashMap<>();
        values.put(diskType, line);
        return new VmConfig("fake", -1, values);
    }

    @Test
    void testGetConfig() {
        var tjweb1 = buildDiskLine("scsi0", "local-lvm:vm-172-disk-1,format=raw,iothread=1,size=90G");
        Map<String, VmConfig.DiskInfo> disks = tjweb1.listDiskConfig();
        assertThat(disks).containsOnlyKeys("scsi0");
        VmConfig.DiskInfo scsi0 = disks.get("scsi0");
        assertThat(scsi0.filename()).isEqualTo("vm-172-disk-1");
        assertThat(scsi0.storageName()).isEqualTo("local-lvm");
        assertThat(scsi0.format()).isEqualTo("raw");
        assertThat(scsi0.ssd()).isFalse();
        assertThat(scsi0.iothread()).isTrue();
    }

    @Test
    void testName() {
        var cfg = new VmConfig("node", 10, Map.of("name", "hostname"));
        assertThat(cfg.getVmid()).isEqualTo(10);
        assertThat(cfg.getNode()).isEqualTo("node");
        assertThat(cfg.getName()).isEqualTo("hostname");


    }
}
