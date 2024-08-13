# Spring-Client für Proxmox-CE

See Proxmox-Documentation :

https://pve.proxmox.com/pve-docs/api-viewer/

## Usage

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

