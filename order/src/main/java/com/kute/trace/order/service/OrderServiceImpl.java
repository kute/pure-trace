package com.kute.trace.order.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kute.trace.order.domain.Order;
import com.kute.trace.order.mapper.OrderMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}