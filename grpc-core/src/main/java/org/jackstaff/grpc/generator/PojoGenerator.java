package org.jackstaff.grpc.generator;

import io.grpc.ServiceDescriptor;

import java.util.List;

/**
 * Generator Pre Search
 * generate POJO source file if Proto "java_multiple_files" is false;
 * @author reco@jackstaff.org
 */
class PojoGenerator {

    private List<ServiceDescriptor> serviceDescriptors;

    public PojoGenerator(List<ServiceDescriptor> serviceDescriptors) {
        this.serviceDescriptors = serviceDescriptors;
    }

    public void generate(){

    }

    public void save(String dir) {

    }

    SourceInfo getPojo(Class<?> proto){
        return new SourceInfo(proto);
    }

}
