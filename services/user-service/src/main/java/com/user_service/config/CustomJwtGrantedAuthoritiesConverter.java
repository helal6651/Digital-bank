package com.user_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomJwtGrantedAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Get authorities from JWT claims
        Collection<String> authorities = jwt.getClaim("authorities");

        // Get scopes from JWT claims (if using standard OAuth2 scope claim)
        Collection<String> scopes = jwt.getClaimAsStringList("scope");

        return Stream.concat(
                        authorities.stream(),
                        scopes.stream().map(scope -> "SCOPE_" + scope)
                )
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}