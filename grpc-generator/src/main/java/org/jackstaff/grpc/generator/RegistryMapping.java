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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.grpc.ServiceDescriptor;
import org.jackstaff.grpc.TransFormRegistry;
import org.jackstaff.grpc.internal.PropertyKind;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author reco@jackstaff.org
 */
class RegistryMapping {

    static final String SIMPLE_CLASS_NAME ="RegistryMapping";

    private final ProcessingEnvironment processingEnv;
    private final String packageName;
    private final String simpleName;
    private TypeSpec.Builder builder;

    public RegistryMapping(ProcessingEnvironment processingEnv, String packageName, String simpleName) {
        this.processingEnv = processingEnv;
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

    public void build(List<TransFormInfo> infos){
        builder = TypeSpec.classBuilder(selfClassName());
        MethodSpec.Builder spec = MethodSpec.methodBuilder("addProtocol").
                addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(String.class).
                addParameter(ParameterizedTypeName.get(Class.class), "protocol").
                addParameter(ServiceDescriptor.class,"descriptor").
                addStatement("return $T.addProtocol(protocol, descriptor)", TransFormRegistry.class);
        builder.addMethod(spec.build());
        infos.stream().sorted((info1, info2)->{
            if (info1.getProtoKind() == info2.getProtoKind() && info1.getProtoKind() == ProtoKind.MESSAGE){
                List<Property> props1 = info1.getProperties().values().stream().
                        filter(p->p.getKind()== PropertyKind.MESSAGE || p.getKind() == PropertyKind.MESSAGE_LIST).
                        collect(Collectors.toList());
                List<Property> props2 = info2.getProperties().values().stream().
                        filter(p->p.getKind()== PropertyKind.MESSAGE || p.getKind() == PropertyKind.MESSAGE_LIST).
                        collect(Collectors.toList());
                if (props1.isEmpty() && props2.isEmpty()){
                    return info1.getProperties().size() - info2.getProperties().size();
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
