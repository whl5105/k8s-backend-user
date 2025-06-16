package com.welab.k8s_backend_user.secret.jwt;

import com.welab.k8s_backend_user.secret.jwt.dto.TokenDto;
import com.welab.k8s_backend_user.secret.jwt.props.JwtConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private final JwtConfigProperties configProperties;
    private volatile SecretKey secretKey;

    private SecretKey getSecretKey() {
        if (secretKey == null) {
            synchronized (this) { //Double-Checked Locking
                if (secretKey == null) {
                    String secret = configProperties.getSecretKey();
                    byte[] decoded = Decoders.BASE64.decode(secret);
                    secretKey = Keys.hmacShaKeyFor(decoded);
                }

            }
        }
        return secretKey;
    }

    public TokenDto.AccessToken generateAccessToken(String userId, String deviceType) {
        TokenDto.JwtToken jwtToken = this.generateJwtToken(userId, deviceType, false);
        return new TokenDto.AccessToken(jwtToken);
    }

    public TokenDto.AccessRefreshToken generateAccessRefreshToken(String userId, String deviceType) {
        TokenDto.JwtToken accessJwtToken = this.generateJwtToken(userId, deviceType, false);
        TokenDto.JwtToken refreshJwtToken = this.generateJwtToken(userId, deviceType, true);
        return new TokenDto.AccessRefreshToken(accessJwtToken, refreshJwtToken);
    }


    //== 사용자의 정보를 바탕으로 JWT를 생성하고, 만료 시간과 함께 DTO로 반환 ==
    public TokenDto.JwtToken generateJwtToken(String userId, String deviceType, boolean refreshToken) {
        log.info("JWT 생성 시작 - userId: {}, deviceType: {}, refreshToken: {}", userId, deviceType, refreshToken);
        int tokenExpiresIn = tokenExpiresIn(refreshToken, deviceType);
        String tokenType = refreshToken ? "refresh" : "access";

        String token = Jwts.builder()
                .issuer("welab")
                .subject(userId)
                .claim("userId", userId)
                .claim("deviceType", deviceType)
                .claim("tokenType", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiresIn * 1000L))
                .signWith(getSecretKey())
                .header().add("typ", "JWT")
                .and()
                .compact();

        return new TokenDto.JwtToken(token, tokenExpiresIn);
    }

    //== refreshToken을 검증 로직
    public String validateJwtToken(String refreshToken){
        String userId = null;
        final Claims claims = this.verifyAndGetClaims(refreshToken);

        if(claims == null){
            return null;
        }

        Date expirationDate = claims.getExpiration();
        if(expirationDate == null || expirationDate.before(new Date())) {
            return null;
        }

        userId = claims.get("userId",String.class);

        String tokenType = claims.get("tokenType", String.class);
        if (!"refresh".equals(tokenType)){
            return null;
        }
        return userId;
    }

    private Claims verifyAndGetClaims(String token) {
        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }



    //== JWT의 만료 시간(expiration time) 계산 ==
    private int tokenExpiresIn(boolean refreshToken, String deviceType) {
        if(!refreshToken){
            return 60 * 15;
        }
        if(deviceType.equals("MOBILE")){
            return configProperties.getMobileExpiresIn();
        }else if(deviceType.equals("TABLET")) {
            return configProperties.getTabletExpiresIn();
        }
        return configProperties.getExpiresIn();

    }

}
