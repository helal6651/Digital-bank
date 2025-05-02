package com.user_service.model.mapper;

import com.common_service.model.entity.User;
import com.user_service.response.user.UserResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;


import java.util.Objects;

public class UserMapper extends BaseMapper<User, UserResponse>{

    private final ModelMapper mapper = new ModelMapper();


    @Override
    public User convertToEntity(UserResponse dto, Object... args) {
        return null;
    }

    @Override
    public UserResponse convertToDto(User entity, Object... args) {
        UserResponse userDto = new UserResponse();
        if(!Objects.isNull(entity)){
            BeanUtils.copyProperties(entity, userDto);

        }
        return userDto;
    }
}