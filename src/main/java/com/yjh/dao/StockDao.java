package com.yjh.dao;

import com.yjh.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author mua
 * @Date 2020/8/4 9:58
 */
@Mapper
public interface StockDao {

    /**
     * 根据商品id查询库存
     */
    Stock checkStock(Integer id);

    /**
     * 根据商品id扣除库存
     * @param stock
     */
    int updateSale(Stock stock);
}
