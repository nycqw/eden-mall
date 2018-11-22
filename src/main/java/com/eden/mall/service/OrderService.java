package com.eden.mall.service;

import com.eden.order.param.OrderParam;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/17
 */
public interface OrderService {

    Long createOrder(OrderParam orderParam);

    Long syncCreateOrder(OrderParam orderParam);
}
