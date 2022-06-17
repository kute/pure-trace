package com.kute.pureagent.javassist;

import com.kute.pureagent.javassist.source.IEvent;
import com.kute.pureagent.javassist.source.IEventInvoker;
import com.kute.pureagent.javassist.source.NormalBeanService;
import com.kute.pureagent.javassist.source.StartEvent;
import com.kute.pureagent.junitrules.MyRule;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * created by kute at 2022/5/6 上午11:11
 */
@Slf4j
public class JavassistTest {

    static {
        // 适配Tomcat，因为Tomcat不是用的默认的类加载器，而Javaassist用的是默认的加载器
        Class<?>[] classArray = new Class<?>[]{
                MyRule.class
        };

        ClassPool classPool = ClassPool.getDefault();

        for (Class<?> clazz : classArray) {
            if (classPool.find(clazz.getCanonicalName()) == null) {
                ClassClassPath classPath = new ClassClassPath(clazz);
                classPool.insertClassPath(classPath);
            }
        }
    }

    ClassPool classPool = null;

    @Before
    public void before() {
        classPool = ClassPool.getDefault();
    }

    /**
     * 展示 如何通过字节码生成 来调用 NormalBeanService.dealEvent 方法，而不是通过反射
     *
     * @throws NoSuchMethodException
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IOException
     */
    @Test
    public void test() throws NoSuchMethodException, NotFoundException, CannotCompileException,
            IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        // 准备数据
        NormalBeanService bean = new NormalBeanService();
        StartEvent startEvent = new StartEvent();
        startEvent.setApp("kute");
        Method method = bean.getClass().getDeclaredMethod("dealEvent", StartEvent.class);
        Class<?> eventClass = startEvent.getClass();

        // 创建一个实现了 IEventInvoker 接口的类
        String suffix = RandomString.make(5);
        CtClass ctClass = classPool.makeClass(IEventInvoker.class.getCanonicalName() + "$" + suffix);
        ctClass.addInterface(classPool.get(IEventInvoker.class.getCanonicalName()));
        ConstPool constPool = ctClass.getClassFile().getConstPool();

        // 类上添加多个注解 @Slf4j， @Service
        AnnotationsAttribute classAnnotationAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        classAnnotationAttribute.addAnnotation(new Annotation(Slf4j.class.getCanonicalName(), constPool));

        Annotation serviceAnnotation = new Annotation("org.springframework.stereotype.Service", constPool);
        String beanName = "eventInvoker$" + suffix + "Service";
        serviceAnnotation.addMemberValue("value", new StringMemberValue(beanName, constPool));
        classAnnotationAttribute.addAnnotation(serviceAnnotation);
        ctClass.getClassFile().addAttribute(classAnnotationAttribute);

        // 定义一个bean的成员
        CtField ctFieldBean = new CtField(classPool.get(bean.getClass().getCanonicalName()), "bean", ctClass);
        ctFieldBean.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
        ctClass.addField(ctFieldBean);

        // 定义一个普通变量 name
        CtField ctField = new CtField(classPool.get(String.class.getCanonicalName()), "name", ctClass);
        ctField.setModifiers(Modifier.PRIVATE);
        // 变量上添加注解
        AnnotationsAttribute fieldAnnotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        fieldAnnotationsAttribute.addAnnotation(new Annotation(Deprecated.class.getCanonicalName(), constPool));
        ctField.getFieldInfo().addAttribute(fieldAnnotationsAttribute);
        ctClass.addField(ctField);

        // getter and setter, CtNewMethod工具类方法，快速创建
        ctClass.addMethod(CtNewMethod.getter("getName", ctField));
        ctClass.addMethod(CtNewMethod.setter("setName", ctField));

        // 定义bean的构造函数
        CtConstructor ctConstructor = new CtConstructor(classPool.get(new String[]{bean.getClass().getCanonicalName()}), ctClass);
        // $0 表示this，其他依次表示各个参数
        ctConstructor.setBody("{this.bean = $1;}");
        ctConstructor.setModifiers(Modifier.PUBLIC);
        ctClass.addConstructor(ctConstructor);

