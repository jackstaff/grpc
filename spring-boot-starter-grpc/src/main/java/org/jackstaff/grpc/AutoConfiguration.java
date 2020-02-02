package org.jackstaff.grpc;

import com.google.gson.internal.$Gson$Preconditions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author reco@jackstaff.org
 */

@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(Configuration.class)
public class AutoConfiguration {

    private final ApplicationContext appContext;

    private final Configuration configuration;

    public AutoConfiguration(ApplicationContext appContext, Configuration configuration) {
        this.appContext = appContext;
        this.configuration = configuration;
    }

    @Bean
    @ConditionalOnMissingBean(PacketServer.class)
    public PacketServer packetServer() throws Exception {
        PacketServer server= new PacketServer(appContext, configuration);
        server.initial();
        return server;
    }

    @Bean
    @ConditionalOnMissingBean(PacketClient.class)
    public PacketClient packetClient() throws Exception {
        PacketClient client = new PacketClient(appContext, configuration);
        client.initial();
        return client;
    }

    //@Import({AutoConfiguration.ScannerRegistrar.class})
    static class ScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

        private BeanFactory factory;

        private ResourceLoader resourceLoader;

        @Override
        public void setBeanFactory(BeanFactory factory) throws BeansException {
            this.factory = factory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, true);
            scanner.setResourceLoader(this.resourceLoader);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Server.class));
            Set<BeanDefinition> beanDefinitions = scanPackages(metadata, scanner);
            System.out.println(beanDefinitions);
            //ProxyUtil.registerBeans(beanFactory, beanDefinitions);
        }

        private Set<BeanDefinition> scanPackages(AnnotationMetadata metadata, ClassPathBeanDefinitionScanner scanner) {
            List<String> packages = new ArrayList<>();
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ComponentScan.class.getCanonicalName());
            if (annotationAttributes != null) {
                String[] basePackages = (String[]) annotationAttributes.get("value");
                if (basePackages.length > 0) {
                    packages.addAll(Arrays.asList(basePackages));
                }
            }
            Set<BeanDefinition> beanDefinitions = new HashSet<>();
            if (CollectionUtils.isEmpty(packages)) {
                return beanDefinitions;
            }
            packages.forEach(pack -> beanDefinitions.addAll(scanner.findCandidateComponents(pack)));
            return beanDefinitions;
        }

    }


}
