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
import com.squareup.javapoet.*;
import org.jackstaff.grpc.TransFormRegistry;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.PropertyKind;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author reco@jackstaff.org
 */
class Property {

    private static final Function<String, String> fHas = s -> "has" + s;
    private static final Function<String, String> fGet = s -> "get" + s;
    private static final Function<String, String> fSet = s -> "set" + s;
    private static final Function<String, String> fBytes = s -> "get" + s + "Bytes";
    private static final Function<String, String> fValue = s -> "get" + s + "Value";
    private static final Function<String, String> fValueList = s -> "get" + s + "ValueList";
    private static final Function<String, String> fList = s -> "get" + s + "List";
    private static final Function<String, String> fCount = s -> "get" + s + "Count";
    private static final Function<String, String> fOrBuilder = s -> "get" + s + "OrBuilder";
    private static final Function<String, String> fOrBuilderList = s -> "get" + s + "OrBuilderList";
    private static final Function<String, String> fAddAll = s -> "addAll" + s;
    private static final Function<String, String> fClear = s -> "clear" + s;

    private final TransFormInfos transForms;
    private final TransFormInfo transform;
    private final String name;
    private final PropertyKind kind;
    private final Map<String, ExecutorInfo> executors;

    private Property oneOfCase;
    private Set<String> enumNames;
    private String enumNameSpecial;
    private TypeName typeName;
    private TypeName elementTypeName;
    private PropertyKind elementKind;
    private String fieldName;

    public Property(TransFormInfos transForms, TransFormInfo transform, String name, PropertyKind kind, List<ExecutorInfo> executors) {
        this.transForms = transForms;
        this.transform = transform;
        this.name = name;
        this.kind = kind;
        this.executors = executors.stream().collect(LinkedHashMap::new, (m, e) -> m.put(e.getName(), e), LinkedHashMap::putAll);
    }

    public Property(TransFormInfos transForms, TransFormInfo transform, String name, PropertyKind kind, ExecutorInfo info) {
        this(transForms, transform, name, kind, Collections.singletonList(info));
    }

    private String refFieldName() {
        return fieldName()+"_";
    }

    private static String UpperName(String name){
        return name.replaceAll("_","").toUpperCase();
    }

    private String enumName(String name) {
        return enumNames.stream().filter(s -> UpperName(s).equals(UpperName(name))).findFirst().orElse(null);
    }

    public String fieldName() {
        if (this.fieldName == null) {
            this.fieldName = fieldNameImp();
        }
        return fieldName;
    }

