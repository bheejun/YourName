package com.sparta.yourname.service;

import com.sparta.yourname.dto.CommonResponseDto;
import com.sparta.yourname.dto.TokenDto;
import com.sparta.yourname.dto.UserRequestDto;
import com.sparta.yourname.entity.RefreshToken;
import com.sparta.yourname.entity.User;
import com.sparta.yourname.exception.CustomError;
import com.sparta.yourname.jwt.JwtUtil;
import com.sparta.yourname.repository.RefreshTokenRepository;
import com.sparta.yourname.repository.UserRepository;
import com.sparta.yourname.util.CustomErrorMessage;
import com.sparta.yourname.util.S3Uploader;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.sparta.yourname.util.ValidateUtil.isValidPassword;
import static com.sparta.yourname.util.ValidateUtil.isValidUsername;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3Uploader s3Uploader;

    @Transactional
    public CommonResponseDto<?> login(UserRequestDto.login requestDto, HttpServletResponse response) {
        User user = userRepository.findByUserId(requestDto.getUserId()).orElseThrow(
                () -> new CustomError(CustomErrorMessage.ID_NOT_EXIST)
        );

        if (!passwordEncoder.matches(requestDto.getPassword(),user.getPassword())) {
            throw new CustomError(CustomErrorMessage.WRONG_PASSWORD); // 디테일한 예외 클래스 필요
        }

        TokenDto tokenDto = jwtUtil.createAllToken(user.getUserId());

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUser(user);

        // refreshToken이 존재할 경우 토큰 업데이트
        if (refreshToken.isPresent()) {
            refreshToken.get().updateToken(tokenDto.getRefreshToken());
        } else { // 존재하지 않을 경우 새로 발급
            RefreshToken newToken = RefreshToken.saveToken(tokenDto.getRefreshToken(), user);
            refreshTokenRepository.save(newToken);
        }

        response.addHeader(JwtUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
        System.out.println(tokenDto.getAccessToken());
        response.addHeader(JwtUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());

        return new CommonResponseDto<>("로그인 성공");
    }

    // 이미지 업로드 기능 추가 시 적용
    // public CommonResponseDto<?> signup(UserRequestDto.info userRequestDto, MultipartFile image) throws IOException
    @Transactional
    public CommonResponseDto<?> signup(UserRequestDto.info userRequestDto) {

        String userId = userRequestDto.getUserId();
        String password = userRequestDto.getPassword();
        // username과 password의 유효성 검사
        boolean isUsernameValid = isValidUsername(userId);
        boolean isPasswordValid = isValidPassword(password);

        // 유효성 검사 결과 출력
        System.out.println("Username is valid: " + isUsernameValid);
        System.out.println("Password is valid: " + isPasswordValid);
        password = passwordEncoder.encode(password);
        userRequestDto.setPassword(password);
        // 회원 중복 확인
        Optional<User> found = userRepository.findByUserId(userId);
        if (found.isPresent()) {
            throw new CustomError(CustomErrorMessage.USER_EXISTS);
        }
//        try {
//              Optional<User> found = userRepository.findByUserId(userId);
//               if (found.isPresent()) {
//                throw new CustomError(CustomErrorMessage.USER_EXISTS);
//                }
//        } catch (Exception e) {
//            // 예외 처리
//            e.printStackTrace();
//            throw new CustomError(CustomErrorMessage.SEARCHING_USER_ERROR);
//        }

        User user = new User(userRequestDto);

        // 이미지 처리
//        if (image == null || image.isEmpty()) {
//            user.setImageUrl(randomImageUrl());
//        } else {
//            String storedImageUrl = s3Uploader.upload(image, "images");
//            user.setImageUrl(storedImageUrl);
//        }

        user.setImageUrl(randomImageUrl());

        userRepository.save(user);
        return new CommonResponseDto<>("회원 가입 성공");
    }

    @Transactional
    public CommonResponseDto<?> delete(UserRequestDto.info userRequestDto) {

        userRepository.findByUserId(userRequestDto.getUserId()).orElseThrow(
                () -> new CustomError(CustomErrorMessage.USER_NOT_EXIST)
        );
        User user = userRepository.findByUserId(userRequestDto.getUserId()).orElseGet(User::new);

        // 해당 사용자와 관련된 refresh_token 레코드 삭제
        refreshTokenRepository.deleteByUser(user);

        userRepository.delete(user);
        return new CommonResponseDto<>("회원 탈퇴 성공");
    }

    public String randomImageUrl() {
        String bucketName = "myimageshop";
        String objectKey = "/randomImages/random";
        objectKey = objectKey + (int) (Math.random() * 7 + 1) + ".png";

        String imageUrl = " https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com" + objectKey;

        return imageUrl;
    }
}
