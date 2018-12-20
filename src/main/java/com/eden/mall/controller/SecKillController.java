package com.eden.mall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.eden.domain.request.StockParam;
import com.eden.domain.result.Result;
import com.eden.mall.domain.BuyParam;
import com.eden.mall.service.ISecKillService;
import com.eden.order.param.OrderParam;
import com.eden.order.service.IOrderService;
import com.eden.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqw
 * @since 2018/12/20
 */
@RestController
@RequestMapping("/sec/kill")
public class SecKillController {

    @Autowired
    private ISecKillService secKillService;

    @RequestMapping("/buy")
    public Result rushBuy(BuyParam param){
        Long orderId = secKillService.rushBuy(param);
        if (orderId != null) {
            return Result.success(orderId);
        }
        return Result.fail("抢购失败");
    }
}
