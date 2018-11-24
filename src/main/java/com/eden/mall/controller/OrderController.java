package com.eden.mall.controller;

import com.eden.domain.result.Result;
import com.eden.mall.service.IOrderService;
import com.eden.order.param.OrderParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

/**
 * @author chenqw
 * @version 1.0
 * @since 2018/11/17
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @RequestMapping("create")
    public Result createOrder(@RequestBody OrderParam orderParam) {
        Long orderId = orderService.createOrder(orderParam);
        //simulatedHighConcurrency(orderParam);
        if (orderId == null) {
            return Result.fail("创建失败");
        }
        return Result.success(orderId);
    }

    @RequestMapping("/sync/create")
    public Result syncCreateOrder(@RequestBody OrderParam orderParam) {
        Long orderId = orderService.syncCreateOrder(orderParam);
        return Result.success(orderId);
    }

    private void simulatedHighConcurrency(OrderParam orderParam) {
        final int threadNumber = 500;
        CountDownLatch countDownLatch = new CountDownLatch(threadNumber);

        for (int i = 0; i < threadNumber; i++) {
            new Thread(() -> {
                countDownLatch.countDown();
                log.info("准备中，Thread{}=====================================", Thread.currentThread().getName());
                // 阻塞当前线程，直到信号量为0
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                }
                orderService.syncCreateOrder(orderParam);
                log.info("执行完毕，Thread{}=====================================", Thread.currentThread().getName());
            }).start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        log.info("开始执行...=======================================");
    }
}
