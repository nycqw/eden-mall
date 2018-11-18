package com.eden.mall.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/17
 */
@Setter
@Getter
public class OrderParam implements Serializable {

    private Long productId;

    private Integer number;

    private Long userId;
}
