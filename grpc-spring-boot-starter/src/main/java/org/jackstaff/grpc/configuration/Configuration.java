package org.jackstaff.grpc.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * spring config file properties
 * spring:
 *   grpc:
 *     server:
 *       port: 9000
 * #      keep-alive-time: 20
 * #      keep-alive-timeout: 10
 * #      permit-keep-alive-time: 300
 * #      permit-keep-alive-without-calls: true
 * #      max-inbound-message-size: 4194304
 * #      max-inbound-metadata-size: 20480
 * #      max-connection-idle: 0
 * #      max-connection-age: 0
 * #      max-connection-age-grace: 0
 * #      kye-cert-chain: /server.crt
 * #      private-key: /pkcs8_key.pem
 *     client:
 *       demo-server:
 *         host: localhost
 *         port: 9000
 * #        keep-alive-time: 20
 * #        keep-alive-timeout: 10
 * #        keep-alive-without-calls: true
 * #        max-inbound-message-size: 4194304
 * #        max-retry-attempts: 0
 * #        idle-timeout: 1800
 * #        plaintext: true
 *
 * @author reco@jackstaff.org
 * @see ServerConfig
 * @see ClientConfig
 */
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

}
