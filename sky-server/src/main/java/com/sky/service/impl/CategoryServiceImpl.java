package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        /*category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());*/
        categoryMapper.update(category);
    }

    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        List<Category> categoryList = categoryMapper.pageQuery(categoryPageQueryDTO.getName(), categoryPageQueryDTO.getType());

        Page<Category> p=(Page)categoryList;
        return new PageResult(p.getTotal(), p.getResult());
    }


    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .id(categoryDTO.getId())
                .type(categoryDTO.getType())
                .name(categoryDTO.getName())
                .sort(categoryDTO.getSort())
                .status(0)
                /*.createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .createUser(BaseContext.getCurrentId())
                .updateUser(BaseContext.getCurrentId())*/
                .build();
        categoryMapper.insert(category);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                /*.updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())*/
                .build();
        categoryMapper.update(category);
    }

    @Override
    public void delete(Long id) {
        Integer count = dishMapper.countByCategoryId(id);
        if(count > 0) {
            throw new IllegalArgumentException("该分类下有菜品，不能删除");
        }
        count = setmealMapper.countByCategoryId(id);
        if(count > 0) {
            throw new IllegalArgumentException("该分类下有套餐，不能删除");
        }
        categoryMapper.delete(id);
    }

    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}
