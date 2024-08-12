package de.mitegro.netbox.proxmox;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Jens Ritter on 03.07.2024.
 */
public class ProxmoxClient {
    private final String hostname;
    private final int port;


    public ProxmoxClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public MyClientSession login(String username, String password, String realm) {
        RestClient client = RestClient.builder()
            .baseUrl("https://" + hostname + ":" + port + "/api2/json")
            .build();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", username + "@" + realm);
        params.add("password", password);

        /*
                        put("password", password);
                put("username", username);
                put("realm", realm);
                put("otp", otp);
         */
        ParameterizedTypeReference<GenericData<AccessTicketResponse>> type = new ParameterizedTypeReference<>() {};
        AccessTicketResponse response = client.post()
            .uri("/access/ticket")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(params)
            .retrieve()
            .body(type)
            .data();

//        httpCon.setRequestProperty("CSRFPreventionToken", _ticketCSRFPreventionToken);
//        httpCon.setRequestProperty("Cookie", "PVEAuthCookie=" + _ticketPVEAuthCookie);

        var use = RestClient.builder()
            .baseUrl("https://" + hostname + ":" + port + "/api2/json")
            .defaultHeader("CSRFPreventionToken", response.csrfToken())
            .defaultHeader("Cookie", "PVEAuthCookie=" + response.ticket())
            .build();


        logger.info("{}", response);

        //{"data":{"username":"java@pve","ticket":"PVE:java@pve:668585F9::T5NDaYIAcRs0LVpcQGPIe7yj6xJVEpMCFXa751x+Oi1R6IUngnSHCMQ/iBWlBjhQGHewQliouAVA2nvrClwfqvxtYz4UM+oqlCo2B3rQhELUuDUM+HOb9dONhdk0q0aopcCE2qMQHFjXm/tVHn3P3ak+8J3g0fROZ3VPKLuFa7tyXhiu9V22iweJg1f+MvY09L6YIH5u1pmjD3DlaKIUoonb5CoGSMqX6+VtAM5jA6LFzt76oGwLHC/DlCYxHa3ADeKYFVV8j5lK5GtnIjaBojlcLIdBdBr2JqDaK5qHYNyphwSh2RUvxaFgWz8kS71hh/Hy5K6MZL4Xd1bV0+2k9A==","CSRFPreventionToken":"668585F9:pcYKrocX3i2EJFP1jlOEfTSUlKUWpwO2U2rI3CRhto4","cap":{"nodes":{},"access":{},"dc":{},"mapping":{},"storage":{},"sdn":{},"vms":{"VM.Monitor":1,"VM.Audit":1,"VM.Config.Options":1,"VM.Config.Disk":1,"VM.Allocate":1,"VM.Config.CPU":1,"VM.Console":1,"VM.PowerMgmt":1,"VM.Config.Cloudinit":1,"VM.Config.Memory":1,"VM.Config.CDROM":1,"VM.Snapshot":1,"VM.Clone":1,"VM.Config.HWType":1,"VM.Backup":1,"VM.Config.Network":1,"VM.Migrate":1,"VM.Snapshot.Rollback":1}}}}

        return new MyClientSession(use);
    }


    public record GenericData<K>(K data) {}

    public record GenericList<K>(List<K> data) {}

    private record AccessTicketResponse(String ticket, @JsonProperty("CSRFPreventionToken") String csrfToken, String username) {}

    private final Logger logger = LoggerFactory.getLogger(ProxmoxClient.class);


    public static final class MyClientSession implements AutoCloseable {
        private final RestClient restClient;

        private MyClientSession(RestClient use) {this.restClient = use;}

        @Override
        public void close() {}

        private final Logger logger = LoggerFactory.getLogger(MyClientSession.class);

        private <K> List<K> getList(String url, ParameterizedTypeReference<GenericList<K>> reponseClass) {
            logger.info("{}", url);
            var data = restClient.get()
                .uri(url)
                .retrieve()
                .body(reponseClass);
            if (data != null) {
                return data.data();
            } else {
                logger.warn("No GenericList-Response.");
                return Collections.emptyList();
            }
        }

        @Nullable
        private <K> K getSimple(String url, Class<K> reponseClass) {
            logger.info("{}", url);
            return restClient.get()
                .uri(url)
                .retrieve()
                .body(reponseClass)
                ;
        }


        public record Version(String release, String version, String repoid) {}

        public Version version() throws IOException {
            Version body = getSimple("/version", Version.class);
            if (body == null) {
                throw new IOException("Cannot read VERSION from Proxmox");
            }
            return body;
        }

        public record Node(String node, String id, String level, String status, String type) {}

        public List<Node> nodes() {
            return getList("/nodes", new ParameterizedTypeReference<>() {});
        }

        private record VmStatusPriv(int vmid, String status, long maxdisk, long maxmem) {}

        public record VmStatus(String node, int vmid, String status, long maxdisk, long maxmem) {}

        public List<VmStatus> vms(String node) {
            List<VmStatusPriv> list = getList("/nodes/" + node + "/qemu", new ParameterizedTypeReference<>() {});
            return list.stream()
                .map(it->new VmStatus(node, it.vmid(), it.status(), it.maxdisk(), it.maxmem()))
                .toList();
        }


        public Config config(String node, int vmid) {
            String url = "/nodes/" + node + "/qemu/" + vmid + "/config";
            logger.info("{}", url);
            ParameterizedTypeReference<GenericData<HashMap<String, String>>> type = new ParameterizedTypeReference<>() {};
            return new Config(node, vmid, restClient.get()
                .uri(url)
                .retrieve()
                .body(type)
                .data());
        }

        public static class Config {
            private final String node;
            private final int vmid;
            private final Map<String, String> map;

            Config(String node, int vmid, Map<String, String> map) {
                this.node = node;
                this.vmid = vmid;
                this.map = map;
            }

            public String getNode() {return node;}

            public int getVmid() {return vmid;}

            public Set<String> keySet() {return this.map.keySet();}

            public String get(String key) {return this.map.get(key);}

            public Map<String, DiskInfo> listDiskConfig() {
                Pattern compile = Pattern.compile("^(scsi|ide|sata|virtio)\\d+");
                return map.entrySet().stream()
                    .filter(it->compile.matcher(it.getKey()).find())
                    .collect(Collectors.toMap(it->it.getKey(), it->parseLine(it.getValue())));
            }

            private final Logger logger = LoggerFactory.getLogger(Config.class);

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


        }

        public record DiskInfo(String storageType, String filename, String format, long sizeG, boolean ssd) {}


    }
}
