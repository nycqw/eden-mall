package com.eden.mall.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @author chenqw
 * @since 2018/12/20
 */
@Setter @Getter
public class BuyParam {
    private Long productId;
    private Long purchaseAmount;
    private Long userId;
    private Long orderId;
}
