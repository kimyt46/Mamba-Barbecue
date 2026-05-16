package com.kimyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.dto.DishDto;
import com.kimyt.reggie.entity.Category;
import com.kimyt.reggie.entity.Dish;
import com.kimyt.reggie.entity.DishFlavor;
import com.kimyt.reggie.service.CategoryService;
import com.kimyt.reggie.service.DishFlavorService;
import com.kimyt.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        Set keys = redisTemplate.keys("dish_" + dishDto.getCategoryId() + "_1");
        redisTemplate.delete(keys);
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

@GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
    DishDto dishDto = dishService.getByIdWithFlavor(id);
    return R.success(dishDto);

    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        Set keys = redisTemplate.keys("dish_" + dishDto.getCategoryId() + "_1");
        redisTemplate.delete(keys);

        return R.success("修改成功");


    }

//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        queryWrapper.eq(Dish::getStatus,1);
//
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//
//
//        return R.success(list);
//    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();

        List<DishDto> dishDtoList= (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList!=null){

            return R.success(dishDtoList);
        }


        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list= dishService.list(queryWrapper);
        dishDtoList = new ArrayList<>();
        for(Dish item : list){
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId(); //分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            
            dishDtoList.add(dishDto);
        }

        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);


        return R.success(dishDtoList);
    }






}
