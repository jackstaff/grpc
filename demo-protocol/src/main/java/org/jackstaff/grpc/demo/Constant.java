package org.jackstaff.grpc.demo;

import java.time.Duration;

public interface Constant {

    String DEMO_SERVER = "demo-server";

    String DEMO_CLIENT = "demo-client";

    Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);

    int INVALID_ID = -1;

}
