package org.jackstaff.grpc.generator;

import io.grpc.ServiceDescriptor;

import java.util.ArrayList;
import java.util.List;
/**
 * Generator Pre Search
 * @author reco@jackstaff.org
 */
public class Generator {

    private String dir;
    private List<ServiceDescriptor> serviceDescriptors;

    public Generator(String dir, List<ServiceDescriptor> serviceDescriptors) {
        this.dir = dir;
        this.serviceDescriptors = serviceDescriptors;
    }

    public void generate(){
        PojoGenerator pojoGenerator = new PojoGenerator(serviceDescriptors);
        pojoGenerator.generate();
        List<ProtocolGenerator> generators = new ArrayList<>();
        for (ServiceDescriptor descriptor: serviceDescriptors) {
            ProtocolGenerator generator = new ProtocolGenerator(descriptor, pojoGenerator);
            generator.generate();
            generators.add(generator);
        }
        pojoGenerator.save(dir);
        generators.forEach(g->g.save(dir));
    }


    public static void main(String[] args) {

    }

}
