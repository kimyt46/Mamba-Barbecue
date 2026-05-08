package com.kimyt.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimyt.reggie.common.CustomException;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.dto.SetmealDto;
import com.kimyt.reggie.entity.Setmeal;
import com.kimyt.reggie.entity.SetmealDish;
import com.kimyt.reggie.mapper.SetmealMapper;
import com.kimyt.reggie.service.SetmealDishService;
import com.kimyt.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@Slf4j



public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {


    @Autowired
    private SetmealDishService setmealDishService;


    @Override
    public void saveWithDish(SetmealDto setmealDto) {

        super.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();


        if (setmealDishes != null && !setmealDishes.isEmpty()){
            Long setmealId = setmealDto.getId();
            for(SetmealDish setmealDish:setmealDishes){
                setmealDish.setSetmealId(setmealId);

            }
            setmealDishService.saveBatch(setmealDishes);
        }



    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
       long count = this.count(queryWrapper);
        if(count>0){
            throw new CustomException("套餐正在售卖中,不能删除");

        }
        this.removeByIds(ids);

        LambdaQueryWrapper<SetmealDish> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper1);


    }
}
