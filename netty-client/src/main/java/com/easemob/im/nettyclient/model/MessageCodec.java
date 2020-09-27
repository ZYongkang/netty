package com.easemob.im.nettyclient.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author yongkang.zhang@easemob.com
 */
public class MessageCodec {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<Message> decode(ByteBuf buf) throws Exception {
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
    
    public void encode(Message message, ByteBuf outputBuf) throws JsonProcessingException {
        byte[] array = objectMapper.writeValueAsBytes(message);
        outputBuf.writeInt(array.length);
        outputBuf.writeBytes(array);
    }
    
    private int readFrameLength(ByteBuf buf) {
        return buf.readInt();
    }
    
    private Message readFrame(ByteBuf buf) throws IOException {
        byte[] array;
        int offset;
        if (buf.hasArray()) {
            array = buf.array();
            offset = buf.arrayOffset();
        } else {
            array = ByteBufUtil.getBytes(buf, 0, buf.readableBytes(), false);
            offset = 0;
        }
        return objectMapper.readValue(array, Message.class);
    }
}
