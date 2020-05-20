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

package org.jackstaff.grpc.generator;

import com.squareup.javapoet.*;
import io.grpc.ServiceDescriptor;
import org.jackstaff.grpc.TransFormRegistry;
import org.jackstaff.grpc.internal.PropertyKind;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author reco@jackstaff.org
 */
class RegistryMapping {

    static final String SIMPLE_CLASS_NAME ="RegistryMapping";

    private final ProcessingEnvironment processingEnv;
    private final List<ProtocolInfo> protocols;
    private final Map<String, RegistryMapping> mappings;
    private final String packageName;
    private final String simpleName;
    private TypeSpec.Builder builder;

    public RegistryMapping(ProcessingEnvironment processingEnv, List<ProtocolInfo> protocols, Map<String, RegistryMapping> mappings, String packageName, String simpleName) {
        this.processingEnv = processingEnv;
        this.protocols = protocols;
        this.mappings = mappings;
        this.packageName = packageName;
        this.simpleName = simpleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public ClassName selfClassName(){
        return ClassName.get(packageName, simpleName);
    }

    public boolean hasBuilder() {
        return builder != null;
    }

    public void write(){
        if (builder != null) {
            Utils.write(processingEnv, packageName, builder);
        }
    }

    private void addDependencies(List<TransFormInfo> infos){
        //protocol dependencies and proto message dependencies.
        Set<TransFormInfo> set = new HashSet<>(infos);
        protocols.stream().filter(p->p.packageName().equals(this.packageName)).
                map(ProtocolInfo::dependencies).flatMap(Collection::stream).forEach(set::add);
        Set<String> packs = set.stream().map(TransFormInfo::packageName).collect(Collectors.toSet());
        set.stream().flatMap(info -> info.dependencies().stream()).map(TransFormInfo::packageName).forEach(packs::add);
        List<String> names = packs.stream().filter(pack->!pack.equals(this.packageName)).
                map(mappings::get).filter(Objects::nonNull).map(r->r.selfClassName().canonicalName()).
                collect(Collectors.toList());
        if (!names.isEmpty()){
            CodeBlock.Builder block = CodeBlock.builder();
            names.forEach(name->block.addStatement("$T.dependency($S)", TransFormRegistry.class, name));
            builder.addStaticBlock(block.build());
        }
    }

    public void build(List<TransFormInfo> infos){
        builder = TypeSpec.classBuilder(selfClassName());
        MethodSpec.Builder spec = MethodSpec.methodBuilder("addProtocol").
                addModifiers(Modifier.STATIC).returns(String.class).
                addParameter(ParameterizedTypeName.get(Class.class), "protocol").
                addParameter(ServiceDescriptor.class,"descriptor").
                addStatement("return $T.addProtocol(protocol, descriptor)", TransFormRegistry.class);
        builder.addMethod(spec.build());
        this.addDependencies(infos);
        infos.stream().sorted((info1, info2)->{
            if (info1.getProtoKind() == info2.getProtoKind() && info1.getProtoKind() == ProtoKind.MESSAGE){
                if (info1.isChild(info2)){
                    return 1;
                }
                if (info2.isChild(info1)){
                    return -1;
                }
                List<Property> props1 = info1.getProperties().values().stream().
                        filter(p->p.getKind()== PropertyKind.MESSAGE || p.getKind() == PropertyKind.MESSAGE_LIST).
                        collect(Collectors.toList());
                List<Property> props2 = info2.getProperties().values().stream().
                        filter(p->p.getKind()== PropertyKind.MESSAGE || p.getKind() == PropertyKind.MESSAGE_LIST).
                        collect(Collectors.toList());
                if (props1.isEmpty() && props2.isEmpty()){
                    return info1.getProperties().size() - info2.getProperties().size();
                }
                Set<TransFormInfo> deps1= props1.stream().map(Property::dependency).filter(Objects::nonNull).collect(Collectors.toSet());
                Set<TransFormInfo> deps2= props2.stream().map(Property::dependency).filter(Objects::nonNull).collect(Collectors.toSet());
                if (deps1.containsAll(deps2)){
                    return 1;
                }
                if (deps2.containsAll(deps1)){
                    return -1;
                }
                long count1= deps1.stream().filter(deps2::contains).count();
                long count2= deps2.stream().filter(deps1::contains).count();
                if (count1>0 || count2>0){
                    return (int)(count2-count1);
                }
                if (props1.size() == props2.size()) {
                    return info1.selfClassName().toString().length() - info2.selfClassName().toString().length();
                }
                return props1.size() - props2.size();
            }
            return info1.getProtoKind().ordinal() - info2.getProtoKind().ordinal();
        }).map(TransFormInfo::getRegistryCodeBlock).filter(Objects::nonNull).
                forEach(block -> builder.addStaticBlock(block.build()));
    }

}
