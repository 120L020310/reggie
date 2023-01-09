package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
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

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }


    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
//        List<DishDto> list = records.stream().map((item) -> {
//            DishDto dishDto = new DishDto();
//            BeanUtils.copyProperties(item,dishDto);
//            Long categoryId = item.getCategoryId();//分类id
//            //根据id查询分类对象
//            Category category = categoryService.getById(categoryId);
//            if(category != null){
//                String categoryName = category.getName();
//                dishDto.setCategoryName(categoryName);
//            }
//            return dishDto;
//        }).collect(Collectors.toList());
        List<DishDto> list=new ArrayList<>();
        for(Dish item:records){
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            list.add(dishDto);
        }
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }
//
    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }
//
    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }
    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        //方法......

        dishService.removeWithFlavor(ids);

        return R.success("菜品数据删除成功");
    }
    /**
     * 停售菜品
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> stop(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        //方法......
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.in(Dish::getId, ids);
        int count = dishService.count(queryWrapper);
        if (!(count > 0)) {
            //如果不能删除，抛出一个业务异常
            throw new CustomException("没有菜品可停售");
        }
        List<Dish> dishes=dishService.list(queryWrapper);
        log.info("dishes:{}",dishes);
        dishService.removeByIds(ids);
        for(Dish dish:dishes){
            dish.setStatus(0);
            dishService.save(dish);
        }
        log.info("dishes:{}",dishes);
        return R.success("菜品状态更改成功！");
    }
    /**
     * 停售菜品
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> start(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        //方法......
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, 0);
        queryWrapper.in(Dish::getId, ids);
        int count = dishService.count(queryWrapper);
        if (!(count > 0)) {
            //如果不能删除，抛出一个业务异常
            throw new CustomException("没有菜品可停售");
        }
        List<Dish> dishes=dishService.list(queryWrapper);
        log.info("dishes:{}",dishes);
        dishService.removeByIds(ids);
        for(Dish dish:dishes){
            dish.setStatus(1);
            dishService.save(dish);
        }
        log.info("dishes:{}",dishes);
        return R.success("菜品状态更改成功！");
    }
}
