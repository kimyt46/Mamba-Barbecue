package com.kimyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.dto.DishDto;
import com.kimyt.reggie.entity.Category;
import com.kimyt.reggie.entity.Dish;
import com.kimyt.reggie.service.CategoryService;
import com.kimyt.reggie.service.DishFlavorService;
import com.kimyt.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("保存成功");


    }


    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!= null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);
        Page<DishDto> dishDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> dishRecords = pageInfo.getRecords();
        Set<Long> categoryIdSet=new HashSet<>();
        for(Dish dish : dishRecords){
            categoryIdSet.add(dish.getCategoryId());
        }
        if (categoryIdSet.isEmpty()) {
            dishDtoPage.setRecords(new ArrayList<>()); // 设置一个空的记录列表
            return R.success(dishDtoPage);
        }

        List<Long> categoryIds = new ArrayList<>(categoryIdSet);
        List<Category> categories =categoryService.listByIds(categoryIds);

        Map<Long,String> categoryNameMap = new HashMap<>();
        for(Category category:categories){
            categoryNameMap.put(category.getId(),category.getName());
        }

        List<DishDto> dishDtoList = new ArrayList<>();
        for(Dish dish:dishRecords){
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            Long categoryid = dish.getCategoryId();
            String categoryName= categoryNameMap.get(categoryid);
            dishDto.setCategoryName(categoryName);
            dishDtoList.add(dishDto);


        }

        dishDtoPage.setRecords(dishDtoList);


        return R.success(dishDtoPage);
    }



}
