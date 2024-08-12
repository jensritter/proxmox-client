package org.jens.proxmox.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jens Ritter on 12.08.2024.
 */
@ConfigurationProperties("proxmox")
public class ProxmoxProperties {

    public ProxmoxProperties() {}

    public ProxmoxProperties(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    private String schema = "https";
    private String hostname;
    private int port = 8006;

    private String username;
    private String password;
    private String realm = "pve";

    public String getSchema() {return schema;}

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getHostname() {return hostname;}

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {return port;}

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {return username;}

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {return password;}

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealm() {return realm;}

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
