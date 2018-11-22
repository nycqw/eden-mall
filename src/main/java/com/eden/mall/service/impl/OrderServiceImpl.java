package com.eden.mall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.eden.mall.constants.RabbitConstants;
import com.eden.mall.mapper.MessageLogMapper;
import com.eden.mall.model.MessageLog;
import com.eden.mall.service.OrderService;
import com.eden.mall.utils.SnowFlake;
import com.eden.order.param.OrderParam;
import com.eden.order.service.IOrderService;
import com.eden.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Date;

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

    @Reference
    private IOrderService orderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessageLogMapper messageLogMapper;

    @PostConstruct
    private void init() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            String messageId = correlationData.getId();
            MessageLog messageLog = messageLogMapper.selectByPrimaryKey(Long.valueOf(messageId));
            if (ack) {
                // 到达交换机若未被覆盖则表示到达队列
                messageLog.setStatus(1);
            } else {
                // 未到达交换机
                messageLog.setStatus(2);
            }
            messageLogMapper.updateByPrimaryKey(messageLog);
        });
        // 未到达队列
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            String messageId = message.getMessageProperties().getMessageId();
            MessageLog messageLog = messageLogMapper.selectByPrimaryKey(Long.valueOf(messageId));
            messageLog.setStatus(3);
            messageLogMapper.updateByPrimaryKey(messageLog);
        });
    }

    @Override
    public Long createOrder(OrderParam orderParam) {
        boolean checkResult = productService.deductingProductStock(orderParam.getProductId(), orderParam.getPurchaseAmount());
        if (checkResult) {
            Long orderId = SnowFlake.generatingId();
            orderParam.setOrderId(orderId);
            orderService.createOrder(orderParam);
            return orderId;
        }
        return null;
    }

    @Override
    public Long createOrderByMQ(OrderParam orderParam) {
        boolean checkResult = productService.deductingProductStock(orderParam.getProductId(), orderParam.getPurchaseAmount());
        if (checkResult) {
            CorrelationData correlationData = new CorrelationData();
            Long orderId = SnowFlake.generatingId();
            orderParam.setOrderId(orderId);
            correlationData.setId(String.valueOf(orderId));
            // 记录消息日志
            recordMessageLog(orderId, JSON.toJSONString(orderParam));
            rabbitTemplate.convertAndSend(RabbitConstants.ORDER_CREATE_EXCHANGE, RabbitConstants.ORDER_CREATE_KEY, JSON.toJSONString(orderParam), correlationData);
            return orderId;
        }
        return null;
    }

    private void recordMessageLog(long orderId, String message) {
        MessageLog messageLog = new MessageLog();
        messageLog.setCreateTime(new Date());
        messageLog.setMessage(message);
        messageLog.setMessageId(orderId);
        messageLog.setStatus(0);
        messageLog.setRetryCount(1);
        messageLogMapper.insert(messageLog);
    }
}
