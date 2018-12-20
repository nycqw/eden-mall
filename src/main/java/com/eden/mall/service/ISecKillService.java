package com.eden.mall.service;

import com.eden.mall.domain.BuyParam;
import com.eden.order.param.OrderParam;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/17
 */
public interface ISecKillService {

    Long rushBuy(BuyParam param);

    Long syncCreateOrder(OrderParam orderParam);
}
