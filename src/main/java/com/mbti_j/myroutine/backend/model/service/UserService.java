package com.mbti_j.myroutine.backend.model.service;

import com.mbti_j.myroutine.backend.model.dto.user.UserInfoResponseDto;
import com.mbti_j.myroutine.backend.model.dto.user.UserOtherDto;
import com.mbti_j.myroutine.backend.model.dto.user.UserSignUpDto;
import com.mbti_j.myroutine.backend.model.entity.User;
import com.mbti_j.myroutine.backend.repository.UserRepository;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Value("${profileUploadFolder}")
    private String profileUploadFolder;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthService authService;


    public User signUpUser(UserSignUpDto userSignUpDto) {
        userSignUpDto.setPassword(passwordEncoder.encode(userSignUpDto.getPassword()));
        User user = new User();
        return userRepository.save(user.signUpDtoToEntity(userSignUpDto));
    }

    public User signUpUser(UserSignUpDto userSignUpDto, MultipartFile profileImg) {
        System.out.println(profileUploadFolder);
        //파일 업로드 및 uuid 설정
        String uuidImg = "";
        if (!profileImg.isEmpty()) {
            uuidImg = imgUpload(profileImg);
            userSignUpDto.setImg(uuidImg);
        }
        userSignUpDto.setPassword(passwordEncoder.encode(userSignUpDto.getPassword()));
        //dto -> entity
        User user = new User();
        return userRepository.save(user.signUpDtoToEntity(userSignUpDto));
    }

    public String imgUpload(MultipartFile profileImg) {
        String originalFilename = profileImg.getOriginalFilename();
        File uploadPath = new File(profileUploadFolder, "uploadFolder");
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }
        String uuidImg = UUID.randomUUID() + "_" + originalFilename;
        File saveFile = new File(uploadPath, uuidImg);
        try {
            profileImg.transferTo(saveFile);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return "http://localhost:8082/uploadFolder/" + uuidImg;
    }

    @Transactional
    public void withdrawUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        //Dirty Checking
        userOptional.ifPresent(user -> {
            //토큰처리

            user.updateDeletedAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);

        });

    }

    public UserInfoResponseDto getMyInfo(Long userId) {
        Optional<User> userOptional = userRepository.findMyDtoByIdAndDeletedAtNull(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return UserInfoResponseDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .phone(user.getPhone())
                    .createAt(new Date(user.getCreatedAt().getTime()))
                    .img(user.getImg())
                    .build();
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public UserOtherDto getOtherInfo(Long userId) {
        return userRepository.findOtherDtoByIdAndDeletedAtNull(userId).orElse(null);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Transactional
    public void updateProfileImg(MultipartFile profileImg) {
        Long userId = authService.getLoginUser().getId();
        String imgPath = imgUpload(profileImg);
        userRepository.updateProfileImg(userId, imgPath);
    }

    @Transactional
    public boolean checkPassword(String inputPassword) {
        Long userId = authService.getLoginUser().getId();
        String getPassword = userRepository.findPasswordById(userId);

        return passwordEncoder.matches(inputPassword, getPassword);
    }

    @Transactional
    public void updatePassword(String newPassword) {
        Long userId = authService.getLoginUser().getId();
        String passwordHash = passwordEncoder.encode(newPassword);
        System.out.println("new PasswordHash : " + passwordHash);
        userRepository.updatePasswordById(userId, passwordHash);

    }
}
