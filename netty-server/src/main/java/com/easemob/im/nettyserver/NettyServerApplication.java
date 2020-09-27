package com.easemob.im.nettyserver;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyServerApplication {
    
    public static void main(String[] args) {
//        Metrics.globalRegistry.add(new LoggingMeterRegistry());
        SpringApplication.run(NettyServerApplication.class, args);
    }
    
}
