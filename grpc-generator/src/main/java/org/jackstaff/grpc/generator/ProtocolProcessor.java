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

import com.google.auto.service.AutoService;
import io.grpc.stub.annotations.RpcMethod;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  Generator Protocol interface,
 *  and POJO java bean when "option java_multiple_files = false" and "option java_outer_classname" end with "Proto" in .proto;
 *  (you can set compilerArgument with "-AJackstaffProto=${Proto}" at maven-compiler-plugin.configuration)
 * @author reco@jackstaff.org
 */
@AutoService(Processor.class)
public class ProtocolProcessor extends AbstractProcessor {

    private String getAnnotationValue(Element element, String name) {
        return element.getAnnotationMirrors().stream()
                .filter(m -> m.getAnnotationType().toString().equals(RpcMethod.class.getName()))
                .map(this.processingEnv.getElementUtils()::getElementValuesWithDefaults).
                        flatMap(v -> v.entrySet().stream()).filter(entry -> entry.getKey().getSimpleName().toString().equals(name)).
                        map(Map.Entry::getValue).map(AnnotationValue::getValue).map(String::valueOf).
                        findFirst().orElse("");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<ProtocolInfo> protocols= new ArrayList<>();
        List<MethodInfo> methods = new ArrayList<>();
        Map<String, TypeElement> grpcs = new LinkedHashMap<>();
        TransFormInfos transForms = new TransFormInfos(this.processingEnv);
        for (Element rpcMethod : roundEnv.getElementsAnnotatedWith(RpcMethod.class)) {
            String fullMethodName = getAnnotationValue(rpcMethod, "fullMethodName");
            String requestType = getAnnotationValue(rpcMethod, "requestType");
            String responseType = getAnnotationValue(rpcMethod, "responseType");
            String methodType = getAnnotationValue(rpcMethod, "methodType");
            TypeElement request = this.processingEnv.getElementUtils().getTypeElement(requestType);
            TypeElement response = this.processingEnv.getElementUtils().getTypeElement(responseType);
            MethodInfo method = new MethodInfo(transForms, fullMethodName, methodType, request, response);
            methods.add(method);
            grpcs.put(method.serviceName(), (TypeElement) rpcMethod.getEnclosingElement());
        }
        methods.stream().collect(Collectors.groupingBy(MethodInfo::serviceName)).forEach((name, methodInfos)->
                protocols.add(new ProtocolInfo(this.processingEnv, transForms, name, grpcs.get(name), methodInfos)));
        methods.stream().flatMap(method-> Stream.of(method.requestElement(), method.responseElement())).
                collect(Collectors.toSet()).forEach(t->transForms.put(t, new TransFormInfo(this.processingEnv, transForms, t)));
        transForms.build();
        transForms.write();
        protocols.forEach(ProtocolInfo::build);
        protocols.forEach(ProtocolInfo::write);
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(RpcMethod.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
