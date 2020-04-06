package org.jackstaff.grpc.demo.protocol;

import org.jackstaff.grpc.Transforms;
import org.jackstaff.grpc.annotation.BidiStreaming;
import org.jackstaff.grpc.annotation.Protocol;

import java.util.function.Consumer;

@Protocol
public interface GoodsService {

    @BidiStreaming
    Consumer<Count> countingGoods (Consumer<Goods> goodsConsumer);

    String SERVICE_NAME = Transforms.addProtocol(GoodsService.class, GoodsServiceGrpc.getServiceDescriptor());


}
