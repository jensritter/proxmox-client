package de.mitegro.netbox.proxmox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jens Ritter on 12.08.2024.
 */
class ProxmoxClientConfigTest {
    @BeforeEach
    void setUp() {
        ProxmoxClient.MyClientSession.Config cfg = new ProxmoxClient.MyClientSession.Config("node", 10, null);
    }

    ProxmoxClient.MyClientSession.Config buildDiskLine(String diskType, String line) {
        Map<String, String> values = new HashMap<>();
        values.put(diskType, line);
        return new ProxmoxClient.MyClientSession.Config("fake", -1, values);
    }

    @Test
    void testGetConfig() {
        var tjweb1 = buildDiskLine("scsi0", "local-lvm:vm-172-disk-1,format=raw,iothread=1,size=90G");
        Map<String, ProxmoxClient.MyClientSession.DiskInfo> disks = tjweb1.listDiskConfig();
        assertThat(disks).containsOnlyKeys("scsi0");
        ProxmoxClient.MyClientSession.DiskInfo scsi0 = disks.get("scsi0");
        assertThat(scsi0.filename()).isEqualTo("vm-172-disk-1");
        assertThat(scsi0.storageType()).isEqualTo("local-lvm");

    }
}
