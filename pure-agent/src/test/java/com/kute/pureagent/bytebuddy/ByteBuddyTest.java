package com.kute.pureagent.bytebuddy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;
import org.junit.Assert;
import org.junit.Test;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * created by kute at 2022/4/14 下午7:24
 */
public class ByteBuddyTest {

    /**
     * 生成一个hello world类，继承Object，重写toString方法
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void test() throws IllegalAccessException, InstantiationException {
        Class<? extends Object> dynamicType = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                // 声明要继承的父类
                .subclass(Object.class)
                // 声明方法
                .method(ElementMatchers.isToString())
//                .method(ElementMatchers.named("toString"))
                // method的实现：返回固定的值
                .intercept(FixedValue.value("Hello World!"))
                // make 触发生成一个新的类，表现形式是  DynamicType.Unloaded
                .make()
                // 将新生成的类加载到 jvm中
                .load(this.getClass().getClassLoader())
                .getLoaded();

        assertEquals(dynamicType.newInstance().toString(), "Hello World!");
    }


    /**
     * 创建一个继承Foo.java的类，将方法代理到 Bar类，具体调用哪个方法，会根据根据方法签名、返回值类型、方法名、注解的顺序来匹配方法（越后面的优先级越高）
     * <p>
     * 有多个匹配时，通过 @BindingPriority 注解来声明优先级
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void test1() throws IllegalAccessException, InstantiationException {
        Class<? extends Foo> dynamicType = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                .subclass(Foo.class)
                .method(named("sayHelloFoo")
                        .and(isDeclaredBy(Foo.class))
                        .and(returns(String.class)))
                // 将方法代理到 Bar类
                .intercept(MethodDelegation.to(Bar.class))
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded();

        assertEquals(dynamicType.newInstance().sayHelloFoo(), Bar.sayHelloBar());
    }

    /**
     * 定义一个新类，定义新的方法、新的字段
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     */
    @Test
    public void test2() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        String newClassName = "Comment";
        String newMethodName = "getComment";
        String newFieldName = "status";

        Class<?> dynamicType = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                .subclass(Object.class)
                // 声明新创建的类的名称
                .name(newClassName)
                // 在新创建的类中定义一个新的方法: 返回类型 String，public修饰
                .defineMethod(newMethodName, String.class, Modifier.PUBLIC)
                // 委托给 Bar，可以使用注解进行一些参数的绑定
                .intercept(MethodDelegation.to(Bar.class))
                // 定义一个新的字段
                .defineField(newFieldName, Integer.class, Modifier.PUBLIC)
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded();

