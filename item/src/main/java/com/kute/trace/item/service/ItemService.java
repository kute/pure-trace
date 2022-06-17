package com.kute.trace.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kute.trace.item.domain.Item;

public interface ItemService extends IService<Item> {
    Integer incr(Integer id, Integer count);
}
