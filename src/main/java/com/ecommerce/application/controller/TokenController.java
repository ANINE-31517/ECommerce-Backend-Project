package com.ecommerce.application.controller;

import com.ecommerce.application.CO.RefreshTokenCO;
import com.ecommerce.application.VO.NewAccessTokenVO;
import com.ecommerce.application.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<NewAccessTokenVO> refreshAccessToken(@Valid @RequestBody RefreshTokenCO request) {
        NewAccessTokenVO responseVO = tokenService.newAccessTokenVO(request);
        return ResponseEntity.ok(responseVO);
    }
}
