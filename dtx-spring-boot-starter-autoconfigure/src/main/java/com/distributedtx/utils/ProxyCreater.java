package com.distributedtx.utils;

import aj.org.objectweb.asm.ClassReader;
import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.ClassWriter;
import aj.org.objectweb.asm.FieldVisitor;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import com.distributedtx.annotation.DtxParam;
import com.distributedtx.annotation.DtxTransactional;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 根据 com.distributedtx.annotation.DtxTransactional 注解，找到需要代理的类，
 * 在内存中创建一个类去继承此类，然后将这个类加载到spring容器中
 */
public class ProxyCreater {
    // 要扫描解析的包名
    private String basePackage;
    private static String proxyClassSuffix = "$DtxProxy";

    //asm 基本类型参数描述对应关系 desc
    private static Map<String, String> paramTypeNameMap = new HashMap<>();
    static{
        paramTypeNameMap.put("boolean", "Z");
        paramTypeNameMap.put("char", "C");
        paramTypeNameMap.put("byte", "B");
        paramTypeNameMap.put("short", "S");
        paramTypeNameMap.put("int", "I");
        paramTypeNameMap.put("float", "F");
        paramTypeNameMap.put("long", "J");
        paramTypeNameMap.put("double", "D");
        paramTypeNameMap.put("[Z", "[Z");
        paramTypeNameMap.put("[[Z", "[[Z");
        paramTypeNameMap.put("[C", "[C");
        paramTypeNameMap.put("[[C", "[[C");
        paramTypeNameMap.put("[B", "[B");
        paramTypeNameMap.put("[[B", "[[B");
        paramTypeNameMap.put("[S", "[S");
        paramTypeNameMap.put("[[S", "[[S");
        paramTypeNameMap.put("[I", "[I");
        paramTypeNameMap.put("[[I", "[[I");
        paramTypeNameMap.put("[F", "[F");
        paramTypeNameMap.put("[[F", "[[F");
        paramTypeNameMap.put("[J", "[J");
        paramTypeNameMap.put("[[J", "[[J");
        paramTypeNameMap.put("[D", "[D");
        paramTypeNameMap.put("[[D", "[[D");
    }

    public ProxyCreater(String basePackage) {
        this.basePackage = basePackage;
    }

    public void initDtxProxy() throws Exception {

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        List<Map<Class, List<Method>>> dtxClass = new ArrayList<>();
        Enumeration<URL> resources = contextClassLoader.getResources(basePackage.replace(".", "/"));
        while (resources.hasMoreElements()) {
            String packagePath = URLDecoder.decode(resources.nextElement().getFile(), "UTF-8");
            getDtxClassMap(basePackage, packagePath, dtxClass);
        }

        ApplicationContext applicationContext = ApplicationContextUtil.applicationContext;

        for(Map<Class, List<Method>> classListMap:dtxClass){
            for (Map.Entry<Class, List<Method>> classListEntry : classListMap.entrySet()) {
                Class clazz = classListEntry.getKey();
                String beanName = clazz.getSimpleName();
                beanName = beanName.replaceFirst(beanName.substring(0, 1), beanName.substring(0, 1).toLowerCase());
                byte[] bytes = addDtxProxyClass(classListEntry.getKey(), classListEntry.getValue());

                /*String fileName = System.getProperty("user.dir") + "/target/classes/"+clazz.getName().replace(".", "/")+proxyClassSuffix+".class";
                File file = new File(fileName);
                if(!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }
                if(!file.exists()){
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();*/

                Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
                defineClass.setAccessible(true); //关闭安全检查
                Class newClass = (Class) defineClass.invoke(contextClassLoader, clazz.getName()+proxyClassSuffix, bytes, 0, bytes.length);

                // spring 注册bean
                ApplicationContextUtil.registerBean(true, beanName, newClass, new Object[]{});

            }
        }
    }


    // 这里并没有考虑 jar 的加载
    private void getDtxClassMap(String basePackage, String packagePath, List<Map<Class, List<Method>>> dtxClass) throws Exception {
        File dir = new File(packagePath);

        if(!dir.exists() || !dir.isDirectory()){
            return ;
        }

        String fileName;
        for(File f : dir.listFiles()){
            if(f.isDirectory()){
                getDtxClassMap(basePackage+"."+f.getName(), f.getAbsolutePath(), dtxClass);
            }else{
                fileName = f.getName();
                if(fileName.endsWith(".class")){
                    //判断有无分布式事物注解
                    String className = fileName.substring(0, fileName.length() - 6);
//                    System.out.println(basePackage+"."+className);
                    resolveDtxAnnotation(Class.forName(basePackage+"."+className), dtxClass);
                }
            }
        }
    }