    private String fieldNameImp() {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public String getName() {
        return name;
    }

    public PropertyKind getKind() {
        return kind;
    }

    public Map<String, ExecutorInfo> getExecutors() {
        return executors;
    }

    public TypeName elementTypeName() {
        if (this.typeName == null) {
            this.typeName = typeNameImp();
        }
        return elementTypeName;
    }

    public TypeName typeName() {
        if (this.typeName == null) {
            this.typeName = typeNameImp();
        }
        return typeName;
    }

    TransFormInfo dependency(){
        switch (kind) {
            case ENUM:
            case MESSAGE:
            case MESSAGE_LIST:
            case ENUM_LIST:
                return transForms.get(findExecutor(fGet).getReturnType());
        }
        return null;
    }

    private TypeName typeNameImp() {
        switch (kind.category()){
            case PRIMITIVE:
            case STRING:
                return TypeName.get(kind.rawType());
            case BYTES:
            case WRAPPER:
                return TypeName.get(kind.boxingType());
            case MESSAGE:
            case ENUM:
                return transForms.get(findExecutor(fGet).getReturnType()).pojoTypeName();
            case REPEATED:
                TypeMirror typeMirror = this.findExecutor(fGet).getReturnType();
                switch (kind) {
                    case PRIMITIVE_LIST:
                        this.elementTypeName = TypeName.get(typeMirror);
                        return TypeName.get(findExecutor(fList).getReturnType());
                    case WRAPPER_LIST:
                        this.elementKind =Arrays.stream(PropertyKind.values()).filter(PropertyKind::isWrapper).
                                filter(k -> transForms.isSameType(k.rawType(), typeMirror)).findAny().orElseThrow(()->new ValidationException("NEVER"));
                        this.elementTypeName = TypeName.get(elementKind.boxingType());
                        return ParameterizedTypeName.get(ClassName.get(List.class), elementTypeName);
                    case STRING_LIST:
                        this.elementKind = PropertyKind.STRING;
                        this.elementTypeName = TypeName.get(String.class);
                        return ParameterizedTypeName.get(ClassName.get(List.class), elementTypeName);
                    case BYTES_LIST:
                        this.elementKind = PropertyKind.BYTES;
                        this.elementTypeName = TypeName.get(byte[].class);
                        return ParameterizedTypeName.get(ClassName.get(List.class), elementTypeName);
                    case ENUM_LIST:
                    case MESSAGE_LIST:
                        this.elementKind = kind == PropertyKind.ENUM_LIST ? PropertyKind.ENUM : PropertyKind.MESSAGE;
                        this.elementTypeName = transForms.get(typeMirror).pojoTypeName();
                        return ParameterizedTypeName.get(ClassName.get(List.class), elementTypeName);
                }
            case UNRECOGNIZED://never
            default:
                return TypeName.get(kind.rawType());
        }
    }

    private String name(Function<String, String> func){
        return func.apply(name);
    }

    private ExecutorInfo findExecutor(Function<String, String> func) {
        return executors.get(func.apply(name));
    }

    private void addField(TypeSpec.Builder builder) {
        if (oneOfCase == null) {
            builder.addField(FieldSpec.builder(typeName(), fieldName(), Modifier.PRIVATE).build());
        }
        if (kind == PropertyKind.ONE_OF_CASE) {
            builder.addField(FieldSpec.builder(TypeName.get(Object.class), refFieldName(), Modifier.PRIVATE).build());
            builder.addMethod(MethodSpec.methodBuilder(fClear.apply(name)).
                    addModifiers(Modifier.PUBLIC).returns(Void.TYPE).
                    addStatement("this.$N = null", fieldName()).addStatement("this.$N = null", refFieldName()).build());
        }
    }

    private void addGet(TypeSpec.Builder builder) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder(fGet.apply(name)).addModifiers(Modifier.PUBLIC).returns(typeName());
        if (oneOfCase == null) {
            switch (kind.category()) {
                case ENUM:
                    spec.addStatement("return this.$N != null ? this.$N : $T.$N", fieldName(), fieldName(), typeName(), enumNameSpecial);
                    break;
                case PRIMITIVE:
                case WRAPPER:
                case MESSAGE:
                    spec.addStatement("return this.$N", fieldName());
                    break;
                case BYTES:
                    spec.addStatement("return this.$N != null ? this.$N : new byte[0]", fieldName(), fieldName());
                    break;
                case STRING:
                    spec.addStatement("return this.$N != null ? this.$N : \"\"", fieldName(), fieldName());
                    break;
                case REPEATED:
                    spec.addStatement("return this.$N != null ? this.$N : $T.emptyList()", fieldName(), fieldName(), Collections.class);
                    break;
            }
        } else {
            switch (kind.category()) {
                case PRIMITIVE:
                case STRING:
                case BYTES:
                    spec.addStatement("return this.$N == $T.$N ? ($T)$N : $N",
                            oneOfCase.fieldName(), oneOfCase.typeName(), oneOfCase.enumName(name),
                            TypeName.get(kind.boxingType()), oneOfCase.refFieldName(), kind.defaultValue());
                    break;
//                case STRING:
//                    spec.addStatement("return this.$N == $T.$N ? ($T)$N : $N",
//                            oneOfCase.fieldName(), oneOfCase.typeName(), oneOfCase.enumName(name),
//                            typeName(), oneOfCase.refFieldName(), kind.defaultValue());
//                    break;
//                case BYTES:
//                    spec.addStatement("return this.$N == $T.$N ? ($T)$N : new byte[0]",
//                            oneOfCase.fieldName(), oneOfCase.typeName(), oneOfCase.enumName(name),
//                            typeName(), oneOfCase.refFieldName());
//                    break;
                case ENUM:
                    if (kind == PropertyKind.ONE_OF_CASE){
                        transForms.error("NEVER: ONE_OF_CASE's ONE_OF_CASE");
                        return;
                    }
                    spec.addStatement("return this.$N == $T.$N ? ($T)$N : $T.$N",
                            oneOfCase.fieldName(), oneOfCase.typeName(), oneOfCase.enumName(name),
                            typeName(), oneOfCase.refFieldName(), typeName(), enumNameSpecial);
                    break;
                case MESSAGE:
                case WRAPPER:
                    spec.addStatement("return this.$N == $T.$N ? ($T)$N : null",
                            oneOfCase.fieldName(), oneOfCase.typeName(), oneOfCase.enumName(name),
                            typeName(), oneOfCase.refFieldName());
                    break;
                case REPEATED:
                    spec.addStatement("return this.$N == $T.$N ? ($T)$N : $T.emptyList()",
                            oneOfCase.fieldName(), oneOfCase.typeName(), oneOfCase.enumName(name),
                            typeName(), oneOfCase.refFieldName(), Collections.class);
                    break;
            }
        }
        builder.addMethod(spec.build());
    }

