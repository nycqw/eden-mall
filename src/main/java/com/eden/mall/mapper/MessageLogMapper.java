package com.eden.mall.mapper;

import com.eden.mall.model.MessageLog;

import java.util.List;

public interface MessageLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(MessageLog record);

    int insertSelective(MessageLog record);

    MessageLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MessageLog record);

    int updateByPrimaryKey(MessageLog record);

    List<MessageLog> selectOfNeedRetry();
}