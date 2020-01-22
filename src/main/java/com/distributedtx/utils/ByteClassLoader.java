package com.distributedtx.utils;

/**
 * 类加载器，加载字节码
 */
public class ByteClassLoader extends ClassLoader {

    public Class defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}