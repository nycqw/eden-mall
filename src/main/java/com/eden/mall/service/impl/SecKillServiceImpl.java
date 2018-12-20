package com.eden.mall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.eden.domain.request.StockParam;
import com.eden.mall.domain.BuyParam;
import com.eden.mall.service.IMessageLogService;
import com.eden.mall.service.ISecKillService;
import com.eden.mall.utils.SnowFlake;
import com.eden.order.constants.MQConstants;
import com.eden.order.param.OrderParam;
import com.eden.order.service.IOrderService;
import com.eden.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/17
 */
@Service
@Slf4j
public class SecKillServiceImpl implements ISecKillService {

    @Reference
    private ProductService productService;

    @Reference
    private IOrderService orderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private IMessageLogService messageLogService;

    @Transactional
    @Override
    public Long rushBuy(BuyParam param) {
        // 预订单创建
        OrderParam orderParam = new OrderParam();
        BeanUtils.copyProperties(param, orderParam);
        orderService.createOrder(orderParam);

        // 扣减库存
        StockParam stockParam = new StockParam();
        stockParam.setProductId(param.getProductId());
        stockParam.setNumber(param.getPurchaseAmount());
        if (productService.reduceStockAsync2(stockParam)){

        }
        return null;
    }

    @Override
    public Long syncCreateOrder(OrderParam orderParam) {
        // 扣减库存
        /*boolean deductingResult = productService.deductingProductStock(orderParam.getProductId(), orderParam.getPurchaseAmount());
        if (!deductingResult) {
            return null;
        }*/

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
