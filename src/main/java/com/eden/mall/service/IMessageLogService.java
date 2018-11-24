package com.eden.mall.service;

public interface IMessageLogService {

    void recordLog(Long messageId, String message);
}
