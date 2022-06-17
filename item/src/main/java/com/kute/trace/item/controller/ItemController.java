package com.kute.trace.item.controller;

import com.kute.trace.item.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 库存递减
     */
    @GetMapping(value = "/{id}/{count}")
    public Integer incrCount(@PathVariable(value = "id") Integer id,
                             @PathVariable(value = "count") Integer count) {
        log.info("incrCount param for id={}, count={}", id, count);
        return itemService.incr(id, count);
    }
}
