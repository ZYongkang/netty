package com.easemob.im.nettyserver.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author yongkang.zhang@easemob.com
 */
@Slf4j
public class NettyServer {
    
    public static LongAdder receiveCounter = new LongAdder();
    public static LongAdder sendCounter = new LongAdder();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final MessageCodec codec = new MessageCodec();
    
    private static Scheduler scheduler = Schedulers.newParallel("netty-server-scheduler");
    
    private TcpServer tcpServer;
    
    private List<Connection> connections = Collections.synchronizedList(new ArrayList<>());
    
    public NettyServer() {
    }
    
    public void start(int port) {
        this.tcpServer = TcpServer.create()
                .metrics(true)
                .doOnConnection(connection -> log.info("client connected | address={}", connection.address()))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, 40960)
                .option(ChannelOption.SO_RCVBUF, 40960)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .port(port)
                .doOnConnection(connection -> {
                    connections.add(connection);
                    sendCounter.increment();
                    log.info("connection count:{}", sendCounter.sum());
                })
                
                .handle((inbound, outbound) -> {
                    inbound.withConnection(connection -> {
                        connection.addHandler(new MSyncDecoder(codec));
                    });
                    MessageHandler messageHandler = new MessageHandler(inbound, outbound);
                    DirectProcessor<Void> completion = DirectProcessor.create();
                    scheduler.schedule(messageHandler::handlerMessage);
                    return completion;
                });
        tcpServer.bind().subscribe();
    }
    
    class MessageHandler {
        private NettyInbound inbound;
        private NettyOutbound outbound;
        private final DirectProcessor<Message> processor = DirectProcessor.create();
        private final FluxSink<Message> fluxSink = processor.sink();
        
        public MessageHandler(NettyInbound inbound, NettyOutbound outbound) {
            this.inbound = inbound;
            this.outbound = outbound;
            receive();
        }
        
        private void receive() {
            this.inbound.receiveObject()
                    .cast(Message.class)
                    .doOnNext(fluxSink::next)
                    .subscribe();
        }
        
        private void handlerMessage() {
            processor
                    .onBackpressureDrop()
                    .map(message -> {
                        log.info("receive message : {}", message);
                        receiveCounter.increment();
                        ByteBuf buffer = outbound.alloc().buffer();
                        try {
                            codec.encode(message, buffer);
                            return outbound.send(Mono.just(buffer));
                        } catch (JsonProcessingException e) {
                            log.error("server send message failed | cause:{}", e.getMessage());
                            return e;
                        }
                    })
                    .subscribe();
        }
    }
}
