package com.heima.user.service.impl;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;

@Service
@Transactional
@Log4j2
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {
    /**
     * App端登录功能
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        //判断用户身份
        if (StringUtils.isBlank(dto.getPassword()) || StringUtils.isBlank(dto.getPhone())){
            //游客方式
            HashMap<String,Object> map = new HashMap<>();
            map.put("token",AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
        //登录方式
        ApUser user = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
        if (user == null){
           return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户不存在");
        }
        String PasswordWithSalt = DigestUtils.md5DigestAsHex(((dto.getPassword() + user.getSalt())).getBytes());
        if (!StringUtils.equals(PasswordWithSalt,user.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
        //校验成功
        HashMap<String, Object> map = new HashMap<>();
        map.put("token", AppJwtUtil.getToken(user.getId().longValue()));
        map.put("user", user);
        return ResponseResult.okResult(map);
    }
}
