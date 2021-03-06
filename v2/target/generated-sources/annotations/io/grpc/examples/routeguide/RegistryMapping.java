package io.grpc.examples.routeguide;

import io.grpc.ServiceDescriptor;
import org.jackstaff.grpc.TransFormRegistry;

/**
 * Generated by org.jackstaff.grpc.generator.ProtocolProcessor;  DO NOT EDIT!
 */
class RegistryMapping {
    static {
        TransFormRegistry<Point, RouteGuideProto.Point, RouteGuideProto.Point.Builder> registry = new TransFormRegistry<>(Point.class, Point::new, RouteGuideProto.Point.class, RouteGuideProto.Point.Builder::build, RouteGuideProto.Point::newBuilder);
        registry.int32(Point::getLatitude, Point::setLatitude, RouteGuideProto.Point::getLatitude, RouteGuideProto.Point.Builder::setLatitude);
        registry.int32(Point::getLongitude, Point::setLongitude, RouteGuideProto.Point::getLongitude, RouteGuideProto.Point.Builder::setLongitude);
        registry.register();
    }
    static {
        TransFormRegistry<RouteSummary, RouteGuideProto.RouteSummary, RouteGuideProto.RouteSummary.Builder> registry = new TransFormRegistry<>(RouteSummary.class, RouteSummary::new, RouteGuideProto.RouteSummary.class, RouteGuideProto.RouteSummary.Builder::build, RouteGuideProto.RouteSummary::newBuilder);
        registry.int32(RouteSummary::getPointCount, RouteSummary::setPointCount, RouteGuideProto.RouteSummary::getPointCount, RouteGuideProto.RouteSummary.Builder::setPointCount);
        registry.int32(RouteSummary::getFeatureCount, RouteSummary::setFeatureCount, RouteGuideProto.RouteSummary::getFeatureCount, RouteGuideProto.RouteSummary.Builder::setFeatureCount);
        registry.int32(RouteSummary::getDistance, RouteSummary::setDistance, RouteGuideProto.RouteSummary::getDistance, RouteGuideProto.RouteSummary.Builder::setDistance);
        registry.int32(RouteSummary::getElapsedTime, RouteSummary::setElapsedTime, RouteGuideProto.RouteSummary::getElapsedTime, RouteGuideProto.RouteSummary.Builder::setElapsedTime);
        registry.register();
    }
    static {
        TransFormRegistry<FeatureDatabase, RouteGuideProto.FeatureDatabase, RouteGuideProto.FeatureDatabase.Builder> registry = new TransFormRegistry<>(FeatureDatabase.class, FeatureDatabase::new, RouteGuideProto.FeatureDatabase.class, RouteGuideProto.FeatureDatabase.Builder::build, RouteGuideProto.FeatureDatabase::newBuilder);
        registry.list(Feature.class, FeatureDatabase::hasFeature, FeatureDatabase::getFeature, FeatureDatabase::setFeature, RouteGuideProto.FeatureDatabase::getFeatureList, RouteGuideProto.FeatureDatabase.Builder::addAllFeature);
        registry.register();
    }
    static {
        TransFormRegistry<Rectangle, RouteGuideProto.Rectangle, RouteGuideProto.Rectangle.Builder> registry = new TransFormRegistry<>(Rectangle.class, Rectangle::new, RouteGuideProto.Rectangle.class, RouteGuideProto.Rectangle.Builder::build, RouteGuideProto.Rectangle::newBuilder);
        registry.value(Point.class, Rectangle::hasLo, Rectangle::getLo, Rectangle::setLo, RouteGuideProto.Rectangle::hasLo, RouteGuideProto.Rectangle::getLo, RouteGuideProto.Rectangle.Builder::setLo);
        registry.value(Point.class, Rectangle::hasHi, Rectangle::getHi, Rectangle::setHi, RouteGuideProto.Rectangle::hasHi, RouteGuideProto.Rectangle::getHi, RouteGuideProto.Rectangle.Builder::setHi);
        registry.register();
    }
    static {
        TransFormRegistry<RouteNote, RouteGuideProto.RouteNote, RouteGuideProto.RouteNote.Builder> registry = new TransFormRegistry<>(RouteNote.class, RouteNote::new, RouteGuideProto.RouteNote.class, RouteGuideProto.RouteNote.Builder::build, RouteGuideProto.RouteNote::newBuilder);
        registry.value(Point.class, RouteNote::hasLocation, RouteNote::getLocation, RouteNote::setLocation, RouteGuideProto.RouteNote::hasLocation, RouteGuideProto.RouteNote::getLocation, RouteGuideProto.RouteNote.Builder::setLocation);
        registry.string(RouteNote::hasMessage, RouteNote::getMessage, RouteNote::setMessage, RouteGuideProto.RouteNote::getMessage, RouteGuideProto.RouteNote.Builder::setMessage);
        registry.register();
    }
    static {
        TransFormRegistry<Feature, RouteGuideProto.Feature, RouteGuideProto.Feature.Builder> registry = new TransFormRegistry<>(Feature.class, Feature::new, RouteGuideProto.Feature.class, RouteGuideProto.Feature.Builder::build, RouteGuideProto.Feature::newBuilder);
        registry.string(Feature::hasName, Feature::getName, Feature::setName, RouteGuideProto.Feature::getName, RouteGuideProto.Feature.Builder::setName);
        registry.value(Point.class, Feature::hasLocation, Feature::getLocation, Feature::setLocation, RouteGuideProto.Feature::hasLocation, RouteGuideProto.Feature::getLocation, RouteGuideProto.Feature.Builder::setLocation);
        registry.register();
    }

    static String addProtocol(Class protocol, ServiceDescriptor descriptor) {
        return TransFormRegistry.addProtocol(protocol, descriptor);
    }
}
