package org.jens.proxmox;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jens.proxmox.config.properties.ProxmoxProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

/**
 * Proxmox-Client
 * <p>
 * Verwendet org.springframework.web.client.RestClient für Abfragen via Https
 *
 * @author Jens Ritter on 03.07.2024.
 */
public class ProxmoxClient {
    private final Logger logger = LoggerFactory.getLogger(ProxmoxClient.class);


    private final ProxmoxProperties proxmoxProperties;
    private final RestClient.Builder restClientBuilder;


    public ProxmoxClient(String hostname, int port) {this(new ProxmoxProperties(hostname, port));}

    public ProxmoxClient(@NotNull ProxmoxProperties properties) {this(properties, null);}

    public ProxmoxClient(@NotNull ProxmoxProperties properties, @Nullable RestClient.Builder restclientBuilder) {
        this.proxmoxProperties = properties;
        restClientBuilder = Optional.ofNullable(restclientBuilder)
            .orElse(RestClient.builder())
            .baseUrl("https://" + proxmoxProperties.getHostname() + ":" + proxmoxProperties.getPort() + "/api2/json")
        ;
    }

    public ProxmoxSession login() {
        if (proxmoxProperties.getUsername() == null || proxmoxProperties.getPassword() == null || proxmoxProperties.getRealm() == null) {
            throw new NullPointerException("Missing credentials.");
        }
        return login(proxmoxProperties.getUsername(), proxmoxProperties.getPassword(), proxmoxProperties.getRealm());
    }

    private static final ParameterizedTypeReference<GenericData<AccessTicketResponse>> ACCESSTICKERRESPONSETYPE = new ParameterizedTypeReference<>() {};

    public ProxmoxSession login(@NotNull String username, @NotNull String password, @NotNull String realm) {

        RestClient loginClient = this.restClientBuilder.build();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", username + "@" + realm);
        params.add("password", password);

        var response = loginClient.post()
            .uri("/access/ticket")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(params)
            .retrieve()
            .body(ACCESSTICKERRESPONSETYPE);
        if (response == null || response.data() == null) {
            throw new RestClientException("Failure during Login. No Useable response from Server");
        }

        logger.trace("Login-Response: {}", response.data());
        AccessTicketResponse ticket = response.data();

        // create Restclient with Auth-Headers
        var use = restClientBuilder
            .defaultHeader("CSRFPreventionToken", ticket.csrfToken())
            .defaultHeader("Cookie", "PVEAuthCookie=" + ticket.ticket())
            .build();
        return new ProxmoxSession(use, ticket.clustername());
    }


    record GenericData<K>(K data) {}


    private record AccessTicketResponse(String ticket, @JsonProperty("CSRFPreventionToken") String csrfToken, String username, String clustername, Map<String, ?> cap) {}

}
