package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServicelmpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;
    /**
     * 新增套餐
     * @param setmealDTO
     */
    public void saveSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.save(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
        setmealDishMapper.saveSetmaelDishes(setmealDishes);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult getWithPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<Setmeal> setmealPage = setmealMapper.getWithPage(setmealPageQueryDTO);
        return new PageResult(setmealPage.getTotal(), setmealPage.getResult());
    }

    /**
     * 删除套餐
     * @param ids
     */
    public void delete(List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        setmealMapper.delete(ids);
        setmealDishMapper.deleteBySetmealId(ids);
    }

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        List<SetmealDish> setmealDishes= setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        System.out.println(setmeal.getStatus());
        System.out.println(setmeal.getDescription());
        // 修改套餐
        setmealMapper.update(setmeal);
        // 删除原来与套餐关联的菜品
        List<Long> list = new ArrayList<>();
        list.add(setmealDTO.getId());
        setmealDishMapper.deleteBySetmealId(list);
        // 重新插入与套餐关联的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDTO.getId()));
        setmealDishMapper.saveSetmaelDishes(setmealDishes);
    }

    /**
     * 套餐起售停售
     *
     * @param status
     * @param id
     */
    public void changeStatus(Integer status, Long id) {
        // 判断起售套餐里是否有停售菜品，有的话则无法起售
        List<Dish> dishList = dishMapper.getBySetmealId(id);
        if (dishList != null && dishList.size() > 0){
            dishList.forEach(dish -> {
                if (dish.getStatus() == StatusConstant.DISABLE) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealMapper.update(setmeal);
    }
}
