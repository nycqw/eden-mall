package com.eden.mall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.eden.mall.service.IMessageLogService;
import com.eden.mall.service.IOrderService;
import com.eden.mall.utils.SnowFlake;
import com.eden.order.constants.MQConstants;
import com.eden.order.param.OrderParam;
import com.eden.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/17
 */
@Service
@Slf4j
public class OrderServiceImpl implements IOrderService {

    @Reference
    private ProductService productService;

    @Reference
    private com.eden.order.service.IOrderService IOrderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private IMessageLogService messageLogService;

    @Override
    public Long createOrder(OrderParam orderParam) {
        boolean checkResult = productService.deductingProductStock(orderParam.getProductId(), orderParam.getPurchaseAmount());
        if (checkResult) {
            Long orderId = SnowFlake.generatingId();
            orderParam.setOrderId(orderId);
            IOrderService.createOrder(orderParam);
            return orderId;
        }
        return null;
    }

    @Override
    public Long syncCreateOrder(OrderParam orderParam) {
        // 扣减库存
        boolean deductingResult = productService.deductingProductStock(orderParam.getProductId(), orderParam.getPurchaseAmount());
        if (!deductingResult) {
            return null;
        }

        Long orderId = orderParam.getOrderId();
        if (orderId == null) {
            orderId = SnowFlake.generatingId();
            orderParam.setOrderId(orderId);
        }

        // 发送消息
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(String.valueOf(orderId));
        String textMessage = JSON.toJSONString(orderParam);
        rabbitTemplate.convertAndSend(MQConstants.ORDER_CREATE_EXCHANGE, MQConstants.ORDER_CREATE_KEY, textMessage, correlationData);

        // 记录消息日志
        messageLogService.recordLog(orderId, textMessage);
        return orderId;
    }

}
