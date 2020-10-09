package com.easemob.im.nettyclient.model;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

/**
 * @author yongkang.zhang@easemob.com
 */
@Slf4j
public class NettyConnection {
    
    private final DirectProcessor<Void> closeProcessor;
    private final NettyInbound inbound;
    private final NettyOutbound outbound;
    
    private final DirectProcessor<String> processor = DirectProcessor.create();
    
    public NettyConnection(NettyInbound inbound, NettyOutbound outbound, DirectProcessor<Void> closeProcessor) {
        this.inbound = inbound;
        this.outbound = outbound;
        this.closeProcessor = closeProcessor;
        sub();
    }
    
    void close() {
        this.closeProcessor.onComplete();
    }
    
    NettyInbound inbound() {
        return inbound;
    }
    
    NettyOutbound outbound() {
        return outbound;
    }
    
    public Mono<Void> send(String content) {
        return this.outbound.sendString(Mono.just(content)).then();
    }
    
    private void sub() {
        this.inbound.receive()
                .asString()
                .doOnNext(processor::onNext)
                .subscribe();
        
    }
    
    public Flux<String> receive() {
        return processor;
    }
}
