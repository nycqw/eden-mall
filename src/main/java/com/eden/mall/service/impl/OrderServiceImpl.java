package com.eden.mall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.eden.mall.domain.OrderParam;
import com.eden.mall.service.OrderService;
import com.eden.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/17
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Reference
    private ProductService productService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Long createOrder(OrderParam orderParam) {
        boolean checkResult = productService.deductingProductStock(orderParam.getProductId(), orderParam.getNumber());
        if (checkResult) {
            String message = JSON.toJSONString(orderParam);
            rabbitTemplate.convertAndSend("CREATE_ORDER_EXCHANGE","topic.order.create", message);
        }
        return null;
    }
}
