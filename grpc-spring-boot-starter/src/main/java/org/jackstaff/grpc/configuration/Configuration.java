/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
