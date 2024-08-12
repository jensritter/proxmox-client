package org.jens.proxmox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jens Ritter on 12.08.2024.
 */
class VmConfigTest {
    @BeforeEach
    void setUp() {
        VmConfig cfg = new VmConfig("node", 10, null);
    }

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
        assertThat(scsi0.storageType()).isEqualTo("local-lvm");
    }
}
