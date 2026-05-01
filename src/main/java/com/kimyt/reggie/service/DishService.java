package com.kimyt.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kimyt.reggie.dto.DishDto;
import com.kimyt.reggie.entity.Dish;
import org.springframework.stereotype.Service;


public interface DishService extends IService<Dish> {


    public void saveWithFlavor(DishDto dishDto);
}
