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

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jackstaff.grpc.annotation.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * @author reco@jackstaff.org
 */
class ProtocolInfo {

    private final ProcessingEnvironment processingEnv;
    private final TransFormInfos transForms;
    private final String serviceName;
    private final TypeElement grpc;
    private final List<MethodInfo> methods;

    private TypeSpec.Builder builder;

    public ProtocolInfo(ProcessingEnvironment processingEnv, TransFormInfos transForms, String serviceName, TypeElement grpc, List<MethodInfo> methods) {
        this.processingEnv = processingEnv;
        this.transForms = transForms;
        this.serviceName = serviceName;
        this.grpc = grpc;
        this.methods = methods;
    }

    private String simpleName(){
        String name =serviceName.contains(".") ? serviceName.substring(serviceName.lastIndexOf(".")+1) : serviceName;
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public void build(){
        this.builder = TypeSpec.interfaceBuilder(simpleName()).
                addModifiers(Modifier.PUBLIC).addAnnotation(Protocol.class);
        for (MethodInfo method: methods){
            MethodSpec.Builder spec = MethodSpec.methodBuilder(method.getMethodName()).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            switch (method.getMethodType()){
                case UNARY:
//                    spec.addAnnotation(Unary.class);
                    spec.returns(method.response() ).addParameter(Utils.parameter(method.request()));
                    break;
                case SERVER_STREAMING:
                    spec.addAnnotation(ServerStreaming.class);
                    spec.addParameter(Utils.parameter(method.request())).addParameter(Utils.responseParameter(method));
                    break;
                case CLIENT_STREAMING:
                    spec.addAnnotation(ClientStreaming.class);
                    spec.returns(Utils.consumerTypeName(method.request())).addParameter(Utils.responseParameter(method));
                    break;
                case BIDI_STREAMING:
                    spec.addAnnotation(BidiStreaming.class);
                    spec.returns(Utils.consumerTypeName(method.request())).addParameter(Utils.responseParameter(method));
                    break;
            }
            builder.addMethod(spec.build());
            MethodSpec.Builder peer = MethodSpec.methodBuilder(method.getMethodName()).addModifiers(Modifier.PUBLIC, Modifier.DEFAULT);
            switch (method.getMethodType()) {
                case UNARY:
                    peer.addAnnotation(AsynchronousUnary.class);
                    peer.addComment("AsynchronousUnary for overload "+method.getMethodName()+". do NOT implement it.").
                            addParameter(Utils.parameter(method.request())).addParameter(Utils.responseParameter(method));
                    break;
                case SERVER_STREAMING:
                    peer.addAnnotation(BlockingServerStreaming.class);
                    peer.addComment("BlockingServerStreaming for overload "+method.getMethodName()+". do NOT implement it.").
                            returns(Utils.listTypeName(method.response())).addParameter(Utils.parameter(method.request())).addStatement("return null");
                    break;
                default:
                    continue;
            }
            builder.addMethod(peer.build());
        }
        builder.addField(FieldSpec.builder(String.class, "SERVICE_NAME")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.addProtocol($N.class, $T.getServiceDescriptor())",
                        transForms.findRegistry(Utils.packageName(grpc)), simpleName(), grpc.asType())
                .build());
    }

    public void write(){
        Utils.write(this.processingEnv, grpc, builder);
    }

}
