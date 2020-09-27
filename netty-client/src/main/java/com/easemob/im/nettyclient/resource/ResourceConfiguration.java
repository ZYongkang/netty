package com.easemob.im.nettyclient.resource;

import com.easemob.im.nettyclient.model.Client;
import com.easemob.im.nettyclient.model.NettyClient;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yongkang.zhang@easemob.com
 */
@Configuration
public class ResourceConfiguration {
    
    @Bean
    @ConfigurationProperties("im.netty.resource")
    ResourceProperties resourceProperties() {
        return new ResourceProperties();
    }
    
    
    @Bean
    public List<Client> nettyClient(ResourceProperties properties) {
    
        List<Client> list = new ArrayList<>();
        URI remote = properties.getRemote();
        for (int i = 0; i < properties.getLocals().size(); i++) {
            String local = properties.getLocals().get(i);
            ConnectionProvider provider = ConnectionProvider.elastic(local);
            NioEventLoopGroup eventExecutors = new NioEventLoopGroup(4);
            Scheduler scheduler = Schedulers.newParallel(local);
            TcpClient tcpClient = TcpClient
                    .create(provider)
                    .bindAddress(() -> new InetSocketAddress(local, 0))
                    .remoteAddress(() -> new InetSocketAddress(remote.getHost(), remote.getPort()))
                    .runOn(eventExecutors);
    
            NettyClient nettyClient = new NettyClient(tcpClient, scheduler);
            list.add(nettyClient);
        }
        return list;
    }
    
}
