package com.eden.mall.config;

import com.eden.mall.constants.MessageStatusEnums;
import com.eden.mall.mapper.MessageLogMapper;
import com.eden.mall.model.MessageLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * 消息发布确认回调定义
 *
 * @author chenqw
 * @version 1.0
 * @since 2018/11/24
 */
@Component
@Slf4j
public class RabbitTemplateConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessageLogMapper messageLogMapper;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 确认消息是否到达exchange
     *
     * @param correlationData correlation data for the callback. 消息相关数据
     * @param ack             true for ack, false for nack 是否接收消息成功
     * @param cause           An optional cause, for nack, when available, otherwise null. 接收失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("消息到达交换机，消息ID：{}", correlationData.getId());
            updateMessageLog(correlationData.getId(), MessageStatusEnums.QUEUED.getStatus());
        } else {
            log.error("消息未到达交换机，消息ID：{}，失败原因：{}", correlationData.getId(), cause);
            updateMessageLog(correlationData.getId(), MessageStatusEnums.QUEUED_FAILURE.getStatus());
        }
    }

    /**
     * 启动消息失败返回，确认消息是否到达队列
     *
     * @param message    the returned message.
     * @param replyCode  the reply code.
     * @param replyText  the reply text.
     * @param exchange   the exchange.
     * @param routingKey the routing key.
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.error("消息体：{}", message);
        log.error("返回码：{}", replyCode);
        log.error("返回内容：{}", replyText);
        log.error("交换机：{}", exchange);
        log.error("路由键：{}", routingKey);
        updateMessageLog(message.getMessageProperties().getMessageId(), MessageStatusEnums.QUEUED_FAILURE.getStatus());
    }

    /**
     * 日志进行异步处理时避免日志还未插入就进行了更新
     *
     * @param messageId 消息ID
     * @param status    消息状态
     */
    private void updateMessageLog(String messageId, int status) {
        while (true) {
            MessageLog messageLog = messageLogMapper.selectByPrimaryKey(Long.valueOf(messageId));
            if (messageLog != null) {
                messageLog.setStatus(status);
                messageLog.setNextRetryTime(new Date(System.currentTimeMillis() + 60 * 1000));
                messageLogMapper.updateByPrimaryKey(messageLog);
                break;
            }
            log.info("等待更新消息日志状态...");
        }
    }
}
