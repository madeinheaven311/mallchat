package com.d4c.www.user.service.impl;

import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.d4c.www.user.dao.UserDao;
import com.d4c.www.user.domain.entity.User;
import com.d4c.www.user.mapper.UserMapper;
import com.d4c.www.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description: 用户基础操作类
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-19
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserDao userDao;

    @Override
    public void register(User user) {
        userDao.save(user);
        //applicationEventPublisher.publishEvent(new UserRegisterEvent(this, user));
    }


}
