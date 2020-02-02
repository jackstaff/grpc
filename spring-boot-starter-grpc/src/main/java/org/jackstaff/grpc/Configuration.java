package org.jackstaff.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "spring.grpc")
public class Configuration {

    public static class Server {

        private int port;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class Client {

        private String host;
        private int port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    private Server server;
    private Map<String, Client> client;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Map<String, Client> getClient() {
        return client;
    }

    public void setClient(Map<String, Client> client) {
        this.client = client;
    }

    Set<String> authority(){
        if (client ==null){
            return new HashSet<>();
        }
        return client.keySet();
    }


}
