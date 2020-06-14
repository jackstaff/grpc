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

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.ProtocolMessageEnum;
import com.squareup.javapoet.ClassName;
import org.jackstaff.grpc.TransFormRegistry;
import org.jackstaff.grpc.internal.PropertyKind;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class TransFormInfos {

    private final ProcessingEnvironment processingEnv;
    private final Elements elements;
    private final Types types;
    private final Messager messager;
    private final TypeElement messageV3;
    private final TypeElement messageV3Builder;
    private final TypeElement messageOrBuilder;
    private final TypeElement messageEnum;
    private final TypeElement byteString;

    private final Map<TypeElement, TransFormInfo> infos = new ConcurrentHashMap<>();
    private final Map<TypeElement, String> parents = new ConcurrentHashMap<>();
    private final Map<String, RegistryMapping> mappings=new ConcurrentHashMap<>();
    private final List<ProtocolInfo> protocols;

    public TransFormInfos(ProcessingEnvironment processingEnv, List<ProtocolInfo> protocols) {
        this.protocols = protocols;
        this.processingEnv = processingEnv;
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
        this.messager = processingEnv.getMessager();
        this.byteString = elements.getTypeElement(ByteString.class.getCanonicalName());
        this.messageEnum = elements.getTypeElement(ProtocolMessageEnum.class.getCanonicalName());
        this.messageOrBuilder = elements.getTypeElement(MessageOrBuilder.class.getCanonicalName());
        this.messageV3 = elements.getTypeElement(GeneratedMessageV3.class.getCanonicalName());
        this.messageV3Builder = messageV3.getEnclosedElements().stream().
                filter(t->t.getKind() == ElementKind.CLASS).
                filter(t->t.getSimpleName().toString().equals("Builder")).
                filter(t->t instanceof TypeElement).map(t->(TypeElement)t).
                findFirst().orElse(null);
    }

    public TransFormInfo get(TypeElement ele){
        return infos.get(ele);
    }

    public TransFormInfo get(TypeMirror typeMirror){
        TypeElement ele = (TypeElement) types.asElement(typeMirror);
        return Optional.ofNullable(this.get(ele)).orElseGet(()->{
            TransFormInfo info= new TransFormInfo(processingEnv, this, ele);
            this.infos.put(ele, info);
            return info;
        });
    }

    public void put(TypeElement ele, TransFormInfo info){
        infos.put(ele, info);
    }

    private boolean isJackstaffProto(Element element){
        String proto = processingEnv.getOptions().getOrDefault("JackstaffProto", "Proto");
        for (String s: proto.split(",")){
            if ("*".equals(s) || element.toString().endsWith(s)){
                return true;
            }
        }
        return false;
    }

    private TypeElement jackstaffProto(String name) {
        return Optional.ofNullable(name).filter(s->s.length()>0).map(elements::getTypeElement).orElse(null);
    }

    private String mappingClassName(String packageName) {
        String name = RegistryMapping.SIMPLE_CLASS_NAME;
        if (elements.getTypeElement(name) ==null){
            return name;
        }
        name = "Jackstaff"+RegistryMapping.SIMPLE_CLASS_NAME;
        while (elements.getTypeElement(packageName+"."+name) !=null){
            name = name + "0";
        }
        return name;
    }

    private TypeElement findJackstaffProto(TypeElement ele) {
        List<TypeElement> list = new ArrayList<>();
        Element parent = ele.getEnclosingElement();
        while (parent.getKind() == ElementKind.CLASS) {
            list.add((TypeElement) parent);
            parent = parent.getEnclosingElement();
        }
        Collections.reverse(list);
        return list.stream().filter(this::isJackstaffProto).findFirst().orElse(null);
    }

    public TypeElement getJackstaffParent(TypeElement protoElement) {
        String name = parents.get(protoElement);
        if (name == null){
            TypeElement ele = findJackstaffProto(protoElement);
            parents.put(ele, Optional.ofNullable(ele).map(Object::toString).orElse(""));
            return ele;
        }
        return jackstaffProto(name);
    }

    public boolean prepare(){
        int size = infos.size();
        List<TypeElement> protos = parents.values().stream().map(this::jackstaffProto).
                filter(Objects::nonNull).distinct().collect(Collectors.toList());
        protos.forEach(p->p.getEnclosedElements().stream().filter(e->e instanceof TypeElement).
                forEach(t->prepare(null, (TypeElement)t)));
        List<TransFormInfo> roots = infos.values().stream().filter(t->t.getParent() ==null).collect(Collectors.toList());
        roots.forEach(TransFormInfo::prepare);
        roots.forEach(TransFormInfo::build);
        return infos.size() > size;
    }

    public void build(){
        boolean next = prepare();
        while (next){
            next = prepare();
        }
        List<TypeElement> protos = parents.values().stream().map(this::jackstaffProto).
                filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<String, List<TransFormInfo>> packs= infos.values().stream().filter(info->info.getBuilder() != null).
                collect(Collectors.groupingBy(TransFormInfo::packageName));
        if (!packs.isEmpty()) {
            protos.stream().map(Utils::packageName).distinct().forEach(packName->
                    mappings.put(packName, new RegistryMapping(processingEnv, protocols, mappings, packName, mappingClassName(packName))));
            packs.forEach((pack, list)->this.mappings.get(pack).build(list));
        }
    }

    private void prepare(TransFormInfo parent, TypeElement element) {
        if (infos.containsKey(element) && infos.get(element).hasParent()){ //for loop references, never
            //warning("loop references?");
            return;
        }
        TransFormInfo info = infos.computeIfAbsent(element, ele->new TransFormInfo(processingEnv, this, ele));
        info.setParent(parent);
        element.getEnclosedElements().stream().filter(e->e instanceof TypeElement).
                map(t->(TypeElement)t).forEach(t->prepare(info, t));
    }

    public void write(){
        infos.values().forEach(TransFormInfo::write);
        mappings.values().forEach(RegistryMapping::write);
    }

    public TransFormInfo findMessageV3Builder(TransFormInfo messageOrBuilder){
        for (Map.Entry<TypeElement, TransFormInfo> entry: infos.entrySet()){
            TransFormInfo tf = entry.getValue();
            if (tf.getProtoKind() == ProtoKind.BUILDER){
                if (tf.getProtoElement().getInterfaces().contains(messageOrBuilder.getProtoElement().asType())){
                    return tf;
                }
            }
        }
        return null;
    }

    public TransFormInfo findMessageOrBuilder(TransFormInfo messageV3){
        for (Map.Entry<TypeElement, TransFormInfo> entry: infos.entrySet()){
            TransFormInfo tf = entry.getValue();
            if (tf.getProtoKind() == ProtoKind.BUILDER_INTERFACE){
                if (messageV3.getProtoElement().getInterfaces().contains(tf.getProtoElement().asType())){
                    return tf;
                }
            }
        }
        return null;
    }

    public ClassName findRegistry(String packageName) {
        return Optional.ofNullable(mappings.get(packageName)).filter(RegistryMapping::hasBuilder).
                map(RegistryMapping::selfClassName).orElseGet(()->ClassName.get(TransFormRegistry.class));
    }

    private final Set<String> WRAPPERS = Arrays.stream(PropertyKind.values()).filter(PropertyKind::isWrapper).
            map(PropertyKind::rawType).map(Class::getCanonicalName).collect(Collectors.toSet());

    private boolean isMessageV3(TypeElement protoElement){
        return types.isSameType(protoElement.getSuperclass(), messageV3.asType()) && !WRAPPERS.contains(protoElement.toString());
    }

    private boolean isMessageV3Builder(TypeElement protoElement){
        String s = protoElement.getSuperclass().toString();
        String v = messageV3Builder.toString();
        return s.startsWith(v);
    }

    private boolean isMessageOrBuilder(TypeElement protoElement) {
        List<? extends TypeMirror> intfs = protoElement.getInterfaces();
        return intfs.size() == 1 && types.isSameType(intfs.get(0), messageOrBuilder.asType());
    }

    private boolean isMessageEnum(TypeElement protoElement) {
        List<? extends TypeMirror> intfs = protoElement.getInterfaces();
        return intfs.size() == 1 && types.isSameType(intfs.get(0), messageEnum.asType());
    }

    private boolean isEnumCase(TypeElement protoElement) {
        List<? extends TypeMirror> intfs = protoElement.getInterfaces();
        return intfs.size() == 2;
    }

    public ProtoKind getProtoType(TypeElement protoElement) {
        switch (protoElement.getKind()) {
            case CLASS: {
                if (this.isMessageV3(protoElement)) {
                    return ProtoKind.MESSAGE;
                }
                if (this.isMessageV3Builder(protoElement)) {
                    return ProtoKind.BUILDER;
                }
                break;
            }
            case INTERFACE: {
                if (this.isMessageOrBuilder(protoElement)) {
                    return ProtoKind.BUILDER_INTERFACE;
                }
                break;
            }
            case ENUM: {
                if (this.isMessageEnum(protoElement)) {
                    return ProtoKind.ENUM;
                }
                if (this.isEnumCase(protoElement)) {
                    return ProtoKind.ENUM_CASE;
                }
                break;
            }
        }
        return ProtoKind.OTHER;
    }

    public boolean isSameType(Class<?> type, TypeMirror typeMirror) {
        return types.isSameType(typeMirror, elements.getTypeElement(type.getCanonicalName()).asType());
    }

    public boolean isByteString(TypeMirror type) {
        return types.isSameType(type, byteString.asType());
    }

    public void warning(String message){
        this.messager.printMessage(Diagnostic.Kind.WARNING, message);
    }

    public void error(String message){
        this.messager.printMessage(Diagnostic.Kind.ERROR, message);
    }


}
