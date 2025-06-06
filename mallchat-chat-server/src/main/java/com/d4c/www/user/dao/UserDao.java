package com.d4c.www.user.dao;

import com.d4c.www.user.domain.entity.User;
import com.d4c.www.user.mapper.UserMapper;
import com.d4c.www.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/madeheaven311">d4c</a>
 * @since 2025-06-06
 */
@Service
public class UserDao extends ServiceImpl<UserMapper, User> implements IUserService {

}
