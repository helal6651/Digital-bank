package com.bankingsystem.account_service.config;
import java.util.Collections;

import com.common_service.model.entity.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JWTtoUserConvertor implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt source) {
        User user = new User();
        user.setUsername(source.getSubject());
        return new UsernamePasswordAuthenticationToken(user, source, Collections.EMPTY_LIST);
    }


}