        // 添加默认构造函数
        ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass));

        // 定义name的构造函数
        CtConstructor nameConstructor = CtNewConstructor.make(new CtClass[]{
                classPool.get(String.class.getCanonicalName())
        }, new CtClass[]{
                classPool.get(IllegalArgumentException.class.getCanonicalName())
        }, "{this.name = $1;}", ctClass);
        ctClass.addConstructor(nameConstructor);

        // 实现 IEventInvoker 接口的方法
        CtMethod ctMethod = new CtMethod(classPool.get(void.class.getCanonicalName()),
                "invoke",
                classPool.get(new String[]{IEvent.class.getCanonicalName()}),
                ctClass);
        ctMethod.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        // 方法实现 调用 原先bean的method
        String invokeMethodBody = "{this.bean." + method.getName() + "((" + eventClass.getCanonicalName() + ")$1);}";// 强制类型转换，转换为具体的Event类型的类型
        ctMethod.setBody(invokeMethodBody);

        // 方法上添加注解 @Override
        AnnotationsAttribute methodAnnotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        methodAnnotationsAttribute.addAnnotation(new Annotation(Override.class.getCanonicalName(), constPool));
        ctMethod.getMethodInfo().addAttribute(methodAnnotationsAttribute);
        // 方法的形参上添加注解
        ParameterAnnotationsAttribute parameterAnnotationsAttribute = new ParameterAnnotationsAttribute(constPool, ParameterAnnotationsAttribute.visibleTag);
        Annotation[][] annotations = new Annotation[1][2];
        annotations[0][0] = new Annotation(NonNull.class.getCanonicalName(), constPool);
        annotations[0][1] = new Annotation(RequestParam.class.getCanonicalName(), constPool);
        // 二维数组，一维表示参数个数，二维表示每个参数的多个注解
        parameterAnnotationsAttribute.setAnnotations(annotations);
        ctMethod.getMethodInfo().addAttribute(parameterAnnotationsAttribute);

        ctClass.addMethod(ctMethod);

        // 将class文件保存
        ctClass.writeFile("./src/test/java");

        // 转成字节数组
        byte[] bytes = ctClass.toBytecode();

        // 从classPoll中释放引用
        ctClass.detach();

        // 通过前面定义的构造函数创建实例对象
        Class<?> invokerClass = ctClass.toClass();
        Constructor<?> constructor = invokerClass.getConstructor(bean.getClass());
        IEventInvoker invoker = (IEventInvoker) constructor.newInstance(bean);

        // 发起调用
        invoker.invoke(startEvent);
    }

    /**
     * 解冻
     * 添加额外的类搜索路径
     * 导入新的包
     *
     * @throws Exception
     */
    @Test
    public void test2() throws Exception {
        // 通过 classPool 加载的CtClass 可以被修改
        CtClass ctClass = classPool.getOrNull("com.kute.pureagent.javassist.source.StartEvent");

        log.info("{}", ctClass.toBytecode().length);

        Class<?> clazz = ctClass.toClass();
        log.info(clazz.getCanonicalName());
        Assert.assertEquals(clazz, StartEvent.class);

        CtField appField = ctClass.getField("app");
        Assert.assertNotNull(appField);
        log.info("{}", appField);

//        ctClass.writeFile("./src/test/java");

        // 如果一个 CtClass 对象通过 writeFile(), toClass(), toBytecode() 被调用，则不允许修改，此时可以解冻
        ctClass.defrost(); // 解冻后，就可以修改了
        ctClass.setSuperclass(classPool.get(NormalBeanService.class.getCanonicalName()));
        ctClass.writeFile("./src/test/java");

        // ClassPool.getDefault() 获取的 ClassPool 使用 JVM 的类搜索路径
        // 如果程序运行在 JBoss 或者 Tomcat 等 Web 服务器上，ClassPool 可能无法找到用户的类，因为 Web 服务器使用多个类加载器作为系统类加载器，
        // 需要添加额外的类搜索路径
        classPool.insertClassPath(new ClassClassPath(this.getClass()));
        // 搜索域名中的类
        classPool.insertClassPath(new URLClassPath("www.javassist.org", 80, "/java/", "org.javassist."));
        classPool.insertClassPath("/usr/local/javalib");
        // 导入新的package
        classPool.importPackage("");

        // 将 ctClass 从classPoll中移出，避免内存溢出
        ctClass.detach();

    }

    /**
     * 监听器 Translator
     * Loader 接口加载
     *
     * @throws Exception
     */
    @Test
    public void test3() throws Throwable {
        String className = "com.kute.pureagent.javassist.source.NormalBeanService";
        CtClass ctClass = classPool.get(className);
        // 修改接口
        ctClass.setInterfaces(new CtClass[]{classPool.getOrNull("java.io.Serializable")});

        Class<?> clazz = ctClass.toClass();
        NormalBeanService beanService = (NormalBeanService) clazz.newInstance();
        beanService.dealEvent(new StartEvent());

        Assert.assertTrue(Serializable.class.isAssignableFrom(clazz));

        // 此时加载的是修改过的class，实现了Serializable接口
        Loader loader = new Loader(classPool);
        // 添加监听器
        loader.addTranslator(classPool, new Translator() {
            // 当事件监听器通过 addTranslator() 添加到 javassist.Loader 对象时，start() 方法会被调用
            @Override
            public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
                log.info("Loader start");
            }

            // 在 javassist.Loader 加载类之前，会调用 onLoad() 方法。可以在 onLoad() 方法中修改被加载的类的定义
            @Override
            public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
                log.info("onLoad for classname={}", classname);
                if (!className.equals(classname)) {
                    return;
                }
                CtClass cc = pool.get(classname);
                if (cc.isFrozen()) {
                    cc.defrost();
                }
                cc.setModifiers(Modifier.FINAL + Modifier.PUBLIC);
            }
        });
        Class<?> clazz2 = loader.loadClass(className);

        Assert.assertTrue(Serializable.class.isAssignableFrom(clazz2));
        Assert.assertEquals(clazz2.getModifiers(), Modifier.FINAL + Modifier.PUBLIC);
    }

    /**
     * 1、语句块示例，每行都是有效的
     * System.out.println("Hello");
     * { System.out.println("Hello"); }
     * if (i < 0) { i = -i; }
     * <p>
     * 2、编译的方法标识符扩展符号，可以用在语句块中
     * <p>
     * $0, $1, $2, ...	this and 方法的参数，$0表示 this
     * $args	方法参数数组.它的类型为 Object[]
     * $$	所有实参。例如, m($$) 等价于 m($1,$2,...)
     * $cflow(...)	cflow 变量
     * $r	返回结果的类型，用于强制类型转换
     * $w	包装器类型，用于强制类型转换
     * $_	返回值
     * $sig	类型为 java.lang.Class 的参数类型数组
     * $type	一个 java.lang.Class 对象，表示返回值类型
     * $class	一个 java.lang.Class 对象，表示当前正在修改的类
     *
     * @throws Exception
     */
    @Test
    public void test4() throws Exception {
        CtClass ctClass = classPool.get("com.kute.pureagent.javassist.source.NormalBeanService");
        if (ctClass.isFrozen()) {
            ctClass.defrost();
        }
        CtMethod ctMethod = ctClass.getDeclaredMethod("dealEvent");

        // 方法执行前加入逻辑，打印第一个参数, insertBefore只接受单个语句或者{}括起来的语句块
        ctMethod.insertBefore("{System.out.println(\"javassist before \" + $1);}");
        // 指定
        ctMethod.insertAt(0, "{System.out.println(\"javassist at\");}");
        // 之后
        ctMethod.insertAfter("{System.out.println(\"javassist after\");}", true);
//        ctMethod.insertAfter("{System.out.println(\"javassist after\");}");

        // 插入的代码片段必须以 throw 或 return 语句结束
        ctMethod.addCatch("{ System.out.println($e); throw $e; }", classPool.get("java.io.IOException"));

        CtMethod m = CtNewMethod.make(
                "public int xmove(int dx) {com.kute.pureagent.javassist.source.JustMain.print();return dx;}",
                ctClass);
        ctClass.addMethod(m);

        ctClass.writeFile(".");

        Class<?> clazz = ctClass.toClass();
        NormalBeanService beanService = (NormalBeanService) clazz.newInstance();
        beanService.dealEvent(new StartEvent());

        Method printMethod = beanService.getClass().getMethod("xmove", int.class);
        printMethod.invoke(beanService, 11);
    }

    /**
     * 读取解析 class 文件
     *
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        String classFilePath = "/Users/kute/work/ideawork/pure-trace/pure-agent/src/test/java/com/kute/pureagent/javassist/source/IEventInvoker$2SvMr.class";
        try (DataInputStream dis = new DataInputStream(new FileInputStream(new File(classFilePath)))) {
            ClassFile classFile = new ClassFile(dis);
            // JDK编译版本
            int minorVersion = classFile.getMajorVersion();
            System.out.println("获取当前的class文件的jdk编译版本信息:" + minorVersion);
            // 实现的接口名称列表
            String[] interfaces = classFile.getInterfaces();
            System.out.println("当前classFile中的所有的接口名称为：" + Arrays.toString(interfaces));
            System.out.println("开始输出常量池中的内容：");
            ConstPool constPool = classFile.getConstPool();
            // 获取常量池内容
            Set<String> classNames = constPool.getClassNames();
            System.out.println("classNames=" + classNames);
            boolean isInterface = classFile.isInterface();
            System.out.println("当前的这个类是否为接口？" + isInterface);
            // 获取这个类上面的注解属性
            List<AttributeInfo> attributes = classFile.getAttributes();
            if (attributes == null || attributes.isEmpty()) {
                System.out.println("没有任何的属性！");
            }
            for (AttributeInfo attributeInfo : attributes) {
                // 获取注解属性
                if (attributeInfo instanceof AnnotationsAttribute) {
                    AnnotationsAttribute attr = (AnnotationsAttribute) attributeInfo;
                    Annotation[] annotations = attr.getAnnotations();
                    System.out.println("解析获得注解：" + Arrays.toString(annotations));
                } else if (attributeInfo instanceof DeprecatedAttribute) {
                    // 获取DeprecatedAttribute属性
                    DeprecatedAttribute attr = (DeprecatedAttribute) attributeInfo;
                    System.out.println("attr=" + attr.tag);
                } else if (attributeInfo instanceof SourceFileAttribute) {
                    // 获取源文件属性
                    SourceFileAttribute attr = (SourceFileAttribute) attributeInfo;
                    String fileName = attr.getFileName();
                    System.out.println("fileName=" + fileName);
                }
            }
            // 由于获取了注解所以可以使用各种其他的操作

            // 获取当前的方法
            System.out.println("输出当前的所有的方法：");
            List<MethodInfo> methods = classFile.getMethods();
            if (methods == null || methods.isEmpty()) {
                System.out.println("没有任何方法.....");
            } else {
                for (MethodInfo methodInfo : methods) {
                    System.out.println(methodInfo.getName() + "," + methodInfo.getDescriptor());
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void test6() throws NotFoundException {
        CtClass ctClass = classPool.getOrNull("com.kute.pureagent.javassist.source.MyService");
        CtMethod ctMethod = ctClass.getDeclaredMethod("generateStartEvent");

        // 方法名 : generateStartEvent
        String methodName = ctMethod.getName();
        System.out.println(methodName);
        // 返回类型
        CtClass returnType = ctMethod.getReturnType();
        System.out.println(returnType);
        // 方法参数，通过此种方式得到方法参数列表 格式：com.kute.pureagent.javassist.source.MyService.generateStartEvent(java.lang.Integer,com.kute.pureagent.javassist.source.IEvent)
        System.out.println(ctMethod.getLongName());
        // 方法签名 格式：(Ljava/lang/Integer;Lcom/kute/pureagent/javassist/source/IEvent;)Lcom/kute/pureagent/javassist/source/StartEvent;
        System.out.println(ctMethod.getSignature());

        // 获取方法参数名称，可以通过这种方式得到方法真实参数名称
        List<String> argKeys = new ArrayList<>();
        MethodInfo methodInfo = ctMethod.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        int len = ctMethod.getParameterTypes().length;
        // 非静态的成员函数的第一个参数是this
        int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;
        for (int i = pos; i < len; i++) {
            argKeys.add(attr.variableName(i));
        }
        System.out.println(len);
        System.out.println(attr.tableLength());
        System.out.println(argKeys);
    }

}
