package com.kute.trace.order.controller;

import com.kute.trace.order.domain.Order;
import com.kute.trace.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping(value = "/order")
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderService orderService;

    /**
     * 下单
     */
    @GetMapping(value = "/{id}/{count}")
    public String add(@PathVariable(value = "id") Integer id,
                      @PathVariable(value = "count") Integer count) {
        log.info("order add for id={}, count={}", id, count);
        //1)递减库存
        int hCount = restTemplate.getForObject("http://localhost:18082/item/1/1", Integer.class);

        //2)下单
        if (hCount > 0) {
            Order order = new Order();
            order.setCount(count);
            order.setItemId(id);
            orderService.save(order);
        }
        return "SUCCESS";
    }
}