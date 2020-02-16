package com.distributedtx.utils;

import aj.org.objectweb.asm.AnnotationVisitor;
import aj.org.objectweb.asm.Attribute;
import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.FieldVisitor;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import aj.org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取 class 字节码 内容
 */
public class ASMClassPrinter extends ClassVisitor {
    private List<Map<String, Object>> methodsMetadata = new ArrayList<>();

    public ASMClassPrinter() {
        super(Opcodes.ASM7);
    }
    public ASMClassPrinter(ClassVisitor cv) {
        super(Opcodes.ASM7, cv);
    }
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println("version:"+version+" access:"+access+" name:"+name+" signature:"+signature+" superName:"+ superName + " interfaces:"+interfaces+ " {");
    }
    public void visitSource(String source, String debug) {
        System.out.println("source:"+source+" debug:"+debug);
    }
    public void visitOuterClass(String owner, String name, String desc) {
        System.out.println("owner:"+owner+" name:"+name+" desc:"+desc);
    }
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        System.out.println("desc:"+desc + " visible:" + visible);
        return null;
    }
    public void visitAttribute(Attribute attr) {
        System.out.println("attr:"+attr);
    }
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        System.out.println("typeRef:"+typeRef+" typePath:"+typePath+" descriptor:"+descriptor+" visible:"+visible);
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        System.out.println("access:"+access+" name:"+name+" desc:"+desc+" signature:"+signature+" value:"+value);
        return null;
    }
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("access:"+access+" name:"+name +" desc:"+ desc+" access:"+signature+" exceptions:"+exceptions);
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("desc", desc);
        map.put("signature", signature);
        map.put("exceptions", exceptions);
        methodsMetadata.add(map);
        return null;
    }
    public void visitEnd() {
        System.out.println("}");
    }

    public List<Map<String, Object>> getMethodsMetadata() {
        return methodsMetadata;
    }
}