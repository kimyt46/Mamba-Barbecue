package com.kimyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.dto.DishDto;
import com.kimyt.reggie.dto.SetmealDto;
import com.kimyt.reggie.entity.Category;
import com.kimyt.reggie.entity.Dish;
import com.kimyt.reggie.entity.Setmeal;
import com.kimyt.reggie.entity.SetmealDish;
import com.kimyt.reggie.service.CategoryService;
import com.kimyt.reggie.service.SetmealDishService;
import com.kimyt.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;


@PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);

        return R.success("保存套餐成功");


    }


    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!= null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        List<Setmeal> setmealRecords = pageInfo.getRecords();
        Set<Long> categoryIdSet=new HashSet<>();
        for(Setmeal setmeal : setmealRecords){
            categoryIdSet.add(setmeal.getCategoryId());
        }

        List<Long> categoryIds = new ArrayList<>(categoryIdSet);
        List<Category> categories =categoryService.listByIds(categoryIds);

        Map<Long,String> categoryNameMap = new HashMap<>();
        for(Category category:categories){
            categoryNameMap.put(category.getId(),category.getName());
        }

        List<SetmealDto> setmealDtoList = new ArrayList<>();
        for(Setmeal setmeal:setmealRecords){
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            Long categoryid = setmeal.getCategoryId();
            String categoryName= categoryNameMap.get(categoryid);
            setmealDto.setCategoryName(categoryName);
            setmealDtoList.add(setmealDto);


        }

        setmealDtoPage.setRecords(setmealDtoList);


        return R.success(setmealDtoPage);
    }


    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){

    log.info("ids:{}",ids);

    setmealService.removeWithDish(ids);

    return R.success("删除成功");


    }


   @PostMapping("/status/{status}")
    public R<String> update(@PathVariable Integer status, @RequestParam List<Long> ids){
       log.info("修改套餐状态: status={}, ids={}", status, ids);
    LambdaUpdateWrapper<Setmeal> queryWrapper=new LambdaUpdateWrapper<>();
    queryWrapper.in(Setmeal::getId,ids);
    queryWrapper.set(Setmeal::getStatus,status);
    setmealService.update(queryWrapper);
    return R.success("修改成功");


   }

   @GetMapping("/list")
    public  R<List<Setmeal>> list(Setmeal setmeal){
    LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
    queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
    queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
    queryWrapper.orderByDesc(Setmeal::getUpdateTime);
       List<Setmeal> list = setmealService.list(queryWrapper);
       return R.success(list);


   }




}