    private void addSet(TypeSpec.Builder builder) {
        if (kind == PropertyKind.ONE_OF_CASE){
            return;
        }
        MethodSpec.Builder spec = MethodSpec.methodBuilder(fSet.apply(name)).addModifiers(Modifier.PUBLIC).
                returns(Void.TYPE).addParameter(typeName(), fieldName());
        if (oneOfCase == null) {
            spec.addStatement("this.$N = $N", fieldName(), fieldName());
        } else {
            switch (kind.category()) {
                case PRIMITIVE:
                    spec.addStatement("this.$N = $N", oneOfCase.refFieldName(), fieldName());
                    spec.addStatement("this.$N = $T.$N", oneOfCase.fieldName(), oneOfCase.typeName(), oneOfCase.enumName(name));
                    break;
                case ENUM:
                    spec.addStatement("if ($N == $T.$N) \nthrow new $T(\"$N must not be unknown value\")",
                            fieldName(), typeName(), enumNameSpecial, IllegalArgumentException.class, fieldName());
                    //break;
                case STRING:
                case BYTES:
                default:
                    spec.addStatement("this.$N = $T.requireNonNull($N)", oneOfCase.refFieldName(), Objects.class, fieldName());
                    spec.addStatement("this.$N = $T.$N", oneOfCase.fieldName(), oneOfCase.typeName(), oneOfCase.enumName(name));
                    break;
            }
        }
        builder.addMethod(spec.build());
    }

    private void addHas(TypeSpec.Builder builder) {
        if (kind == PropertyKind.ONE_OF_CASE){
            return;
        }
        MethodSpec.Builder spec = MethodSpec.methodBuilder(fHas.apply(name)).addModifiers(Modifier.PUBLIC).returns(Boolean.TYPE);
        if (oneOfCase ==null){
            switch (kind.category()){
                case PRIMITIVE:
                    return;
                case STRING:
                    spec.addStatement("return this.$N != null && this.$N.length()>0", fieldName(), fieldName());
                    break;
                case REPEATED:
                    spec.addStatement("return this.$N != null && this.$N.size()>0", fieldName(), fieldName());
                    break;
                case BYTES:
                    spec.addStatement("return this.$N != null && this.$N.length >0", fieldName(), fieldName());
                    break;
                case WRAPPER:
                case MESSAGE:
                    spec.addStatement("return this.$N != null", fieldName());
                    break;
                case ENUM:
                    spec.addStatement("return this.$N != null && this.$N != $T.$N", fieldName(), fieldName(), typeName(), enumNameSpecial);
                    break;
            }
        }else {
            spec.addStatement("return this.$N == $T.$N", oneOfCase.fieldName(), oneOfCase.typeName(), oneOfCase.enumName(name));
        }
        builder.addMethod(spec.build());
    }

    private void build(TypeSpec.Builder builder) {
        addField(builder);
        addHas(builder);
        addGet(builder);
        addSet(builder);
    }

