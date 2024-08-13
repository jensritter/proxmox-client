package org.jens.proxmox;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Jens Ritter on 12.08.2024.
 */
public class VmConfig {
    private final Logger logger = LoggerFactory.getLogger(VmConfig.class);

    static final Set<String> DISKS = Set.of("scsi", "ide", "sata", "virtio", "efidisk");
    static final Pattern DISKPATTERN = Pattern.compile("^(" + String.join("|", DISKS) + ")\\d+");

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
        return map.entrySet().stream().filter(it->DISKPATTERN.matcher(it.getKey()).find()).collect(Collectors.toMap(Map.Entry::getKey, it->parseLine(it.getKey(), it.getValue())));
    }

    public DiskInfo parseLine(String diskid, String diskLine) {
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
        Long sizeInGB = parseSize(size);
        //TODO: iso-mounts?
        return new DiskInfo(diskid, store, filename, properties.get("format"), sizeInGB, "1".equals(properties.get("ssd")), "1".equals(properties.get("iothread")));
    }

    private static @Nullable Long parseSize(String size) {
        if (size != null) {
            String sizeEinheit = size.substring(size.length() - 1);
            long number = Long.parseLong(size.substring(0, size.length() - 1));
            return switch (sizeEinheit) {
                case "G" -> number;
                case "M" -> number / 1024L;
                case "K" -> number / 1024L / 1024L;
                default -> throw new IllegalStateException("Unexpected value: " + sizeEinheit);
            };
        } else {
            // unmounted cdroms, haben NULL size
            return null;
        }
    }

    public String getName() {return this.map.getOrDefault("name", "");}

    public int getCores() {return Integer.parseInt(this.map.getOrDefault("cores", "0"));}

    public int getSockets() {return Integer.parseInt(this.map.getOrDefault("sockets", "0"));}

    public boolean isAgent() {return "1".equals(map.get("agent"));}

    public boolean isOnBoot() {return "1".equals(map.get("onboot"));}

    public String getOsType() {return map.getOrDefault("ostype", "");}

    public Startup getStartUp() {
        var startup = map.getOrDefault("startup", "");
        throw new IllegalStateException("unimplemented: TODO: generic csv-parser für this and DiskInfo");
    }

    public Set<String> getTags() {
        var tags = map.getOrDefault("tags", "");
        return new LinkedHashSet<>(
            Arrays.asList(tags.split(";"))
        );
    }

    public record DiskInfo(
        /*
         * scsi0, ide0, or scsi1, etc ...
         */
        String diskid,
        /*
         * Name of storage
         */
        String storageName,
        /*
         * Filename in storage
         */
        String filename,
        /*
         * 'raw' or other internal fileformat
         */
        String format,
        /*
         * Size in GB
         */
        @Nullable Long sizeG,
        /*
         * Option 'ssd=1'
         */
        boolean ssd,
        /*
         * Option iothread=1
         */
        boolean iothread) {}

    public record Startup(int order, int up, int down) {}
}
