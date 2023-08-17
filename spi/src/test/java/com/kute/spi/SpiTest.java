package com.kute.spi;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Created by kute on 2023/8/16 17:15
 */
@Slf4j
public class SpiTest {

    /**
     * 文件必须放在META-INF/services/目录底下
     * 文件名必须为接口的全限定名，内容为接口实现的全限定名，一行一个
     * <p>
     * 读取文件，然后通过反射实例化实现类，即 默认构造函数
     * <p>
     * 一次性全部加载，无法按需加载，浪费资源
     * 无法区别实现类，需要自己通过 接口设计 得到
     * <p>
     * 在实际的框架设计中，上面这段测试代码其实是框架作者写到框架内部的，而对于框架的使用者来说，要想自定义LoadBalance实现，嵌入到框架，仅仅只需要写接口的实现和spi文件即可
     */
    @Test
    public void testJavaSPI() {
        ServiceLoader<LoadBalance> serviceLoader = ServiceLoader.load(LoadBalance.class, this.getClass().getClassLoader());
        for (LoadBalance loadBalance : serviceLoader) {
            loadBalance.print();
        }
    }

    /**
     * 配置文件必须在META-INF/目录下，文件名必须为spring.factories
     * 文件内容为键值对，一个键可以有多个值，只需要用逗号分割就行，同时键值都需要是类的全限定名，键和值可以没有任何类与类之间的关系，当然也可以有实现的关系
     * <p>
     * 读取文件，通过构造函数实例化
     * SpringBoot3.0之后不再使用SpringFactoriesLoader，而是Spring重新从META-INF/spring/目录下的org.springframework.boot.autoconfigure.AutoConfiguration.imports文件中读取
     * <p>
     * 但是可以通过 loadFactoryNames 方法获取所有的 SPI 实现，来 注入到 Spring 容器，通过 Spring 容器实例化
     * <p>
     * 示例：PropertySourceLoader见org.springframework.boot.context.config.ConfigFileApplicationListener.Loader
     */
    @Test
    public void testSpringSPI() {
        List<LoadBalance> loadBalanceList = SpringFactoriesLoader.loadFactories(LoadBalance.class, this.getClass().getClassLoader());
        for (LoadBalance loadBalance : loadBalanceList) {
            loadBalance.print();
        }

        // 获取所有类的SPI 实现名称
        List<String> implClassList = SpringFactoriesLoader.loadFactoryNames(LoadBalance.class, this.getClass().getClassLoader());
        log.info("{}", implClassList);
    }

    /**
     * 接口必须要加@SPI注解
     * 配置文件可以放在META-INF/services/、META-INF/dubbo/internal/ 、META-INF/dubbo/ 、META-INF/dubbo/external/这四个目录底下，文件名也是接口的全限定名
     * 内容为键值对，键为短名称（可以理解为spring中Bean的名称），值为实现类的全限定名
     * <p>
     * 自适应，自适应扩展类的含义是说，基于参数，在运行时动态选择到具体的目标类，然后执行
     * 每个接口有且只能有一个自适应类，通过ExtensionLoader的getAdaptiveExtension方法就可以获取到这个类的对象，这个对象可以根据运行时具体的参数找到目标实现类对象，然后调用目标对象的方法
     * 自适应类有两种方式产生，第一种就是自己指定，在接口的实现类或者方法上加@Adaptive注解; 第二种是 dubbo动态（基于 Cglib 等动态代理）生成
     * <p>
     * 在方法上添加@Adaptive 注解，则表示Dubbo会为该接口自动生成一个子类，并且按照一定的格式重写该方法
     * <p>
     * 在Dubbo中SPI接口的实现中，有一种特殊的类，被称为Wrapper类，这个类的作用就是来实现AOP的
     * 判断Wrapper类的唯一标准就是这个类中必须要有这么一个构造参数，这个构造方法的参数只有一个，并且参数类型就是接口的类型
     * <p>
     * 注解@Active 注解表示激活，表示在满足某些条件下激活某些扩展，如 在 provider 端激活部分 filter，在 consumer 端激活另一部分 filter
     * group 表示 只有传了指定的 group 时才会被激活，value 表示只有传了指定了参数才会被激活，参数的值不重要
     */
    @Test
    public void testDubboSPI() {
        ExtensionLoader<LoadBalance> extensionLoader = ExtensionLoader.getExtensionLoader(LoadBalance.class);

        // 获取指定名称的 扩展点实现类
        // 如果有包装类，那么此时通过 getExtension("random") 获取到的是 包装类，类中的实例才是真正的 random 实例
        extensionLoader.getExtension("random").print();

        // 获取 扩展点实现类的 key
        log.info("{}", extensionLoader.getExtensionName(HashLoadBalance.class)); // 通过 类获取
        log.info("{}", extensionLoader.getExtensionName(extensionLoader.getExtension("hash"))); // 通过实例获取

        // 获取默认的 SPI 扩展，通过@SPI(value = 'random') 指定
        extensionLoader.getDefaultExtension().print();

        // 自适应 扩展点，根据参数 load.balance 这里获取的是 random 扩展
        extensionLoader.getAdaptiveExtension().log(URL.valueOf("http://127.0.0.1:1001?load.balance=random"));
        // 这里获取的是 hash 扩展
        extensionLoader.getAdaptiveExtension().log(URL.valueOf("http://127.0.0.1:1001?load.balance=hash"));

        log.info("============");
        // random 被激活
        extensionLoader.getActivateExtension(URL.valueOf("test://127.0.0.1:1001"), "", "b")
                .forEach(LoadBalance::print);
        log.info("============");
        // 无 被激活
        extensionLoader.getActivateExtension(URL.valueOf("test://127.0.0.1:1001"), "", "a")
                .forEach(LoadBalance::print);
        log.info("============");
        // hash 被激活
        extensionLoader.getActivateExtension(URL.valueOf("test://127.0.0.1:1001?x=11"), "", "a")
                .forEach(LoadBalance::print);



        System.out.println();

    }

}
