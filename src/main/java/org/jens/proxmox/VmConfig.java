package org.jens.proxmox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Jens Ritter on 12.08.2024.
 */
public class VmConfig {

    private final String node;
    private final int vmid;
    private final Map<String, String> map;

    /**
     * Privater Constructor. Obj wird nur durch Jackson initiert.
     *
     * @param node Node Hostname
     * @param vmid VmId
     * @param map  config-map
     * @see ProxmoxSession#queryConfig(String, int)
     */
    VmConfig(String node, int vmid, Map<String, String> map) {
        this.node = node;
        this.vmid = vmid;
        this.map = map != null ? Collections.unmodifiableMap(map) : null;
    }

    public String getNode() {return node;}

    public int getVmid() {return vmid;}

    public Set<String> keySet() {return this.map.keySet();}

    public String get(String key) {return this.map.get(key);}

    public Map<String, DiskInfo> listDiskConfig() {
        Pattern compile = Pattern.compile("^(scsi|ide|sata|virtio)\\d+");
        return map.entrySet().stream()
            .filter(it->compile.matcher(it.getKey()).find())
            .collect(Collectors.toMap(Map.Entry::getKey, it->parseLine(it.getValue())));
    }

    private final Logger logger = LoggerFactory.getLogger(VmConfig.class);

    public DiskInfo parseLine(String diskLine) {
        String[] split = diskLine.split(",");
        // 1. eintrag ist immer der speicherort


        String store = "";
        String filename = "";
        Map<String, String> properties = new LinkedHashMap<>();
        boolean firstEntry = true;
        for (String s : split) {
            logger.debug("{}", s);
            if (firstEntry) {
                firstEntry = false;

                if ("none".equals(s)) {
                    store = "none";
                    continue;
                }
                int idx = s.indexOf(':');
                if (idx == -1) {
                    logger.error("{}", s);
                    throw new IllegalStateException("unimplemented: ");
                }
                store = s.substring(0, idx);
                filename = s.substring(store.length() + 1);
                continue;
            }
            String key = s.substring(0, s.indexOf('='));
            String value = s.substring(key.length() + 1);
            properties.put(key, value);
        }


        String size = properties.get("size");
        long calcSize = 0;
        if (size != null) {
            String sizeEinheit = size.substring(size.length() - 1);
            Long i = Long.valueOf(size.substring(0, size.length() - 1));
            calcSize = switch (sizeEinheit) {
                case "G" -> i;
                case "M" -> i / 1024L;
                case "K" -> i / 1024L / 1024L;
                default -> throw new IllegalStateException("Unexpected value: " + sizeEinheit);
            };
        } else {

        }
        //TODO: iso-mounts?
        return new DiskInfo(store, filename, properties.get("format"), calcSize, "1".equals(properties.get("ssd")));
    }


    public record DiskInfo(String storageType, String filename, String format, long sizeG, boolean ssd) {}
}
