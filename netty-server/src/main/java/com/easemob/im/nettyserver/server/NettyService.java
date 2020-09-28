package com.easemob.im.nettyserver.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author yongkang.zhang@easemob.com
 */
@Component
@Slf4j
public class NettyService implements CommandLineRunner {
    
    @Autowired
    private NettyServer nettyServer;
    
    @Override
    public void run(String... args) {
        nettyServer.start(5589);
        Flux.interval(Duration.ofSeconds(1))
                .doOnNext(ignore -> log.info("connection count:{}", NettyServer.sendCounter.sum()))
                .subscribe();
    }
}
