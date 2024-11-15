package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param ids
     * @return
     */
    List<Long> getSetMealIdByDishIds(List<Long> ids);


    /**
     * 根据套餐新增对应的套餐菜品
     * @param setmealDishes
     */
    void saveSetmaelDishes(List<SetmealDish> setmealDishes);
}
