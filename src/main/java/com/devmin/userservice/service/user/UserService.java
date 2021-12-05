package com.devmin.userservice.service.user;

import com.devmin.userservice.component.JwtUtil;
import com.devmin.userservice.domain.user.User;
import com.devmin.userservice.domain.user.UserRepository;
import com.devmin.userservice.web.dto.user.UserLoginRequestDto;
import com.devmin.userservice.web.dto.user.UserLoginResponseDto;
import com.devmin.userservice.web.dto.user.UserResponseDto;
import com.devmin.userservice.web.dto.user.UserSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto){
        String username = userLoginRequestDto.getUsername();
        String password = userLoginRequestDto.getPassword();

        //계정 확인
        User entity = userRepository.findByUsername(username)
                .orElseThrow(()-> new IllegalArgumentException("해당 ID가 없습니다. ID=" + username));
        if(!passwordEncoder.matches(password, entity.getPassword()))
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");

        //토큰 발급
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtUtil.createAccessToken(entity));
        tokens.put("refreshToken", jwtUtil.createRefreshToken(entity));

        entity.updateRefreshToken(tokens.get("refreshToken"));
        return new UserLoginResponseDto(entity, tokens);
    }

    public UserResponseDto findById(Long id){
        User user =  userRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id));
        return new UserResponseDto(user);
    }

    @Transactional
    public Long save(UserSaveRequestDto userSaveRequestDto){
        userSaveRequestDto.encryptPassword(passwordEncoder.encode(userSaveRequestDto.getPassword()));
        return userRepository.save( userSaveRequestDto.toEntity() ).getId();
    }
}
