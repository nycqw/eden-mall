package com.eden.mall.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eden.domain.result.Result;
import com.eden.mall.constants.MessageStatusEnums;
import com.eden.mall.mapper.MessageLogMapper;
import com.eden.mall.model.MessageLog;
import com.eden.order.constants.MQConstants;
import com.eden.order.constants.ResultEnum;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/24
 */
@Slf4j
@Component
public class OrderCallbackListener {

    @Autowired
    private MessageLogMapper messageLogMapper;

    @RabbitListener(bindings = @QueueBinding(
            key = MQConstants.ORDER_CREATE_CALLBACK_KEY,
            value = @Queue(value = MQConstants.ORDER_CREATE_CALLBACK_QUEUE, durable = "true"),
            exchange = @Exchange(value = MQConstants.ORDER_CREATE_CALLBACK_EXCHANGE, type = "topic")
    ))
    public void orderCreateCallback(String msg, Channel channel, Message message){
        Result result = JSON.parseObject(msg, new TypeReference<Result>() {});
        Long messageId = (Long) result.getData();
        try {
            MessageLog messageLog = messageLogMapper.selectByPrimaryKey(Long.valueOf(messageId));
            if (result.getCode() == ResultEnum.SUCCESS.getCode()) {
                messageLog.setStatus(MessageStatusEnums.CONSUMED.getStatus());
                messageLogMapper.updateByPrimaryKey(messageLog);
                log.info("消费成功，消息ID：{}", messageId);
            } else {
                messageLog.setStatus(MessageStatusEnums.CONSUMED_FAILURE.getStatus());
                messageLogMapper.updateByPrimaryKey(messageLog);
                log.info("消费失败，消息ID：{}", messageId);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                log.error("mq basicNack exception", ex.getMessage());
            }
        }
    }
}
