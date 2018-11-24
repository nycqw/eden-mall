package com.eden.mall.service.impl;

import com.eden.mall.constants.MessageStatusEnums;
import com.eden.mall.mapper.MessageLogMapper;
import com.eden.mall.model.MessageLog;
import com.eden.mall.service.IMessageLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/24
 */
@Service
@Slf4j
public class MessageLogServiceImpl implements IMessageLogService {

    @Autowired
    private MessageLogMapper messageLogMapper;

    @Async
    @Override
    public void recordLog(Long messageId, String message){
        MessageLog messageLog = new MessageLog();
        messageLog.setMessage(message);
        messageLog.setMessageId(messageId);
        messageLog.setStatus(MessageStatusEnums.CREATED.getStatus());
        messageLog.setCreateTime(new Date());

        try {
            messageLogMapper.insertSelective(messageLog);
        } catch (Exception e) {
            log.error("消息日志记录异常...", e);
        }
    }
}
