package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();//菜品id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        log.info("flavors:"+flavors);
//        flavors = flavors.stream()
//                .map((item) -> {
//            item.setDishId(dishId);
//            return item; })
//                .collect(Collectors.toList());
        for (DishFlavor item:flavors) {
            item.setDishId(dishId);
        }
        log.info("flavors:"+flavors);
        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish=this.getById(id);
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list=dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(list);
        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        List<DishFlavor> flavors=dishDto.getFlavors();
        for (DishFlavor item:flavors) {
            item.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);
    }
    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
    public void removeWithFlavor(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.in(Dish::getId, ids);
        int count = this.count(queryWrapper);
        if (count > 0) {
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        super.removeByIds(ids);
        LambdaQueryWrapper<DishFlavor> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper1);
    }
//    /**
//     * 停售菜品
//     * @param ids
//     */
//    @Transactional
//    public void stopByIds(List<Long> ids) {
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Dish::getStatus, 1);
//        queryWrapper.in(Dish::getId, ids);
//        int count = this.count(queryWrapper);
//        if (!(count > 0)) {
//            //如果不能删除，抛出一个业务异常
//            throw new CustomException("没有菜品可停售");
//        }
//        List<Dish> dishes=this.list(queryWrapper);
//        log.info("dishes:{}",dishes);
//        for(Dish dish:dishes){
//            dish.setStatus(0);
//        }
//    }
}
