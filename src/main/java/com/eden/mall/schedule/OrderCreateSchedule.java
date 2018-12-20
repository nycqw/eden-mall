package com.eden.mall.schedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eden.mall.mapper.MessageLogMapper;
import com.eden.mall.model.MessageLog;
import com.eden.mall.service.ISecKillService;
import com.eden.order.param.OrderParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/24
 */
@Component
@Slf4j
public class OrderCreateSchedule {

    @Autowired
    private MessageLogMapper messageLogMapper;

    @Autowired
    private ISecKillService orderService;

    @Scheduled(cron = "0 0/1 * * * ? ")
    public void handleOrderCreateFailure() {
        List<MessageLog> messageLogs = messageLogMapper.selectOfNeedRetry();
        for (MessageLog messageLog : messageLogs) {
            String message = messageLog.getMessage();
            OrderParam orderParam = JSON.parseObject(message, new TypeReference<OrderParam>(){});
            orderService.syncCreateOrder(orderParam);
            log.info("消息重发，消息内容：{}", message);
        }
    }
}
