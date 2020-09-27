package com.easemob.im.nettyclient.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
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
    
    private final DirectProcessor<Message> processor = DirectProcessor.create();
    
    private static final MessageCodec codec = new MessageCodec();
    
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
    
    public Mono<Void> send(Message content) throws JsonProcessingException {
        ByteBuf outputBuf = this.outbound.alloc().buffer();
        codec.encode(content, outputBuf);
        return this.outbound.send(Mono.just(outputBuf)).then();
    }
    
    private void sub() {
        this.inbound.receiveObject()
                .cast(Message.class)
                .doOnNext(processor::onNext)
                .subscribe();
        
    }
    
    public Flux<Message> receive() {
        return processor;
    }
}
