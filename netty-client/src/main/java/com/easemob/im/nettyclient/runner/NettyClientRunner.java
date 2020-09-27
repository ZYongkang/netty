package com.easemob.im.nettyclient.runner;

import com.easemob.im.nettyclient.model.*;
import com.easemob.im.nettyclient.resource.ResourceProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * @author yongkang.zhang@easemob.com
 */
@Slf4j
@Component
public class NettyClientRunner implements CommandLineRunner {
    
    @Autowired
    private List<Client> nettyClients;
    
    @Autowired
    private ResourceProperties resourceProperties;
    
    private final Random random = new Random();
    
    @Override
    public void run(String... args) throws Exception {
        log.info("run this?");
        Message message = new Message("content");
        List<User> list = new ArrayList<>();
        for (int i = 0; i < resourceProperties.getClientCount(); i++) {
            int finalI = i;
            List<User> users = Flux.range(0, resourceProperties.getLength() / resourceProperties.getClientCount())
                    .delayElements(Duration.ofMillis(10))
                    .flatMap(n -> {
                        String string = UUID.randomUUID().toString();
                        return nettyClients.get(finalI).open(string, message);
                    })
                    .collectList()
                    .block();
            if (users != null && !users.isEmpty()) {
                list.addAll(users);
            }
        }
        
        Thread.sleep(5000);
        int i = 1_000_000 / list.size();
        Flux.interval(Duration.ofMillis(1))
                .flatMap(n -> {
                    try {
                        User user = list.get(Math.toIntExact(n % list.size()));
                        return user.send().repeat(i);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                })
                .blockLast(Duration.ofMinutes(30));
    }
    
    private void loginfo() {
        Flux.interval(Duration.ofSeconds(1))
                .doOnNext(ignore -> log.info("send message count:{}, receive message count:{}",
                        User.sendCounter.sum(), User.receiveCounter.sum()))
                .subscribe();
    }
    
    
}
