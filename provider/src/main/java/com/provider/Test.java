package com.provider;

import aj.org.objectweb.asm.AnnotationVisitor;
import aj.org.objectweb.asm.Attribute;
import aj.org.objectweb.asm.ClassReader;
import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.ClassWriter;
import aj.org.objectweb.asm.FieldVisitor;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import aj.org.objectweb.asm.TypePath;
import com.distributedtx.annotation.DtxParam;
import com.distributedtx.annotation.DtxTransactional;
import com.distributedtx.utils.ASMClassPrinter;
import com.distributedtx.utils.ProxyCreater;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) throws Exception {
        String basePackage = "com.provider";
//        ProxyCreater proxyCreater = new ProxyCreater(basePackage);
//        proxyCreater.initDtxProxy();

//        new Test().addClass();
//        new Test().accessClass(classPath);
//        new Test().readClass("com.provider.service.LeaveBillService");
    }

    public void accessClass(String classPath) throws IOException {
        ClassReader reader = new ClassReader(classPath);
        ClassVisitor visitor = new ClassWriter(reader, 0);
        reader.accept(visitor, 0);

        byte[] bytes = ((ClassWriter) visitor).toByteArray();
        String fileName = System.getProperty("user.dir") + "/provider/target/ExtendProxy.class";
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
        fos.close();
    }

    public void readClass(String classPath) throws IOException {
        ClassReader reader = new ClassReader(classPath);
        ASMClassPrinter cp = new ASMClassPrinter();
        reader.accept(cp, 0);
        List<Map<String, Object>> methodsMetadata = cp.getMethodsMetadata();
        System.out.println(methodsMetadata);
    }

}
