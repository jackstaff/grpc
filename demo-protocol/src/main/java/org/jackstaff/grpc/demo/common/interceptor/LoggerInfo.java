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
    public void before(Context context) {
        context.setAttribute(this, System.nanoTime());
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger ->
            logger.info("BEFORE: {}.{}", context.getType().getName(), context.getMethod().getName())
        );
    }

    @Override
    public void returning(Context context, @Nullable Object result) {
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger -> {
            logger.info("RETURNING: {}.{}, useTime:{}ns, result: {}",
                    context.getType().getName(), context.getMethod().getName(),
                    System.nanoTime()-(Long) context.getAttribute(this), result);
        });
    }

    @Override
    public void recall(Context context, @Nonnull Exception ex) {
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger -> {
            logger.info("RECALL: {}.{}, recalled! exception: {}", context.getType().getName(), context.getMethod().getName(), ex.getMessage());
        });
    }

    @Override
    public void throwing(Context context, @Nonnull Exception ex) {
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger -> {
            logger.info("THROWING: {}.{}, useTime:{}ns, throw exception : {}",
                    context.getType().getName(), context.getMethod().getName(),
                    System.nanoTime()-(Long) context.getAttribute(this), ex);
        });
    }

}
