package com.example.demo.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fupeng-ds on 2018/6/28.
 */
public class Cache {

    private static final ConcurrentHashMap<String, byte[]> map = new ConcurrentHashMap<>();

    private static Kryo kryo = new Kryo();

    static {
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
    }

    public static void set(String key, Object value, int expire) {
        map.put(key, objToByte(value));
    }

    public static void set(String key, String field, Object value, int expire) {
        ConcurrentHashMap<String, byte[]> fap = new ConcurrentHashMap<>();
        fap.put(field, objToByte(value));
        map.put(key, objToByte(fap));
    }

    public static <T> T get(String key, Class<T> t) {
        byte[] bytes = map.get(key);
        return byteToObj(bytes, t);
    }

    public static <T> T get(String key, String field, Class<T> t) {
        byte[] bytes = map.get(key);
        ConcurrentHashMap fap = byteToObj(bytes, ConcurrentHashMap.class);
        byte[] b = (byte[]) fap.get(field);
        return byteToObj(b, t);
    }

    public static void del(String key) {
        map.remove(key);
    }

    public static <T> T byteToObj(byte[] buffer, Class<T> type) {
        Input input = null;
        try {
            input = new Input(buffer);
            return kryo.readObject(input, type);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    public static <T> byte[] objToByte(T t) {
        Output output = null;
        try {
            output = new Output(new ByteArrayOutputStream());
            kryo.writeObject(output, t);
            return output.toBytes();
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

}
