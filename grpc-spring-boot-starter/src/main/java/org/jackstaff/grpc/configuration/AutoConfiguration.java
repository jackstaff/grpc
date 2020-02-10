package org.jackstaff.grpc.configuration;

import org.jackstaff.grpc.Client;
import org.jackstaff.grpc.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot Auto Configuration for Jackstaff RPC Framework
 * @author reco@jackstaff.org
 * @see SpringConfiguration
 * @see Configuration
 */

@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(Configuration.class)
public class AutoConfiguration {

    private final SpringConfiguration springConfiguration;

    public AutoConfiguration(ApplicationContext appContext, Configuration configuration) {
        this.springConfiguration = new SpringConfiguration(appContext, configuration);
    }

    @Bean
    @ConditionalOnMissingBean(Server.class)
    public Server packetServer() throws Exception {
        return springConfiguration.newServer();
    }

    @Bean
    @ConditionalOnMissingBean(Client.class)
    public Client packetClient() throws Exception {
        return springConfiguration.newClient();
    }

}
