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

package org.jackstaff.grpc.internal;

import com.google.protobuf.*;

/**
 * @author reco@jackstaff.org
 */
public enum PropertyKind {

    BOOLEAN(Boolean.TYPE, Boolean.class, "false","bool"),
    BYTE(Byte.TYPE, Byte.class, "(byte)0", "int8"),
    SHORT(Short.TYPE, Short.class, "(short)0", "int16"),
    INT(Integer.TYPE, Integer.class, "(int)0", "int32"),
    LONG(Long.TYPE, Long.class, "(long)0", "int64"),
    CHAR(Character.TYPE, Character.class, "(char)0", "character"),
    FLOAT(Float.TYPE, Float.class, "(float)0", "float32"),
    DOUBLE(Double.TYPE, Double.class, "(double)0", "float64"),

    DOUBLE_VALUE(DoubleValue.class, Double.class, "(double)0", "doubleValue"),
    FLOAT_VALUE(FloatValue.class, Float.class, "(float)0", "floatValue"),
    INT_VALUE(Int32Value.class, Integer.class, "(int)0","int32Value"),
    LONG_VALUE(Int64Value.class, Long.class, "(long)0", "int64Value"),
    U_INT_VALUE(UInt32Value.class, Integer.class, "(int)0", "uint32Value"),
    U_LONG_VALUE(UInt64Value.class, Long.class, "(long)0", "uint64Value"),
    BOOL_VALUE(BoolValue.class, Boolean.class, "false","boolValue"),
    DURATION(Duration.class, java.time.Duration.class, null,"duration"),
    TIMESTAMP(Timestamp.class, java.sql.Timestamp.class, null,"timestamp"),
    STRING_VALUE(StringValue.class, String.class, null,"stringValue"),
    BYTES_VALUE(BytesValue.class, byte[].class, null,"bytesValue"),

    STRING(String.class, String.class, "\"\"","string"),  //String..getXXX(),  ..ByteString..getXXXBytes(),,,
    BYTES(ByteString.class, byte[].class, "new byte[0]","bytes"), //ByteString.. getXXX()

    MESSAGE("message"), //getXXX(), hasXXX()

    ENUM("enumerate"), //getXXX(), .int.getXXXValue
    ONE_OF_CASE("oneOf"), //getXXX() is one case

    PRIMITIVE_LIST("list"), //getXXX(int),  ..List<XXX>..getXXXList(),,,
    STRING_LIST("list"), //getXXX(int),  ..List<XXX>..getXXXList(),,,..int..getXXXCount(),,,,ByteString.. getXXXBytes(),,
    BYTES_LIST("list"),
    WRAPPER_LIST("list"),
    ENUM_LIST("list"), //getXXX(int),  ..List<XXX>..getXXXList(),,, ..int..getXXXCount(),,,,, ..int.getXXXValue(int),  ..List<Integer>..getXXXValueList()
    MESSAGE_LIST("list"); //getXXX(int),  ..List<XXX>..getXXXList(),,, ..int..getXXXCount(),,,,, ..getXXXOrBuilder(int),  ..List<XXXOrBuilder>..getXXXOrBuilderList()

    public enum Category {
        PRIMITIVE, //get & set
        WRAPPER, // has & get & set
        STRING,  // has(only pojo) & get & set
        BYTES, // has(only pojo) & get & set
        MESSAGE, // has & get & set
        ENUM, // has(only pojo) & get & set
        REPEATED,// has(only pojo) & get & set
        UNRECOGNIZED,
    }

    private final Class<?> rawType;
    private final Class<?> boxingType;
    private final String defaultValue;
    private final String func;

    PropertyKind(String func) {
        this(null, null, null, func);
    }

    PropertyKind(Class<?> rawType, Class<?> boxingType, String defaultValue, String func) {
        this.rawType = rawType;
        this.boxingType = boxingType;
        this.defaultValue = defaultValue;
        this.func = func;
    }

    public Category category(){
        switch (this) {
            case ONE_OF_CASE:
            case ENUM:
                return Category.ENUM;
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case CHAR:
            case FLOAT:
            case DOUBLE:
                return Category.PRIMITIVE;
            case BOOL_VALUE:
            case INT_VALUE:
            case U_INT_VALUE:
            case LONG_VALUE:
            case U_LONG_VALUE:
            case FLOAT_VALUE:
            case DOUBLE_VALUE:
            case BYTES_VALUE:
            case STRING_VALUE:
            case DURATION:
            case TIMESTAMP:
                return Category.WRAPPER;
            case PRIMITIVE_LIST:
            case STRING_LIST:
            case BYTES_LIST:
            case WRAPPER_LIST:
            case ENUM_LIST:
            case MESSAGE_LIST:
                return Category.REPEATED;
            case STRING:
                return Category.STRING;
            case MESSAGE:
                return Category.MESSAGE;
            case BYTES:
                return Category.BYTES;
            default:
                return Category.UNRECOGNIZED;
        }
    }

    public boolean isPrimitive() {
        return category() == Category.PRIMITIVE;
    }

    public boolean isWrapper() {
        return category() == Category.WRAPPER;
    }

    public boolean isRepeated() {
        return category() == Category.REPEATED;
    }

    public boolean isEnum(){
        return category() == Category.ENUM;
    }

    public Class<?> rawType() {
        return rawType;
    }

    public Class<?> boxingType() {
        return boxingType;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public String func() {
        return func;
    }

}
