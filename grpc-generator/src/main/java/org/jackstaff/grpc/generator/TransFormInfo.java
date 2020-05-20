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
import org.jackstaff.grpc.TransFormRegistry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;

/**
 * @author reco@jackstaff.org
 */
class TransFormInfo {

    private final ProcessingEnvironment processingEnv;
    private final TransFormInfos transForms;
    private final TypeElement jackstaffParent;
    private final TypeElement protoElement;
    private final ProtoKind protoKind;

    private ClassName pojoTypeName;
    private TypeSpec.Builder builder;
    private TransFormInfo parent;
    private Set<TransFormInfo> children=new HashSet<>();
    private Map<String, Property> properties = new HashMap<>();
    private CodeBlock.Builder registryCodeBlock;

    public TransFormInfo(ProcessingEnvironment processingEnv, TransFormInfos transForms, TypeElement protoElement) {
        this.processingEnv = processingEnv;
        this.transForms = transForms;
        this.protoElement = protoElement;
        this.jackstaffParent = transForms.getJackstaffParent(protoElement);
        this.protoKind =  transForms.getProtoType(protoElement);
    }

    public TypeName pojoTypeName() {
        return Optional.ofNullable((TypeName)pojoTypeName).orElseGet(()-> Utils.typeName(protoElement));
    }

    public TypeElement getProtoElement() {
        return protoElement;
    }

    public ProtoKind getProtoKind() {
        return protoKind;
    }

    public TypeSpec getTypeSpec() {
        return Optional.ofNullable(builder).map(TypeSpec.Builder::build).orElse(null);
    }

    public boolean hasParent(){
        return parent != null;
    }

    public TransFormInfo getParent() {
        return parent;
    }

    public void setParent(TransFormInfo parent) {
        this.parent = parent;
        if (parent != null && (protoKind == ProtoKind.MESSAGE || protoKind == ProtoKind.ENUM || protoKind == ProtoKind.ENUM_CASE)){
            parent.children.add(this);
        }
    }

    public boolean isChild(TransFormInfo info){
        if (children.contains(info)){
            return true;
        }
        return children.stream().anyMatch(c->c.isChild(info));
    }

    public void prepare(){
        if (jackstaffParent == null){
            return;
        }
        children.forEach(TransFormInfo::prepare);
        switch (protoKind) {
            case MESSAGE:
                this.builder = TypeSpec.classBuilder(simpleName());
                TransFormInfo messageOrBuilder = transForms.findMessageOrBuilder(this);
                Map<String, ExecutorInfo> infos = messageOrBuilder.getProtoElement().getEnclosedElements().stream().
                        filter(e->e instanceof ExecutableElement).
                        map(ele->new ExecutorInfo((ExecutableElement) ele)).
                        collect(LinkedHashMap::new, (m, e)->m.put(e.getName(), e), LinkedHashMap::putAll);
                this.properties = Property.extract(transForms, this, infos);
                break;
            case ENUM:
            case ENUM_CASE:
                this.builder = buildEnum();
                break;
            default:
                return;
        }
        this.pojoTypeName = buildPojoTypeName();
        this.builder.addModifiers(Modifier.PUBLIC);
        if (parent != null){
            this.builder.addModifiers(Modifier.STATIC);
        }
    }

    private ClassName buildPojoTypeName(){
        StringBuilder name = new StringBuilder(simpleName());
        TransFormInfo p = this.parent;
        while (p != null){
            name.insert(0, p.simpleName() + ".");
            p = p.parent;
        }
        return ClassName.get(packageName(), name.toString());
    }

    private TypeSpec.Builder buildEnum(){
        TypeSpec.Builder builder = TypeSpec.enumBuilder(simpleName());
        Utils.enumNames(protoElement).forEach(builder::addEnumConstant);
        MethodSpec.Builder getNumber = MethodSpec.methodBuilder("getNumber").addModifiers(Modifier.PUBLIC).returns(Integer.TYPE);
        getNumber.addStatement("return $T.valueOf(this.name()).getNumber()", protoElement.asType());
        builder.addMethod(getNumber.build());
        MethodSpec.Builder forNumber = MethodSpec.methodBuilder("forNumber").
                addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                returns(simpleClassName()).addParameter(Integer.TYPE, "value");
        forNumber.addStatement("return valueOf($T.forNumber(value).name())", protoElement.asType());
        builder.addMethod(forNumber.build());
        this.registryCodeBlock = enumRegistryCode();
        return builder;
    }

    private CodeBlock.Builder enumRegistryCode(){
        CodeBlock.Builder block = CodeBlock.builder();
        TypeName typeName = selfClassName();
        TypeName protoTypeName = TypeName.get(getProtoElement().asType());
        //block.addStatement("//Registry mapping: $T -> $T", typeName, protoTypeName);
        block.addStatement("$T.register($T.class, $T.class)", TransFormRegistry.class, typeName, protoTypeName);
        return block;
    }

    private CodeBlock.Builder messageRegistryCode(){
        TransFormInfo messageOrBuilder = transForms.findMessageOrBuilder(this);
        TransFormInfo messageV3Builder = transForms.findMessageV3Builder(messageOrBuilder);
        return Property.registryCodeBlock(selfClassName(), TypeName.get(protoElement.asType()),
                TypeName.get(messageV3Builder.protoElement.asType()), properties);
    }

    Set<TransFormInfo> dependencies(){
        Set<TransFormInfo> set = new HashSet<>();
        properties.values().stream().map(Property::dependency).filter(Objects::nonNull).forEach(set::add);
        children.stream().map(TransFormInfo::dependencies).forEach(set::addAll);
        return set;
    }

    CodeBlock.Builder getRegistryCodeBlock() {
        return registryCodeBlock;
    }

    TypeSpec.Builder getBuilder() {
        return builder;
    }

    ClassName selfClassName(){
        if (parent != null){
            return buildPojoTypeName();
        }
        return ClassName.get("", simpleName());
    }

    ClassName simpleClassName(){
        return ClassName.get("", simpleName());
    }

    private String simpleName(){
        return protoElement.getSimpleName().toString();
    }

    String packageName(){
        return Utils.packageName(jackstaffParent);
    }

    Map<String, Property> getProperties() {
        return properties;
    }

    public void build(){
        if (builder ==null){
            return;
        }
        children.forEach(TransFormInfo::build);
        children.stream().map(TransFormInfo::getTypeSpec).filter(Objects::nonNull).forEach(builder::addType);
        Property.build(builder, transForms, properties);
        if (protoKind == ProtoKind.MESSAGE) {
            Property.addConstructor(builder, properties);
            this.registryCodeBlock = messageRegistryCode();
        }
    }

    public void write(){
        if (parent != null || builder == null){
            return;
        }
        Utils.write(processingEnv, jackstaffParent, builder);
    }

}