    public static CodeBlock.Builder registryCodeBlock(TypeName typeName, TypeName protoTypeName, TypeName builderTypeName, Map<String, Property> properties){
        CodeBlock.Builder block = CodeBlock.builder();
        //block.addStatement("//Registry mapping: $T -> $T", typeName, protoTypeName);
        TypeName regTypeName = ParameterizedTypeName.get(ClassName.get(TransFormRegistry.class), typeName, protoTypeName, builderTypeName);
        block.addStatement("$T registry = new $T<>($T.class, $T::new, $T.class, $T::build, $T::newBuilder)",
                regTypeName, TransFormRegistry.class, typeName, typeName, protoTypeName, builderTypeName, protoTypeName);
        properties.values().stream().filter(p->p.oneOfCase==null).forEach(p -> {
            switch (p.kind.category()) {
                case PRIMITIVE:
                    block.addStatement("registry.$N($T::$N, $T::$N, $T::$N, $T::$N)", p.kind.func(),
                            typeName, p.name(fGet), typeName, p.name(fSet),
                            protoTypeName, p.name(fGet), builderTypeName, p.name(fSet));
                    break;
                case WRAPPER:
                    block.addStatement("registry.$N($T::$N, $T::$N, $T::$N, $T::$N, $T::$N, $T::$N)",p.kind.func(),
                            typeName, p.name(fHas), typeName, p.name(fGet), typeName, p.name(fSet),
                            protoTypeName, p.name(fHas), protoTypeName, p.name(fGet), builderTypeName, p.name(fSet));
                    break;
                case MESSAGE:
                    block.addStatement("registry.value($T.class, $T::$N, $T::$N, $T::$N, $T::$N, $T::$N, $T::$N)",
                            p.typeName(), typeName, p.name(fHas), typeName, p.name(fGet), typeName, p.name(fSet),
                            protoTypeName, p.name(fHas), protoTypeName, p.name(fGet), builderTypeName, p.name(fSet));
                    break;
                case ENUM:
                    if (p.kind == PropertyKind.ENUM){
                        block.addStatement("registry.value($T.class, $T::$N, $T::$N, $T::$N, $T::$N, $T::$N)",
                                p.typeName(), typeName, p.name(fHas),
                                typeName, p.name(fGet), typeName, p.name(fSet),
                                protoTypeName, p.name(fGet), builderTypeName, p.name(fSet));
                    }
                    break;
                case STRING:
                case BYTES:
                    block.addStatement("registry.$N($T::$N, $T::$N, $T::$N, $T::$N, $T::$N)", p.kind.func(),
                            typeName, p.name(fHas), typeName, p.name(fGet), typeName, p.name(fSet),
                            protoTypeName, p.name(fGet), builderTypeName, p.name(fSet));
                    break;
                case REPEATED:
                    switch (p.kind) {
                        case PRIMITIVE_LIST:
                        case STRING_LIST:
                            block.addStatement("registry.list($T::$N, $T::$N, $T::$N, $T::$N, $T::$N)",
                                    typeName, p.name(fHas), typeName, p.name(fGet), typeName, p.name(fSet),
                                    protoTypeName, p.name(fList), builderTypeName, p.name(fAddAll));
                            break;
                        case BYTES_LIST:
                        case WRAPPER_LIST:
                            block.addStatement("registry.$Ns($T::$N, $T::$N, $T::$N, $T::$N, $T::$N)", p.elementKind.func(),
                                   // PropertyKind.class, p.elementKind.name(),
                                    typeName, p.name(fHas), typeName, p.name(fGet), typeName, p.name(fSet),
                                    protoTypeName, p.name(fList), builderTypeName, p.name(fAddAll));
                            break;
                        case ENUM_LIST:
                        case MESSAGE_LIST:
                        default:
                            block.addStatement("registry.list($T.class, $T::$N, $T::$N, $T::$N, $T::$N, $T::$N)",
                                    p.elementTypeName,
                                    typeName, p.name(fHas), typeName, p.name(fGet), typeName, p.name(fSet),
                                    protoTypeName, p.name(fList), builderTypeName, p.name(fAddAll));
                            break;
                    }
                    break;
            }
        });
        for (Map.Entry<Property, List<Property>> entry : properties.values().stream().filter(p->p.oneOfCase!=null).
                collect(Collectors.groupingBy(p->p.oneOfCase)).entrySet()) {
            CodeBlock.Builder code = CodeBlock.builder();
            Property oneOf = entry.getKey();
            code.add("registry.oneOf($T.class, $T::$N, $T::$N).", oneOf.typeName(), typeName,
                    oneOf.name(fGet), protoTypeName, oneOf.name(fGet)).indent();
            for (Property p: entry.getValue()) {
                code.add("\r\n");
                switch (p.kind.category()) {
                    case PRIMITIVE:
                    case STRING:
                    case WRAPPER:
                    case BYTES:
                        code.add("$N($T.$N, $T::$N, $T::$N, $T::$N, $T::$N).", p.kind.func(),
                                oneOf.typeName(), oneOf.enumName(p.name),
                                typeName, p.name(fGet), typeName, p.name(fSet),
                                protoTypeName, p.name(fGet), builderTypeName, p.name(fSet));
                        break;
                    case MESSAGE:
                    case ENUM:
                        code.add("mapping($T.$N, $T.class, $T::$N, $T::$N, $T::$N, $T::$N).",
                                oneOf.typeName(), oneOf.enumName(p.name), p.typeName(),
                                typeName, p.name(fGet), typeName, p.name(fSet),
                                protoTypeName, p.name(fGet), builderTypeName, p.name(fSet));
                        break;
                    case REPEATED://never
                    default:
                        break;
                }
            }
            code.add("\r\n").add("build();").add("\r\n").unindent();
            block.add(code.build());
        }

        block.addStatement(CodeBlock.of("registry.register()"));
        return block;
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Property){
            Property another = (Property) object;
            return this.transform == another.transform && this.name.equals(another.name);
        }
        return false;
    }

    static void addConstructor(TypeSpec.Builder builder, Map<String, Property> properties){
        builder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());
        List<Property> props = properties.values().stream().
                filter(p->p.kind != PropertyKind.ONE_OF_CASE && p.oneOfCase == null).collect(Collectors.toList());
        if (props.size()>0){
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
            for (Property p: props){
                constructor.addParameter(p.typeName(), p.fieldName());
                constructor.addStatement("this.$N = $N", p.fieldName(), p.fieldName());
            }
            builder.addMethod(constructor.build());
        }
    }

    public static void build(TypeSpec.Builder builder, TransFormInfos transForms, Map<String, Property> properties) {
        List<Property> enumProps = properties.values().stream().filter(p -> p.kind.isEnum()).collect(Collectors.toList());
        for (Property property: enumProps) {
            ExecutorInfo info = property.findExecutor(fGet);
            TransFormInfo transForm = transForms.get(info.getReturnType());
            if (transForm == null) {
                transForms.error("NEVER HAPPEN! NOT MATCH "+property.fieldName()+"("+property.getKind()+"),"+info.getReturnType()+",,"+info.getName());
                return;
            }
            TypeElement typeElement = transForms.get(info.getReturnType()).getProtoElement();
            List<String> enumNames = Utils.enumNames(typeElement);
            property.enumNameSpecial = enumNames.remove(enumNames.size()-1);
            property.enumNames = new HashSet<>(enumNames);
        }
        enumProps.stream().filter(p -> p.kind == PropertyKind.ONE_OF_CASE).forEach(property -> {
            for (Map.Entry<String, Property> entry : properties.entrySet()) {
                Optional.ofNullable(property.enumName(entry.getKey())).
                        ifPresent(p->entry.getValue().oneOfCase = property);
            }
        });
        properties.values().forEach(property -> property.build(builder));
    }

    private static List<Function<String, String>> nameFunctions(PropertyKind kind) {
        /**
         * STRING,  //String..getXXX(),  ..ByteString..getXXXBytes(),,,
         * BYTES, //ByteString.. getXXX(),
         * MESSAGE, //getXXX(), hasXXX(), getXXXOrBuilder()
         * WRAPPER, //same as message
         * ENUM, //getXXX(), .int.getXXXValue
         * ONE_OF_CASE, //getXXX() is one case
         * PRIMITIVE_LIST, //getXXX(int),  ..List<XXX>..getXXXList(),,,..int..getXXXCount(),,,,,
         * BYTES_LIST, //ByteString..getXXX(int),  ..List<XXX>..getXXXList(),,,..int..getXXXCount(),,,,,
         * STRING_LIST,//getXXX(int),  ..List<XXX>..getXXXList(),,,..int..getXXXCount(),,,,ByteString.. getXXXBytes(),,
         * ENUM_LIST, //getXXX(int),  ..List<XXX>..getXXXList(),,, ..int..getXXXCount(),,,,, ..int.getXXXValue(int),  ..List<Integer>..getXXXValueList()
         * MESSAGE_LIST, //getXXX(int),  ..List<XXX>..getXXXList(),,, ..int..getXXXCount(),,,,, ..getXXXOrBuilder(int),  ..List<XXXOrBuilder>..getXXXOrBuilderList()
         */
        switch (kind) {
            case STRING:
                return Arrays.asList(fGet, fBytes);
            case MESSAGE:
                return Arrays.asList(fGet, fHas, fOrBuilder);
            case ENUM:
                return Arrays.asList(fGet, fValue);
            case STRING_LIST:
                return Arrays.asList(fGet, fList, fCount, fBytes);
            case PRIMITIVE_LIST:
                return Arrays.asList(fGet, fList, fCount);
            case ENUM_LIST:
                return Arrays.asList(fGet, fList, fCount, fValue, fValueList);
            case MESSAGE_LIST:
                return Arrays.asList(fGet, fList, fCount, fOrBuilder, fOrBuilderList);
            default:
                return Collections.singletonList(fGet); //NEVER
        }
    }

    private static Optional<Property> extractByName(TransFormInfos transForms, TransFormInfo transform, Map<String, ExecutorInfo> infos,
                                              String name, PropertyKind kind) {
        List<String> names = nameFunctions(kind).stream().map(f -> f.apply(name)).collect(Collectors.toList());
        if (infos.keySet().containsAll(names)) {
            List<ExecutorInfo> executors =names.stream().map(infos::remove).collect(Collectors.toList());
            switch (kind) {
                case MESSAGE:
                case MESSAGE_LIST:
                case PRIMITIVE_LIST:
                    ExecutorInfo get = executors.stream().filter(e->e.getName().equals(fGet.apply(name))).
                            findFirst().orElseThrow(()->new ValidationException("NEVER"));
                    if (transForms.isSameType(ByteString.class, get.getReturnType())){//is bytes list
                        return Optional.of(new Property(transForms, transform, name, PropertyKind.BYTES_LIST, executors));
                    }
                    Optional<PropertyKind> wrapper =Arrays.stream(PropertyKind.values()).filter(PropertyKind::isWrapper).
                            filter(k->transForms.isSameType(k.rawType(), get.getReturnType())).findFirst();
                    if (wrapper.isPresent()){ //wrapper or wrapper list
                        if (kind == PropertyKind.MESSAGE) {
                            return Optional.of(new Property(transForms, transform, name, wrapper.get(), executors));
                        }
                        return Optional.of(new Property(transForms, transform, name, PropertyKind.WRAPPER_LIST, executors));
                    }
                    break;
            }
            return Optional.of(new Property(transForms, transform, name, kind, executors));
        }
        return Optional.empty();
    }

    private static void extractByName(TransFormInfos transForms, TransFormInfo transform, Map<String, ExecutorInfo> infos, List<String> names, Map<String, Property> properties) {
        List<PropertyKind> kinds = Arrays.asList(PropertyKind.MESSAGE_LIST, PropertyKind.ENUM_LIST,
                PropertyKind.STRING_LIST, PropertyKind.PRIMITIVE_LIST,
                PropertyKind.MESSAGE, PropertyKind.ENUM, PropertyKind.STRING);
        for (PropertyKind kind: kinds){
            for (String name: names){
                extractByName(transForms, transform, infos, name, kind).ifPresent(p -> properties.put(p.getName(), p));
            }
        }
    }

    private static Optional<Property> extractByKind(TransFormInfos transForms, TransFormInfo transform, String mname, ExecutorInfo executor) {
        if (mname.startsWith("get")) {
            String name = mname.substring(3);
            TypeKind kind = executor.getReturnKind();
            if (kind.isPrimitive()) {
                return Optional.of(new Property(transForms, transform, name, PropertyKind.valueOf(kind.name()), executor));
            }
            if (kind == TypeKind.DECLARED) {
                if (transForms.isByteString(executor.getReturnType())) {
                    return Optional.of(new Property(transForms, transform, name, PropertyKind.BYTES, executor));
                }
                return Optional.of(new Property(transForms, transform, name, PropertyKind.ONE_OF_CASE, executor));
            }
        }
        return Optional.empty();
    }

    public static Map<String, Property> extract(TransFormInfos transForms, TransFormInfo transform, Map<String, ExecutorInfo> infos) {
        List<String> names = infos.keySet().stream().filter(name -> name.startsWith("get")).map(name -> name.substring(3)).collect(Collectors.toList());
        Map<String, Property> properties = new HashMap<>();
        List<String> sortedNames = names.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.toList());
        extractByName(transForms, transform, infos, sortedNames, properties);
        infos.forEach((name, executor) -> extractByKind(transForms, transform, name, executor).ifPresent(p -> properties.put(p.getName(), p)));
        return names.stream().map(properties::get).filter(Objects::nonNull).collect(LinkedHashMap::new, (m,p)->m.put(p.getName(), p), Map::putAll);
    }


}
