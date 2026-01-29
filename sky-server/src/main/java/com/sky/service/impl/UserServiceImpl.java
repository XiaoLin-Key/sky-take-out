package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Api(tags = "用户端用户登陆相关接口")
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //调用微信接口服务，获得微信用户的openid
        String openid=getOpenid(userLoginDTO.getCode());
        //判断openid是否为空
        if(openid==null){
            //openid为空，抛出异常
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断存在，新增用户，并返回用户信息
        //根据openid查询用户
        User user=userMapper.getByOpenid(openid);
        //判断用户是否存在
        if(user==null){
            //用户不存在，新增用户
            user= User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户对象
        return user;
    }

    private String getOpenid(String code){
        //调用微信接口服务，获得微信用户的openid
        Map<String,String> map=new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String json= HttpClientUtil.doGet(WX_LOGIN_URL, map);

        JSONObject jsonObject=JSONObject.parseObject(json);
        String openid=jsonObject.getString("openid");

        return openid;
    }
}
