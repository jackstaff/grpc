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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jackstaff.grpc.MethodType;
import org.jackstaff.grpc.annotation.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    Set<TransFormInfo> dependencies(){
        return methods.stream().flatMap(method-> Stream.of(method.requestElement(), method.responseElement())).
                distinct().map(transForms::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private String simpleName(){
        String name =serviceName.contains(".") ? serviceName.substring(serviceName.lastIndexOf(".")+1) : serviceName;
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public String packageName(){
        return Utils.packageName(grpc);
    }

    public void build(){
        this.builder = TypeSpec.interfaceBuilder(simpleName()).
                addModifiers(Modifier.PUBLIC).addAnnotation(Protocol.class);
        for (MethodInfo method: methods){
            MethodSpec.Builder spec = MethodSpec.methodBuilder(method.getMethodName()).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            switch (method.getMethodType()){
                case UNARY:
                    spec.addAnnotation(AnnotationSpec.builder(RpcMethod.class).addMember("methodType", "$T.UNARY", MethodType.class).build());
                    spec.returns(method.response() ).addParameter(Utils.parameter(method.request()));
                    break;
                case SERVER_STREAMING:
                    spec.addAnnotation(AnnotationSpec.builder(RpcMethod.class).addMember("methodType", "$T.SERVER_STREAMING", MethodType.class).build());
                    spec.addParameter(Utils.parameter(method.request())).addParameter(Utils.responseParameter(method));
                    break;
                case CLIENT_STREAMING:
                    spec.addAnnotation(AnnotationSpec.builder(RpcMethod.class).addMember("methodType", "$T.CLIENT_STREAMING", MethodType.class).build());
                    spec.returns(Utils.consumerTypeName(method.request())).addParameter(Utils.responseParameter(method));
                    break;
                case BIDI_STREAMING:
                    spec.addAnnotation(AnnotationSpec.builder(RpcMethod.class).addMember("methodType", "$T.BIDI_STREAMING", MethodType.class).build());
                    spec.returns(Utils.consumerTypeName(method.request())).addParameter(Utils.responseParameter(method));
                    break;
            }
            builder.addMethod(spec.build());
            MethodSpec.Builder peer = MethodSpec.methodBuilder(method.getMethodName()).addModifiers(Modifier.PUBLIC, Modifier.DEFAULT);
            switch (method.getMethodType()) {
                case UNARY:
                    peer.addAnnotation(AnnotationSpec.builder(RpcMethod.class).addMember("methodType", "$T.ASYNCHRONOUS_UNARY", MethodType.class).build());
                    peer.addParameter(Utils.parameter(method.request())).addParameter(Utils.responseParameter(method)).
                            addStatement("throw new $T(\"Do NOT implement it.\")", RuntimeException.class);
                    break;
                case SERVER_STREAMING:
                    peer.addAnnotation(AnnotationSpec.builder(RpcMethod.class).addMember("methodType", "$T.BLOCKING_SERVER_STREAMING", MethodType.class).build());
                    peer.returns(Utils.listTypeName(method.response())).addParameter(Utils.parameter(method.request())).
                            addStatement("throw new $T(\"Do NOT implement it.\")", RuntimeException.class);
                    break;
                default:
                    continue;
            }
            builder.addMethod(peer.build());
        }
        grpc.getEnclosedElements().stream().filter(t->t.getKind()== ElementKind.METHOD).map(Element::getSimpleName).
                filter(name->"getServiceDescriptor".equals(name.toString())).findAny().ifPresent(name->{
            builder.addField(FieldSpec.builder(String.class, "SERVICE_NAME")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.addProtocol($N.class, $T.$N())",
                            transForms.findRegistry(Utils.packageName(grpc)), simpleName(), grpc.asType(), name)
                    .build());
        });
    }

    public void write(){
        Utils.write(this.processingEnv, packageName(), builder);
    }

}