    // 只取public的方法
    public void resolveDtxAnnotation(Class clazz, List<Map<Class, List<Method>>> dtxClass){
        List<Method> list = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if(method.getAnnotation(DtxTransactional.class) != null){
                list.add(method);
            }
        }
        if(list.size()>0){
            Map<Class, List<Method>> map = new HashMap<>();
            map.put(clazz, list);
            dtxClass.add(map);
        }
    }

    /**
     * 内存中创建一个字节码文件去继承目标类
     *  ILOAD,  boolean,byte,char,short,int
     *  LLOAD,  long
     *  FLOAD,  float
     *  DLOAD,  double
     *  ALOAD,  Object
     */
    public byte[] addDtxProxyClass(Class clazz, List<Method> methods) throws IOException {
//        String classFullPath = "com/provider/service/LeaveBillService";
        String classFullPath = clazz.getName().replace(".", "/");
        String proxyFullPath = classFullPath+proxyClassSuffix;
        ClassVisitor visitor = new ClassWriter(0);
        String classPath = "";

        //加类
        visitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, proxyFullPath, null, classFullPath, null);
        visitor.visitAnnotation("Lorg/springframework/stereotype/Service;", true).visitEnd();

        //加属性
        FieldVisitor providerTaskMapper = visitor.visitField(Opcodes.ACC_PRIVATE, "providerTaskMapper", "Lcom/distributedtx/mapper/ProviderTaskMapper;", null, null);
        providerTaskMapper.visitAnnotation("Lorg/springframework/beans/factory/annotation/Autowired;", true);
        providerTaskMapper.visitEnd();
        //getter
        MethodVisitor providerTaskMapperGetter = visitor.visitMethod(Opcodes.ACC_PUBLIC, "getProviderTaskMapper", "()Lcom/distributedtx/mapper/ProviderTaskMapper;", null, null);
        providerTaskMapperGetter.visitCode();
        providerTaskMapperGetter.visitVarInsn(Opcodes.ALOAD, 0);
        providerTaskMapperGetter.visitMethodInsn(Opcodes.GETFIELD, proxyFullPath, "providerTaskMapper", "Lcom/distributedtx/mapper/ProviderTaskMapper;", false);
        providerTaskMapperGetter.visitInsn(Opcodes.ARETURN);
        providerTaskMapperGetter.visitMaxs(1, 1);
        providerTaskMapperGetter.visitEnd();
        //setter
        MethodVisitor providerTaskMapperSetter = visitor.visitMethod(Opcodes.ACC_PUBLIC, "setProviderTaskMapper", "(Lcom/distributedtx/mapper/ProviderTaskMapper;)V", null, null);
        providerTaskMapperSetter.visitCode();
        providerTaskMapperSetter.visitVarInsn(Opcodes.ALOAD, 0);
        providerTaskMapperSetter.visitVarInsn(Opcodes.ALOAD, 1);
        providerTaskMapperSetter.visitMethodInsn(Opcodes.PUTFIELD, proxyFullPath, "providerTaskMapper", "Lcom/distributedtx/mapper/ProviderTaskMapper;", false);
        providerTaskMapperSetter.visitInsn(Opcodes.RETURN);
        providerTaskMapperSetter.visitMaxs(2, 2);
        providerTaskMapperSetter.visitEnd();

        //加构造函数
        MethodVisitor initMethod = visitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        initMethod.visitCode();
        initMethod.visitVarInsn(Opcodes.ALOAD, 0);
        initMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, classFullPath, "<init>", "()V", false);
        initMethod.visitInsn(Opcodes.RETURN);
        initMethod.visitMaxs(1, 1);
        initMethod.visitEnd();

        //加方法，读取原方法的元信息，重写
        ClassReader reader = new ClassReader(clazz.getName());
        ASMClassPrinter cp = new ASMClassPrinter();
        reader.accept(cp, 0);
        List<Map<String, Object>> methodsMetadata = cp.getMethodsMetadata();
        for (Method method : methods){
            String methodName = method.getName();
            int parameterCount = method.getParameterCount();
            if(parameterCount == 0){
                throw new RuntimeException("没有dtx参数，请使用com.distributedtx.domain.CommonParam["+method.getName()+"]");
            }
            Map<String, Object> methodMd = getMethodMetadata(method, methodsMetadata);
            Assert.notNull(methodMd, "没有找到方法描述["+clazz.getName()+"."+method.getName()+"]");

            Parameter[] parameters = method.getParameters();
            Map<String, Object> dtxParamMap = getDtxParamEntity(method, parameters);
            int dtxIndex = (int) dtxParamMap.get("dtxIndex");
            Parameter dtxParameter = (Parameter) dtxParamMap.get("dtxParam");

            MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, methodName, (String)methodMd.get("desc"), (String)methodMd.get("signature"), (String[]) methodMd.get("exceptions"));
            methodVisitor.visitAnnotation("Ljava/lang/Override;", true);
            methodVisitor.visitAnnotation("Lorg/springframework/transaction/annotation/Transactional;", true);
            methodVisitor.visitCode();

            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, proxyFullPath, "providerTaskMapper", "Lcom/distributedtx/mapper/ProviderTaskMapper;");
            methodVisitor.visitVarInsn(Opcodes.ALOAD, dtxIndex+1);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, dtxParameter.getType().getName().replace(".", "/"), "getMsgId", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/distributedtx/mapper/ProviderTaskMapper", "updateDone", "(Ljava/lang/String;)I", true);
            methodVisitor.visitInsn(Opcodes.POP);

            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0); //this
            int i = 1;
            for (Parameter parameter : parameters) {
                switch (parameter.getType().getName()){
                    case "boolean":
                    case "byte":
                    case "char":
                    case "short":
                    case "int":
                        methodVisitor.visitVarInsn(Opcodes.ILOAD, i);
                        i += 1;
                        break;
                    case "float":
                        methodVisitor.visitVarInsn(Opcodes.FLOAD, i);
                        i += 1;
                        break;
                    case "long":
                        methodVisitor.visitVarInsn(Opcodes.LLOAD, i);
                        i += 2;
                        break;
                    case "double":
                        methodVisitor.visitVarInsn(Opcodes.DLOAD, i);
                        i += 2;
                        break;
                    default:
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, i);
                        i += 1;
                }
            }
