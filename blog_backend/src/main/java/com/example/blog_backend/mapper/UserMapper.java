package com.example.blog_backend.mapper;

import com.example.blog_backend.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User>{

    // 查询所有
    List<User> selectAll();

    // 批量添加
    void batchInsert(List<User> list);

    // 自定义 update，当前业务主要使用 MyBatis-Plus updateById
    void update(User user);


}
