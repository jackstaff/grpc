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

import com.squareup.javapoet.TypeName;
import io.grpc.MethodDescriptor;

import javax.lang.model.element.TypeElement;
import java.util.Optional;

/**
 * @author reco@jackstaff.org
 */
class MethodInfo {

    private final TransFormInfos transForms;
    private final String serviceName;
    private final String methodName;
    private final TypeElement requestElement;
    private final TypeElement responseElement;
    private final MethodDescriptor.MethodType methodType;

    public MethodInfo(TransFormInfos transForms, String fullMethodName,
                      String methodType, TypeElement requestElement, TypeElement responseElement) {
        this.transForms = transForms;
        this.methodType = MethodDescriptor.MethodType.valueOf(methodType);
        this.requestElement = requestElement;
        this.responseElement = responseElement;
        this.serviceName = Optional.ofNullable(MethodDescriptor.extractFullServiceName(fullMethodName)).orElse("");
        this.methodName = Utils.lowerCamelName(fullMethodName.substring(this.serviceName.length()+1));
    }

    public String serviceName(){
        return serviceName;
    }

    public TypeElement requestElement() {
        return requestElement;
    }

    public TypeElement responseElement() {
        return responseElement;
    }

    public TypeName request() {
        return transForms.get(requestElement).pojoTypeName();
    }

    public TypeName response() {
        return transForms.get(responseElement).pojoTypeName();
    }

    public MethodDescriptor.MethodType getMethodType() {
        return methodType;
    }

    public String getMethodName() {
        return methodName;
    }

}
