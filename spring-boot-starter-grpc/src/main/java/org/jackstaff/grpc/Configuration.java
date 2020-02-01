package org.jackstaff.grpc;

import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
/**
 * @author reco@jackstaff.org
 */

@org.springframework.context.annotation.Configuration
class Configuration {

    static class Client {

        private String host;
        private int port;

    }

    @Value("${spring.grpc.port:9000}")
    private int port;

    @Value("${spring.grpc.client:}")
    private Map<String, Client> client;


}