//            methodVisitor.visitVarInsn(Opcodes.ALOAD, dtxIndex+1);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, classFullPath, methodName, (String)methodMd.get("desc"), false);

            //根据方法描述返回
            String returnType = (String)methodMd.get("desc");
            returnType = returnType.substring(returnType.indexOf(")")+1);
            switch (returnType){
                case "I":
                case "S":
                case "B":
                case "C":
                case "Z":
                    methodVisitor.visitInsn(Opcodes.IRETURN);
                    break;
                case "F":
                    methodVisitor.visitInsn(Opcodes.FRETURN);
                    break;
                case "J":
                    methodVisitor.visitInsn(Opcodes.LRETURN);
                    break;
                case "D":
                    methodVisitor.visitInsn(Opcodes.DRETURN);
                    break;
                case "V":
                    methodVisitor.visitInsn(Opcodes.RETURN);
                    break;
                default:
                    methodVisitor.visitInsn(Opcodes.ARETURN);
            }
//            methodVisitor.visitInsn(Opcodes.IRETURN);
            methodVisitor.visitMaxs(parameterCount+2, parameterCount+2);

            methodVisitor.visitEnd();
        }
        visitor.visitEnd();
        return ((ClassWriter) visitor).toByteArray();
    }

    /**
     * 没有重名取第一个，如果重名，则计算desc 对比
     * @param method
     * @param metadatas
     * @return
     */
    private Map<String, Object> getMethodMetadata(Method method, List<Map<String, Object>> metadatas){
        metadatas = metadatas.stream().filter(m -> method.getName().equals(m.get("name"))).collect(Collectors.toList());
        Assert.notEmpty(metadatas, "没有找到方法描述["+method.getName()+"]");

        if(metadatas.size() == 1){
            return metadatas.get(0);
        }

        // 计算 desc
        StringBuilder descriper = new StringBuilder("(");
        for (Parameter parameter : method.getParameters()) {
            String typeName = parameter.getType().getName();
            String descName = paramTypeNameMap.get(typeName);
            if(descName!=null){
                descriper.append(descName);
            }else if(parameter.getType().isArray()){
                descriper.append(typeName.replace(".", "/"));
            }else{
                descriper.append("L").append(typeName.replace(".", "/")).append(";");
            }
        }
        descriper.append(")");
        System.out.println(descriper.toString());

        Map<String, Object> md = null;
        String desc = null;
        //(Lcom/provider/domain/LeaveBill;Ljava/lang/String;DZ[I[C[Ljava/lang/Object;)I
        //(Lcom/provider/domain/LeaveBill;DLjava/lang/String;Z[I[C[Ljava/lang/Object;)I
        for (Map<String, Object> metadata : metadatas) {
            desc = (String)metadata.get("desc");
            if(desc.indexOf(descriper.toString()) > -1){
                md = metadata;
                break;
            }
        }
        return md;
    }

    /**
     * 如果没有DtxParam参数注解，则默认取第一个，如果第一个没有 getMsgId 方法，则在运行时报错
     * @param method
     * @param parameters
     * @return
     */
    private Map<String, Object> getDtxParamEntity(Method method, Parameter[] parameters){
        Map<String, Object> map = new HashMap<>();
        map.put("dtxIndex", 0);
        map.put("dtxParam", parameters[0]);
        if(parameters.length == 1){
            return map;
        }
        for(int i=0; i<parameters.length; i++){
            if(parameters[i].getAnnotation(DtxParam.class) != null){
                map.put("dtxIndex", i);
                map.put("dtxParam", parameters[i]);
                break;
            }
        }
        return map;
    }
}
