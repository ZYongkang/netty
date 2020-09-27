package com.easemob.im.nettyclient.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author yongkang.zhang@easemob.com
 */
@Slf4j
public class User {
    
    private final String name;
    private final Message content;
    private final MonoProcessor<User> userProcessor;
    
    private NettyConnection connection;
    
    public static LongAdder receiveCounter = new LongAdder();
    public static LongAdder sendCounter = new LongAdder();
    
    
    public User(String name, Message content, MonoProcessor<User> userProcessor) {
        this.name = name;
        this.content = content;
        this.userProcessor = userProcessor;
    }
    
    void handleConnection(NettyConnection connection) {
        log.info("user  connected | name:{}", name);
        this.connection = connection;
        receive();
    }
    
    public Mono<Long> send() throws JsonProcessingException {
        return this.connection.send(content).doOnSuccess(ignore -> sendCounter.increment()).thenReturn(1L);
    }
    
    public void receive() {
        this.connection.receive()
                .doOnNext(s -> {
                    if (log.isDebugEnabled()) {
                        log.debug(s.toString());
                    }
                    receiveCounter.increment();
                })
                .subscribe();
    }
}
