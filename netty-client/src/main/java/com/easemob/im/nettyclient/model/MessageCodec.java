package com.easemob.im.nettyclient.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * @author yongkang.zhang@easemob.com
 */
@Slf4j
public class MessageCodec {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<Message> decode(ByteBuf buf) {
        ImmutableList.Builder<Message> builder = ImmutableList.builder();
        try {
            do {
                buf.markReaderIndex();
                int frameLength = readFrameLength(buf);
                if (!buf.isReadable(frameLength)) {
                    buf.resetReaderIndex();
                    break;
                }
                Message message = readFrame(buf.readSlice(frameLength));
                builder.add(message);
            } while (true);
        } catch (IndexOutOfBoundsException e) {
            buf.resetReaderIndex();
        }
        return builder.build();
    }
    
    public void encode(Message message, ByteBuf outputBuf) {
        byte[] array = new byte[0];
        try {
            array = objectMapper.writeValueAsBytes(message);
        } catch (JsonProcessingException e) {
            log.error("encode message failed");
        }
        outputBuf.writeInt(array.length);
        outputBuf.writeBytes(array);
    }
    
    private int readFrameLength(ByteBuf buf) {
        return buf.readInt();
    }
    
    private Message readFrame(ByteBuf buf) {
        byte[] array;
        int offset;
        if (buf.hasArray()) {
            array = buf.array();
            offset = buf.arrayOffset();
        } else {
            array = ByteBufUtil.getBytes(buf, 0, buf.readableBytes(), false);
            offset = 0;
        }
        try {
            return objectMapper.readValue(array, Message.class);
        } catch (IOException e) {
            return new Message(null);
        }
        
    }
}
