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

package org.jackstaff.grpc;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * the package for interact client side and server side
 * @author reco@jackstaff.org
 */
public class Packet<T> {

    private static final int OK = 0;
    private static final int EXCEPTION = 1;

    private int command;
    private T payload;

    public Packet() {

    }

    public Packet(int command) {
        this.command = command;
    }

    public Packet(int command, T payload) {
        this.command = command;
        this.payload = payload;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "command=" + command +
                ", payload=" + payload +
                '}';
    }

    boolean isException(){
        return command == EXCEPTION;
    }

    static Packet<?> throwable(Throwable ex){
        return new Packet<>(EXCEPTION, ex);
    }

    static Packet<?> ok(Object value){
        return new Packet<>(OK, value);
    }

    Object[] unboxing(){
        return Arrays.stream((Object[])payload).map(a->a ==null || a.getClass().equals(Object.class) ? null : a).toArray();
    }

    static Packet<Object[]> boxing(Object[] args){
        return new Packet<>(0, Arrays.stream(args).map(a-> a == null || a instanceof Consumer ? new Object() : a).toArray());
    }

}
