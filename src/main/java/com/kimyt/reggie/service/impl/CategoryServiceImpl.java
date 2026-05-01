package com.kimyt.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimyt.reggie.entity.Category;
import com.kimyt.reggie.entity.Dish;
import com.kimyt.reggie.entity.Setmeal;
import com.kimyt.reggie.mapper.CategoryMapper;
import com.kimyt.reggie.service.CategoryService;
import com.kimyt.reggie.service.DishService;
import com.kimyt.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final DishService dishService;
    
    @Autowired
    private SetmealService setmealService;

    public CategoryServiceImpl(DishService dishService) {
        this.dishService = dishService;
    }

    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getCategoryId, id);
        long count = dishService.count(lambdaQueryWrapper);
        if (count > 0) {
            throw new RuntimeException("当前分类下存在菜品,不能删除");
        }

        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper2.eq(Setmeal::getCategoryId, id);
        long count2 = setmealService.count(lambdaQueryWrapper2);
        if (count2 > 0) {
            throw new RuntimeException("当前分类下存在套餐,不能删除");
        }

        super.removeById(id);
    }
}
