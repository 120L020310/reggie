package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){

        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Setmeal::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行分页查询
        setmealService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list=new ArrayList<>();
        for(Setmeal item:records){
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            list.add(setmealDto);
        }
        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }
    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        return R.success(setmealDto);
    }
    /**
     * 修改菜品
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());

        setmealService.updateWithDish(setmealDto);

        return R.success("修改菜品成功");
    }
    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        //方法......
        setmealService.removeWithDish(ids);

        return R.success("套餐数据删除成功");
    }

//    @GetMapping("/list")
//    public R<List<Setmeal>> list(Setmeal setmeal) {
//        log.info("setmeal:{}", setmeal);
//        //条件构造器
//
////        return R.success(setmealService.list(queryWrapper));
//        return null;
//    }
    /**
     * 停售菜品
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> stop(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        //方法......
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getStatus, 1);
        queryWrapper.in(Setmeal::getId, ids);
        int count = setmealService.count(queryWrapper);
        if (!(count > 0)) {
            //如果不能删除，抛出一个业务异常
            throw new CustomException("没有菜品可停售");
        }
        List<Setmeal> setmeals=setmealService.list(queryWrapper);
        log.info("dishes:{}",setmeals);
        setmealService.removeByIds(ids);
        for(Setmeal setmeal:setmeals){
            setmeal.setStatus(0);
            setmealService.save(setmeal);
        }
        log.info("dishes:{}",setmeals);
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
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getStatus, 0);
        queryWrapper.in(Setmeal::getId, ids);
        int count = setmealService.count(queryWrapper);
        if (!(count > 0)) {
            //如果不能删除，抛出一个业务异常
            throw new CustomException("没有菜品可停售");
        }
        List<Setmeal> setmeals=setmealService.list(queryWrapper);
        log.info("dishes:{}",setmeals);
        setmealService.removeByIds(ids);
        for(Setmeal setmeal:setmeals){
            setmeal.setStatus(1);
            setmealService.save(setmeal);
        }
        log.info("dishes:{}",setmeals);
        return R.success("菜品状态更改成功！");
    }
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmeals=setmealService.list(queryWrapper);
        return R.success(setmeals);
    }
}
