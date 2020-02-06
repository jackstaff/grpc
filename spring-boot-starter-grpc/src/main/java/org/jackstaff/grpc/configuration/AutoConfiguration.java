package org.jackstaff.grpc.configuration;

import org.jackstaff.grpc.PacketClient;
import org.jackstaff.grpc.PacketServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author reco@jackstaff.org
 */

@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(Configuration.class)
public class AutoConfiguration {

    private final SpringConfiguration springConfiguration;

    public AutoConfiguration(ApplicationContext appContext, Configuration configuration) {
        this.springConfiguration = new SpringConfiguration(appContext, configuration);
    }

    @Bean
    @ConditionalOnMissingBean(PacketServer.class)
    public PacketServer packetServer() throws Exception {
        return springConfiguration.newPacketServer();
    }

    @Bean
    @ConditionalOnMissingBean(PacketClient.class)
    public PacketClient packetClient() throws Exception {
        return springConfiguration.newPacketClient();
    }

}
