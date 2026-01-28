package com.sky.mapper;

import com.sky.entity.Category;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 更新 分类
     * @param category
     * @return
     */
    void update(Category category);

    /**
     * 分类分页查询
     * @param name
     * @param type
     * @return
     */
    List<Category> pageQuery(String name, Integer type);
    /**
     * 新增分类
     * @param category
     */
    @Insert("insert into category (id,type, name, sort, create_time, status, create_user,update_time, update_user) " +
            "values (#{id}, #{type}, #{name}, #{sort}, #{createTime}, #{status}, #{createUser}, #{updateTime}, #{updateUser})")
    void insert(Category category);
    /**
     * 根据id删除分类
     * @param id
     */
     @Delete("delete from category where id = #{id}")
    void delete(Long id);
    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    List<Category> list(Integer type);
}