        assertEquals(dynamicType.newInstance().getClass().getSimpleName(), newClassName);
        Method method = dynamicType.getDeclaredMethod(newMethodName, null);
        assertEquals(method.invoke(dynamicType.newInstance(), null), Bar.sayHelloBar());
        assertNotNull(dynamicType.getDeclaredField(newFieldName));
    }

    /**
     * 重新定义一个已经存在的类，改变原有类的方法的行为，然后使用 buddy agent重新加载到jvm中
     */
    @Test
    public void test3() {
        ByteBuddyAgent.install();

        String newMethodValue = "sayHelloFoo new value";

        new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                .redefine(Foo.class)
                .method(named("sayHelloFoo"))
                .intercept(FixedValue.value(newMethodValue))
                .make()
                .load(Foo.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        Foo foo = new Foo();
        assertEquals(foo.sayHelloFoo(), newMethodValue);
    }

    /**
     * 新创建的类的 名称 的定制化
     * net.bytebuddy.NamingStrategy
     */
    @Test
    public void test4() {
        // 按默认命名策略创建
        // 未声明类名称时，默认策略是按照 net.bytebuddy.renamed 的前缀 以及 父类， 加 随机串来命名
        Class<?> dynamicType1 = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                .subclass(Object.class)
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded();
        // net.bytebuddy.renamed.java.lang.Object$ByteBuddy$9BiDNmwx
        System.out.println(dynamicType1.getName());

        // 通过name指定类名称
        Class<?> dynamicType2 = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                .subclass(Object.class)
                .name("Comment")
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded();
        // Comment
        System.out.println(dynamicType2.getName());

        // 定制策略
        Class<?> dynamicType3 = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                .with(new NamingStrategy.AbstractBase() {
                    @Override
                    protected String name(TypeDescription superClassType) {
                        return "i.love." + superClassType.getSimpleName();
                    }
                })
                .subclass(Object.class)
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded();
        // i.loveObject
        System.out.println(dynamicType3.getName());

        // 定制策略,后缀 加随机数
        Class<?> dynamicType4 = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                .with(new NamingStrategy.SuffixingRandom("i.love"))
                .subclass(Object.class)
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded();
        // net.bytebuddy.renamed.java.lang.Object$i.love$xfUTJDOt
        System.out.println(dynamicType4.getName());

        // 定制策略,前缀 加随机数
        Class<?> dynamicType5 = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                .with(new NamingStrategy.PrefixingRandom("i.love"))
                .subclass(Object.class)
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded();
        // i.love.java.lang.Object$8fUAol34
        System.out.println(dynamicType5.getName());
    }

    /**
     * 通过类的全限定名称解析类
     *
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        TypePool typePool = TypePool.Default.ofSystemLoader();
//        TypePool typePool = TypePool.Default.ofPlatformLoader();
//        TypePool typePool = TypePool.Default.ofBootLoader();

        String className = "com.kute.pureagent.bytebuddy.Foo";
        String newValue = "sssss";
        Class<?> dynamicType = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                // 通过名称解析类
                .subclass(typePool.describe(className).resolve())
                .method(named("sayHelloFoo"))
                .intercept(FixedValue.value(newValue))
                .defineField("qux", String.class, Modifier.PUBLIC)
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded();

        Method method = dynamicType.getDeclaredMethod("sayHelloFoo");

        assertNotNull(method);
        assertEquals(method.invoke(dynamicType.newInstance()), newValue);
        assertNotNull(dynamicType.getDeclaredField("qux"));
    }

    /**
     * 方法匹配
     *
     * @throws Exception
     */
    @Test
    public void test6() throws Exception {

        String methodName = "say";
        Class<?> dynamicType = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
                .subclass(Foo.class)

                // 指定名称，且 无参的方法
//                .method(named(methodName).and(takesNoArguments()))
                // 指定名称，且 有一个参数的方法
                .method(named(methodName).and(takesArguments(2)))
//                .method(named(methodName).and(takesArguments(1)))
                // 指定名称，第一个参数的类型是 String
//                .method(named(methodName).and(takesArgument(0, String.class)))
//                .method(named(methodName).and(takesArgument(0, TypeDescription.ForLoadedType.of(String.class))))
                // 指定名称，第一个参数的名称是 name
//                .method(named(methodName).and(takesArgument(0, named("name"))))
                // 指定名称，且 声明抛出 IllegalStateException异常
//                .method(named(methodName).and(canThrow(IllegalStateException.class)))

                // 根据方法签名， 匹配静态方法
//                .intercept(MethodDelegation.to(Target.class))
                // 方法也可以委托给 字段
//                .intercept(MethodDelegation.to("qux"))
//                .intercept(MethodDelegation.to("qux", String.class))
                .intercept(MethodDelegation.to(Target.class))
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded();

        Method method = dynamicType.getDeclaredMethod(methodName, String.class, String.class);
        assertEquals(method.invoke(dynamicType.newInstance(), "kute", "18"), "six 18kute");
    }

    /**
     * 代理创建
     *
     * @throws Exception
     */
    @Test
    public void test7() throws Exception {
        Instrumentation instrumentation = null;

        // 代理 DispatcherServlet的doDispatch 方法
        new AgentBuilder.Default()
//        new AgentBuilder.Default(new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8)))
                .type(named("org.springframework.web.servlet.DispatcherServlet"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                        return builder.method(named("doDispatch"))
                                .intercept(FixedValue.value("new value"));
                    }
                })
                .with(new AgentBuilder.Listener.Adapter() {
                    @Override
                    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                        super.onDiscovery(typeName, classLoader, module, loaded);
                    }

                    @Override
                    public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
                        super.onTransformation(typeDescription, classLoader, module, loaded, dynamicType);
                    }

                    @Override
                    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
                        super.onIgnored(typeDescription, classLoader, module, loaded);
                    }

                    @Override
                    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
                        super.onError(typeName, classLoader, module, loaded, throwable);
                    }

                    @Override
                    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                        super.onComplete(typeName, classLoader, module, loaded);
                    }
                })
                .installOn(instrumentation);

    }

    /**
     * 类注入到classloader测试
     */
    @Test
    public void test8() throws ClassNotFoundException {
        // a new classloader
        final ClassLoader classLoader = new URLClassLoader(new URL[0], ClassLoadingStrategy.BOOTSTRAP_LOADER);

        // ensure not exists
        Assert.assertThrows(ClassNotFoundException.class, () -> Class.forName(Foo.class.getName(), false, classLoader));

        Map<TypeDescription, byte[]> types1 = Maps.newHashMap(ImmutableMap.of(
                TypeDescription.ForLoadedType.of(Foo.class), ClassFileLocator.ForClassLoader.read(Foo.class)
        ));

        Class<?> clazz = new ClassInjector.UsingUnsafe(classLoader)
                // inject
                .inject(types1)
                .get(TypeDescription.ForLoadedType.of(Foo.class));
        assertThat(clazz, notNullValue(Class.class));

        clazz = Class.forName(Foo.class.getName(), false, classLoader);
        assertThat(clazz.getName(), is(Foo.class.getName()));

        // ---------injectRaw
        final ClassLoader classLoader2 = new URLClassLoader(new URL[0], ClassLoadingStrategy.BOOTSTRAP_LOADER);

        // ensure not exists
        Assert.assertThrows(ClassNotFoundException.class, () -> Class.forName(Foo.class.getName(), false, classLoader2));

        // com.kute.pureagent.bytebuddy.Foo
        String className = Foo.class.getName();
        Map<String, byte[]> types2 = Maps.newHashMap(ImmutableMap.of(
                className, ClassFileLocator.ForClassLoader.read(Foo.class)
        ));

        clazz = new ClassInjector.UsingUnsafe(classLoader2)
                // injectRaw
                .injectRaw(types2)
                .get(className);
        assertThat(clazz, notNullValue(Class.class));

        clazz = Class.forName(Foo.class.getName(), false, classLoader2);
        assertThat(clazz.getName(), is(className));

    }

}
