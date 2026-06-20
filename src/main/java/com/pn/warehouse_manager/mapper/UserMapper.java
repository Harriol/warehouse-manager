package com.pn.warehouse_manager.mapper;

import com.pn.warehouse_manager.entity.User;

/**
 * user_info表的mapper接口
 */
public interface UserMapper {

    //根据账号查询用户信息
    User findUserByCode(String userCode);
}
