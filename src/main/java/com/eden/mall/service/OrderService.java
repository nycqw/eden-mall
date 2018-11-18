package com.eden.mall.service;

import com.eden.mall.domain.OrderParam;
import com.eden.model.TProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/17
 */
public interface OrderService {

    Long createOrder(OrderParam orderParam);
}
