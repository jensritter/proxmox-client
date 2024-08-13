package org.jens.proxmox;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Jens Ritter on 12.08.2024.
 */
public final class ProxmoxSession {
    private final Logger logger = LoggerFactory.getLogger(ProxmoxSession.class);

    private final RestClient restClient;
    private final String clustername;


    ProxmoxSession(RestClient use, String clustername) {
        this.restClient = use;
        this.clustername = clustername;
    }

    public String getClustername() {return clustername;}


    private record GenericList<K>(@NotNull List<K> data) {}

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

    private <K> K getSingle(String url, ParameterizedTypeReference<ProxmoxClient.GenericData<K>> reponseClasss) {
        logger.info("{}", url);
        var response = restClient.get()
            .uri(url)
            .retrieve()
            .body(reponseClasss);

        if (response == null || response.data() == null) {
            throw new RestClientException("No Useable Response from Server");
        }
        return response.data();
    }


    public record Version(String release, String version, String repoid) {}

    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    public Version queryVersion() {return getSingle("/version", new ParameterizedTypeReference<>() {});}


    public record Node(String node, String id, String level, String status, String type) {}

    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    public List<Node> queryNodes() {return getList("/nodes", new ParameterizedTypeReference<>() {});}


    public record VmStatus(String node, int vmid, String status, long maxdisk, long maxmem) {}

    private record TmpVmStatus(int vmid, String status, long maxdisk, long maxmem) {}

    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    public List<VmStatus> queryVms(String node) {
        List<TmpVmStatus> tmpList = getList("/nodes/" + node + "/qemu", new ParameterizedTypeReference<>() {});
        return tmpList.stream()
            .map(it->new VmStatus(node, it.vmid(), it.status(), it.maxdisk(), it.maxmem()))
            .toList();
    }


    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    public VmConfig queryConfig(String node, int vmid) {
        String url = "/nodes/" + node + "/qemu/" + vmid + "/config";
        logger.info("{}", url);
        ParameterizedTypeReference<ProxmoxClient.GenericData<HashMap<String, String>>> type = new ParameterizedTypeReference<>() {};
        var response = restClient.get()
            .uri(url)
            .retrieve()
            .body(type);
        if (response == null || response.data() == null) {
            throw new RestClientException("No Useable Response from Server");
        }
        return new VmConfig(node, vmid, response.data());
    }

}
