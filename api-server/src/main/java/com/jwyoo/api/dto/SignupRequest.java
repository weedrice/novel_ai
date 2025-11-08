package com.jwyoo.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3-50자 사이여야 합니다")
    private String username;

    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    private String name; // 사용자 실명 (선택)

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 주소를 입력하세요")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, max = 100, message = "비밀번호는 6-100자 사이여야 합니다")
    private String password;
}