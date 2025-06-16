package com.welab.k8s_backend_user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiteUserLoginDto {
    @NotBlank(message = "사용자 아이디를 입력하세요.")
    private String userId;
    @NotBlank(message = "사용자 비밀번호를 입력하세요.")
    private String password;
}
