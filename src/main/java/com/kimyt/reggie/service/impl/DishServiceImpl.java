package com.kimyt.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimyt.reggie.dto.DishDto;
import com.kimyt.reggie.entity.Dish;
import com.kimyt.reggie.entity.DishFlavor;
import com.kimyt.reggie.mapper.DishMapper;
import com.kimyt.reggie.service.DishFlavorService;
import com.kimyt.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

@Autowired
private DishFlavorService dishFlavorService;

@Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {

        this.save(dishDto);


        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();

        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);

        }
        dishFlavorService.saveBatch(flavors);


    }
}
