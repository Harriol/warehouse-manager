package com.pn.warehouse_manager.service.impl;

import com.pn.warehouse_manager.entity.User;
import com.pn.warehouse_manager.mapper.UserMapper;
import com.pn.warehouse_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    //注入UserMapper
    @Autowired
    private UserMapper userMapper;

    @Override
    public User queryUserByCode(String userCode) {
        return userMapper.findUserByCode(userCode);
    }
}
