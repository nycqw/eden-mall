package com.eden.mall.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author chenqw
 * @since 2018/11/19
 */
@AllArgsConstructor
public enum MessageStatusEnums {

    CREATED(0, "消息创建成功"),
    QUEUED(1, "消息发送成功"),
    CONSUMED(2, "消息消费成功"),
    QUEUED_FAILURE(-1, "消息发送失败"),
    CONSUMED_FAILURE(-2, "消息消费失败");

    @Getter
    private int status;
    @Getter
    private String desc;
}
