package org.jackstaff.grpc.demo.common.interceptor;

import org.jackstaff.grpc.Context;
import org.jackstaff.grpc.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class LoggerInfo implements Interceptor {

    @Override
    public void before(Context context) throws Exception {
        context.setAttribute(this, System.nanoTime());
    }

    @Override
    public void returning(Context context, @Nullable Object result) {
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger -> {
            logger.info("{}.{}, useTime:{}ns, result: {}",
                    context.getType().getName(), context.getMethod().getName(),
                    System.nanoTime()-(Long) context.getAttribute(this), result);
        });
    }

    @Override
    public void recall(Context context, @Nonnull Exception ex) {
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger -> {
            logger.info("{}.{}, recalled! exception: {}", context.getType().getName(), context.getMethod().getName(), ex.getMessage());
        });
    }

    @Override
    public void throwing(Context context, @Nonnull Exception ex) {
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger -> {
            logger.info("{}.{}, useTime:{}ns, throw exception message: {}",
                    context.getType().getName(), context.getMethod().getName(),
                    System.nanoTime()-(Long) context.getAttribute(this), ex.getMessage());
        });
    }

}
