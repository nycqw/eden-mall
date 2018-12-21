package com.eden.mall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.eden.domain.request.StockParam;
import com.eden.mall.domain.BuyParam;
import com.eden.mall.service.IMessageLogService;
import com.eden.mall.service.ISecKillService;
import com.eden.order.param.OrderParam;
import com.eden.order.service.IOrderService;
import com.eden.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/17
 */
@Service
@Slf4j
public class SecKillServiceImpl implements ISecKillService {

    @Reference
    private IProductService productService;

    @Reference
    private IOrderService orderService;

    @Override
    public Long rushBuy(BuyParam param) {
        // 订单创建
        OrderParam orderParam = new OrderParam();
        BeanUtils.copyProperties(param, orderParam);
        Long orderId = orderService.createOrder(orderParam);

        // 扣减库存
        StockParam stockParam = new StockParam();
        stockParam.setProductId(param.getProductId());
        stockParam.setPurchaseAmount(param.getPurchaseAmount());
        boolean success = productService.reduceStockAsync2(stockParam);

        orderParam.setOrderId(orderId);
        if (success) {
            // 确认订单
            orderService.confirmOrder(orderParam);
            return orderId;
        } else {
            // 取消订单
            orderService.cancelOrder(orderParam);
            return null;
        }
    }

}
