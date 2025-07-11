package com.welab.k8s_backend_user.secret.jwt.dto;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtToken {
        private String token; //토큰 정보
        private Integer expiresIn; // 만료정보
    }
    @Getter
    @RequiredArgsConstructor
    public static class AccessToken {
        private final JwtToken access;
    }
    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class AccessRefreshToken {
        private final JwtToken access;
        private final JwtToken refresh;
    }
}