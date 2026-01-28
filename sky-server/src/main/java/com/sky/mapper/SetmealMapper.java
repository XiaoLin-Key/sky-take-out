package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
    /**
     * 新增套餐
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);
    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    List<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
    /**
     * 根据id删除套餐
     * @param id
     */
    @Delete("delete from setmeal where id = #{id}")
    void delete(Long id);
    /**
     * 更新套餐
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);
    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("select s.*,c.name as category_name from setmeal s left join category c on s.category_id = c.id where s.id = #{id}")
    Setmeal selectById(Long id);
}
