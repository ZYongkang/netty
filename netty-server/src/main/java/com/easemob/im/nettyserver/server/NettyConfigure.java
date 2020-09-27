package com.easemob.im.nettyserver.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yongkang.zhang@easemob.com
 */
@Configuration
public class NettyConfigure {
    
    @Bean
    public NettyServer nettyServer() {
        return new NettyServer();
    }
}
