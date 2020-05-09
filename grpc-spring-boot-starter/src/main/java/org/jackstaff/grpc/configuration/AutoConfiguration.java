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
