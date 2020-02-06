package org.jackstaff.grpc.configuration;

public class ServerConfig {

    private int port;
    private int keepAliveTime;
    private int keepAliveTimeout;
    private int permitKeepAliveTime;
    private boolean permitKeepAliveWithoutCalls=true;
    private int maxInboundMessageSize;
    private int maxInboundMetadataSize=20*1024;
    private int maxConnectionIdle;
    private int maxConnectionAge;
    private int maxConnectionAgeGrace;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public boolean isPermitKeepAliveWithoutCalls() {
        return permitKeepAliveWithoutCalls;
    }

    public void setPermitKeepAliveWithoutCalls(boolean permitKeepAliveWithoutCalls) {
        this.permitKeepAliveWithoutCalls = permitKeepAliveWithoutCalls;
    }

    public int getPermitKeepAliveTime() {
        return permitKeepAliveTime;
    }

    public void setPermitKeepAliveTime(int permitKeepAliveTime) {
        this.permitKeepAliveTime = permitKeepAliveTime;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
    }

    public int getMaxConnectionAge() {
        return maxConnectionAge;
    }

    public void setMaxConnectionAge(int maxConnectionAge) {
        this.maxConnectionAge = maxConnectionAge;
    }

    public int getMaxConnectionIdle() {
        return maxConnectionIdle;
    }

    public void setMaxConnectionIdle(int maxConnectionIdle) {
        this.maxConnectionIdle = maxConnectionIdle;
    }

    public int getMaxConnectionAgeGrace() {
        return maxConnectionAgeGrace;
    }

    public void setMaxConnectionAgeGrace(int maxConnectionAgeGrace) {
        this.maxConnectionAgeGrace = maxConnectionAgeGrace;
    }

    public int getMaxInboundMetadataSize() {
        return maxInboundMetadataSize;
    }

    public void setMaxInboundMetadataSize(int maxInboundMetadataSize) {
        this.maxInboundMetadataSize = maxInboundMetadataSize;
    }
}
