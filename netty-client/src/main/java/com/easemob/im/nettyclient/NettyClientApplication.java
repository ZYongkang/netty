package com.easemob.im.nettyclient;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyClientApplication {
    
    public static void main(String[] args) {
        Metrics.globalRegistry.add(new LoggingMeterRegistry());
        SpringApplication.run(NettyClientApplication.class, args);
    }
    
}
