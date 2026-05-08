package com.kimyt.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.dto.SetmealDto;
import com.kimyt.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setmealDto);

    public void removeWithDish(List<Long> ids);
}
