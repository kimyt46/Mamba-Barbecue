package com.kimyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimyt.reggie.common.BaseContext;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.entity.OrderDetail;
import com.kimyt.reggie.entity.Orders;
import com.kimyt.reggie.service.OrderDetailService;
import com.kimyt.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        
        orderService.page(pageInfo, queryWrapper);
        
        List<Orders> records = pageInfo.getRecords();
        if (!records.isEmpty()) {
            List<Long> orderIds = records.stream().map(Orders::getId).collect(Collectors.toList());
            LambdaQueryWrapper<OrderDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
            detailQueryWrapper.in(OrderDetail::getOrderId, orderIds);
            List<OrderDetail> details = orderDetailService.list(detailQueryWrapper);
            
            Map<Long, List<OrderDetail>> detailMap = details.stream()
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId));
            
            for (Orders order : records) {
                order.setOrderDetails(detailMap.getOrDefault(order.getId(), new ArrayList<>()));
            }
        }
        
        return R.success(pageInfo);
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, 
                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.like(number != null, Orders::getNumber, number);
        queryWrapper.ge(beginTime != null, Orders::getOrderTime, beginTime);
        queryWrapper.le(endTime != null, Orders::getOrderTime, endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        
        orderService.page(pageInfo, queryWrapper);
        
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders) {
        log.info("修改订单状态：{}", orders);
        
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orders.getId());
        updateWrapper.set(Orders::getStatus, orders.getStatus());
        
        orderService.update(updateWrapper);
        
        return R.success("操作成功");
    }
}
