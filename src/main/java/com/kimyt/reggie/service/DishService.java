package com.kimyt.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.dto.DishDto;
import com.kimyt.reggie.entity.Dish;
import org.springframework.stereotype.Service;


public interface DishService extends IService<Dish> {


    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);

    public void updateWithFlavor(DishDto dishDto);
}
