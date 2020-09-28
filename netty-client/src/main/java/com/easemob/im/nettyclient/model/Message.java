package com.easemob.im.nettyclient.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yongkang.zhang@easemob.com
 */
@Data
@EqualsAndHashCode
public class Message {
    
    private String content;
    
    @JsonCreator
    public Message(@JsonProperty("content") String content) {
        this.content = content;
    }
}
