package com.easemob.im.nettyclient.model;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author yongkang.zhang@easemob.com
 */
public class MSyncDecoder extends ByteToMessageDecoder {
    
    private MessageCodec codec;
    
    public MSyncDecoder(MessageCodec codec) {
        this.codec = codec;
    }
    
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        List<Message> mSyncList = this.codec.decode(byteBuf);
        list.addAll(mSyncList);
    }
}
