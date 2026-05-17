package com.kimyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.entity.OrderDetail;
import com.kimyt.reggie.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orderDetail")
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping("/{id}")
    public R<List<OrderDetail>> detail(@PathVariable Long id) {
        log.info("查询订单明细，订单ID：{}", id);
        
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, id);
        queryWrapper.orderByAsc(OrderDetail::getNumber);
        
        List<OrderDetail> list = orderDetailService.list(queryWrapper);
        
        return R.success(list);
    }
}
