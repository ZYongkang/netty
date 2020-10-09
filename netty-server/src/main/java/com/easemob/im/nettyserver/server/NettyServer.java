package com.easemob.im.nettyserver.server;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.Connection;
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
    
    private static final MessageCodec CODEC = new MessageCodec();
    
    private static final Scheduler SCHEDULER = Schedulers.newParallel("netty-server-scheduler");
    
    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());
    
    public NettyServer() {
    }
    
    public void start(int port) {
    
        TcpServer tcpServer = TcpServer.create()
                .metrics(true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, 409600)
                .option(ChannelOption.SO_RCVBUF, 409600)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .port(port)
                .doOnConnection(connection -> {
                    connections.add(connection);
                    sendCounter.increment();
                })
                .handle((inbound, outbound) -> {
                    MessageHandler messageHandler = new MessageHandler(inbound, outbound);
                    DirectProcessor<Void> completion = DirectProcessor.create();
                    SCHEDULER.schedule(messageHandler::handlerMessage);
                    return completion;
                });
        tcpServer.bind().subscribe();
    }
    
    static class MessageHandler {
        private final NettyInbound inbound;
        private final NettyOutbound outbound;
        private final DirectProcessor<String> processor = DirectProcessor.create();
        private final FluxSink<String> fluxSink = processor.sink();
        
        public MessageHandler(NettyInbound inbound, NettyOutbound outbound) {
            this.inbound = inbound;
            this.outbound = outbound;
            receive();
        }
        
        private void receive() {
            this.inbound
                    .receive()
                    .asString()
                    .doOnNext(fluxSink::next)
                    .subscribe();
        }
        
        private void handlerMessage() {
            processor
                    .onBackpressureDrop()
                    .map(s -> {
                        log.info("receive [{}]", s);
                        receiveCounter.increment();
                        return outbound.sendString(Mono.just(s));
                    })
                    .subscribe();
        }
    }
}
