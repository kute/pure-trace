package com.kute.spi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;


/**
 * Created by kute on 2023/8/16 17:13
 */
// SPI 这里指定的是 默认的 SPI 扩展
@SPI("random")
public interface LoadBalance {

    void print();

    /**
     * 在 方法上添加 @Adaptive ，表示 在获取当前该接口的扩展时会自动生成一个 子类，然后
     * 在调用此方法时 会根据 @Adaptive 注解的参数 来 选择真正的扩展进行 调用
     */
    // 这里的参数可以声明多个，使用 在url 中值不为空的参数的值 作为 查找 扩展的名称
    // 即 若在 url 中 参数 aa 不存在或者值为空，则 查找 bb，若 bb 参数存在且 值不为空，则 bb 的值就作为扩展的名称
//    @Adaptive(value = {"aa", "bb"})
    // 如果不指定参数，则默认 是以接口名按规则查找到，如 这里的 LoadBalance 将会按照 load.balance 作为参数在 url 中查找
    @Adaptive
    void log(URL url);

}
