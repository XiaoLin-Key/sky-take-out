package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "用户端菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品选项
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品选项")
    public Result<List<DishVO>> list(Long categoryId) {
        //构造redis的key，dish_分类id
        String key = "dish_" + categoryId;

        //查询redis中是否存在菜品缓存
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if(list != null&& list.size() > 0){
            log.info("缓存命中，正在返回缓存数据...");
            //如果存在直接返回，无需查询数据库
            return Result.success(list);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//1表示起售中

        //如果不存在，查询数据库
        list = dishService.listWithFlavor(dish);

        //将查询到的数据存入redis
        redisTemplate.opsForValue().set(key, list);
        log.info("缓存未命中，正在查询数据库...");
        return Result.success(list);
    }
}