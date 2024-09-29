package com.example.backend.mapper;

import com.example.backend.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where email=#{text} or username=#{text}")
    User findByEmailOrUsername(String text);
}
