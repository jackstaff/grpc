package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.configuration.ClientConfig;

import java.time.Duration;

public interface Constant {

    String DEMO_SERVER = "demo-server";

    String DEMO_CLIENT = "demo-client";

    /**
     * @see ClientConfig#getDefaultTimeout()
     */
    Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);

    int INVALID_ID = -1;

}
