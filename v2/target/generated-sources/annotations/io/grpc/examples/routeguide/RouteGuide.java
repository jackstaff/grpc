package io.grpc.examples.routeguide;

import java.util.List;
import java.util.function.Consumer;
import org.jackstaff.grpc.annotation.AsynchronousUnary;
import org.jackstaff.grpc.annotation.BidiStreaming;
import org.jackstaff.grpc.annotation.BlockingServerStreaming;
import org.jackstaff.grpc.annotation.ClientStreaming;
import org.jackstaff.grpc.annotation.Protocol;
import org.jackstaff.grpc.annotation.ServerStreaming;

/**
 * Generated by org.jackstaff.grpc.generator.ProtocolProcessor;  DO NOT EDIT!
 */
@Protocol
public interface RouteGuide {
    String SERVICE_NAME = RegistryMapping.addProtocol(RouteGuide.class, RouteGuideGrpc.getServiceDescriptor());

    Feature getFeature(Point point);

    @AsynchronousUnary
    default void getFeature(Point point, Consumer<Feature> featureResult) {
        // AsynchronousUnary for overload getFeature. do NOT implement it.
    }

    @ServerStreaming
    void listFeatures(Rectangle rectangle, Consumer<Feature> featureStream);

    @BlockingServerStreaming
    default List<Feature> listFeatures(Rectangle rectangle) {
        // BlockingServerStreaming for overload listFeatures. do NOT implement it.
        return null;
    }

    @ClientStreaming
    Consumer<Point> recordRoute(Consumer<RouteSummary> routeSummaryResult);

    @BidiStreaming
    Consumer<RouteNote> routeChat(Consumer<RouteNote> routeNoteStream);
}