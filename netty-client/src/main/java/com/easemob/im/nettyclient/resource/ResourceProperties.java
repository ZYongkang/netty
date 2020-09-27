package com.easemob.im.nettyclient.resource;

import lombok.Data;

import java.net.URI;
import java.util.List;

/**
 * @author yongkang.zhang@easemob.com
 */
@Data
public class ResourceProperties {
    
    private List<String> locals;
    private URI remote;
    private int length;
    private int sendSpeed;
    private int clientCount;
}
