package com.kute.trace.item.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kute.trace.item.domain.Item;
import com.kute.trace.item.mapper.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    /**
     * 库存递减
     */
    @Override
    public Integer incr(Integer id, Integer count) {
        return itemMapper.updateItem(id,count);
    }
}
