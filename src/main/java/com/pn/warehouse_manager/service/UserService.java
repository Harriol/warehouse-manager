package com.pn.warehouse_manager.service;

import com.pn.warehouse_manager.entity.User;

/**
 * user_info表的service接口
 */
public interface UserService {

    //根据账号查询用户的业务方法
    public User queryUserByCode(String c);
}
