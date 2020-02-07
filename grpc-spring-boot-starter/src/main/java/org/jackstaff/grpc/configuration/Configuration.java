package org.jackstaff.grpc.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ConfigurationProperties(prefix = "spring.grpc")
public class Configuration {

    private ServerConfig server;
    private Map<String, ClientConfig> client;

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public Map<String, ClientConfig> getClient() {
        return client;
    }

    public void setClient(Map<String, ClientConfig> client) {
        this.client = client;
    }

    Set<String> authority(){
        if (client ==null){
            return new HashSet<>();
        }
        return client.keySet();
    }


}
