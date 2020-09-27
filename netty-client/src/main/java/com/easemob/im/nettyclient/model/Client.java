package com.easemob.im.nettyclient.model;

import reactor.core.publisher.Mono;

/**
 * @author yongkang.zhang@easemob.com
 */
public interface Client {
    
    Mono<User> open(String username, Message content);
}
