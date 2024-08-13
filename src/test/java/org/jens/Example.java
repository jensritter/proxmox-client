package org.jens;

import org.jens.proxmox.ProxmoxClient;
import org.jens.proxmox.ProxmoxSession;
import org.jens.proxmox.VmConfig;
import org.jens.proxmox.VmConfig.DiskInfo;
import org.jens.proxmox.config.properties.ProxmoxProperties;

import java.util.List;
import java.util.Map;

import static org.jens.proxmox.ProxmoxSession.Node;
import static org.jens.proxmox.ProxmoxSession.VmStatus;

/**
 * Code for Readme.
 *
 * @author Jens Ritter on 13.08.2024.
 */
public class Example {
    public static void main(String[] args) {
        ProxmoxProperties props = new ProxmoxProperties();
        props.setHostname("localhost");
        props.setPort(8006);
        props.setSchema("https");

        ProxmoxClient client = new ProxmoxClient(props);

        ProxmoxSession session = client.login();

        List<Node> nodes = session.queryNodes();
        List<VmStatus> pve = session.queryVms("pve");

        VmConfig vmConfig = session.queryConfig("pve", 1);
        Map<String, DiskInfo> diskConfig = vmConfig.listDiskConfig();
        for (DiskInfo value : diskConfig.values()) {
            System.out.println(value);
        }
    }
}
