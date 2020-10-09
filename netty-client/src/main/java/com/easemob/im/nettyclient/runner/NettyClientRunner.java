package com.easemob.im.nettyclient.runner;

import com.easemob.im.nettyclient.model.Client;
import com.easemob.im.nettyclient.model.User;
import com.easemob.im.nettyclient.resource.ResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    
    @Override
    public void run(String... args) throws Exception {
        log.info("run this?");
        String content = "content";
        List<User> list = new ArrayList<>();
        for (int i = 0; i < resourceProperties.getClientCount(); i++) {
            int finalI = i;
            List<User> users = Flux.range(0, resourceProperties.getLength() / resourceProperties.getClientCount())
                    .delayElements(Duration.ofMillis(1))
                    .flatMap(n -> {
                        String string = UUID.randomUUID().toString();
                        return nettyClients.get(finalI).open(string, content);
                    })
                    .collectList()
                    .block();
            if (users != null && !users.isEmpty()) {
                list.addAll(users);
            }
        }
        
        Thread.sleep(5000);
        Flux.interval(Duration.ofSeconds(30))
                .flatMap(n -> Flux.fromIterable(list)
                .flatMap(User::send))
                .blockLast(Duration.ofMinutes(30));
    }
    
}
