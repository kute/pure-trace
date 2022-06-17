package com.kute.trace.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kute.trace.item.domain.Item;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ItemMapper extends BaseMapper<Item> {
    @Update("update tb_item set count=count-#{count} where id=#{id} and count>=#{count}")
    Integer updateItem(@Param("id") Integer id, @Param("count") Integer count);
}
