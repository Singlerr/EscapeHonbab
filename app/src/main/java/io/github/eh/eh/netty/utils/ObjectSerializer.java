package io.github.eh.eh.netty.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ObjectSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static ByteBuf writeAsByteBuf(Object object) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);

        byte[] bs = bos.toByteArray();

        ByteBuf byteBuf = Unpooled.directBuffer();
        byteBuf.writeBytes(bs);

        return byteBuf;
    }

    public static ByteBuf writeJsonAsByteBuf(Object object) throws Exception {
        byte[] bs = mapper.writeValueAsBytes(object);
        ByteBuf byteBuf = Unpooled.directBuffer();
        byteBuf.writeBytes(bs);
        return byteBuf;
    }

    public static Object readJsonByteBufAsObject(ByteBuf byteBuf, Class<?> classType) throws IOException {
        int len = byteBuf.readableBytes();
        byte[] read = new byte[len];
        for (int i = 0; i < len; i++) {
            read[i] = byteBuf.getByte(i);
        }
        return mapper.readValue(read, classType);
    }

    public static Object readAsObject(ByteBuf byteBuf) throws Exception {
        int len = byteBuf.readableBytes();
        byte[] read = new byte[len];
        for (int i = 0; i < len; i++) {
            read[i] = byteBuf.getByte(i);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(read);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
    }
}
