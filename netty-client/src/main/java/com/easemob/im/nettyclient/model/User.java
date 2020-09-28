package com.easemob.im.nettyclient.model;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

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
        this.connection = connection;
        receive();
    }
    
    public Mono<Long> send() {
        return this.connection
                .send(content)
                .doOnSuccess(ignore -> {
                    log.info("send message success| [{}]", content);
                    sendCounter.increment();
                })
                .thenReturn(1L);
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
