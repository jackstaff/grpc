package org.jackstaff.grpc.generator;

import io.grpc.ServiceDescriptor;

/**
 * Generator Pre Search
 * generate protocol interface base on grpc-java generated files
 * @author reco@jackstaff.org
 */
class ProtocolGenerator {

    private ServiceDescriptor serviceDescriptor;
    private PojoGenerator pojoGenerator;

    public ProtocolGenerator(ServiceDescriptor serviceDescriptor, PojoGenerator pojoGenerator) {
        this.serviceDescriptor = serviceDescriptor;
        this.pojoGenerator = pojoGenerator;
    }

    public void generate(){

    }

    public void save(String dir) {

    }

}
