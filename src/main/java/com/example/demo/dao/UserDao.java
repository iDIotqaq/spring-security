package com.example.demo.dao;


import com.example.demo.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author andyzhao
 */
@Mapper
public interface UserDao {
    User findByUserName(String user_name);
    User findByUserMobile(String mobile);
    User findByUserEmail(String email);
    List<User> listAll();
}
