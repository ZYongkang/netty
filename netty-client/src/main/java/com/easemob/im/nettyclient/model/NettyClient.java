package com.easemob.im.nettyclient.model;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.netty.tcp.TcpClient;

/**
 * @author yongkang.zhang@easemob.com
 */
@Slf4j
public class NettyClient implements Client {
    
    private TcpClient tcpClient;
    
    private static final MessageCodec codec = new MessageCodec();
    
    private Scheduler scheduler;
    
    public NettyClient(TcpClient tcpClient, Scheduler scheduler) {
        this.tcpClient = tcpClient;
        this.scheduler = scheduler;
    }
    
    @Override
    public Mono<User> open(String username, Message content) {
        MonoProcessor<User> processor = MonoProcessor.create();
        User user = new User(username, content, processor);
        this.tcpClient
                .metrics(true)
                .handle((inbound, outbound) -> {
                    inbound.withConnection(connection -> {
                        connection.addHandler(new MSyncDecoder(codec));
                    });
                    DirectProcessor<Void> completion = DirectProcessor.create();
                    NettyConnection connection = new NettyConnection(inbound, outbound, completion);
                    scheduler.schedule(() -> user.handleConnection(connection));
                    return completion;
                })
                .connect()
                .subscribe();
        return Mono.just(user);
    }
    
}
