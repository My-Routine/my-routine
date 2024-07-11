package com.mbti_j.myroutine.backend.model.entity;

import com.mbti_j.myroutine.backend.model.dto.request.UserInfoDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import java.sql.Date;
import java.time.LocalDate;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@ToString
@Entity
@EntityListeners(AuditingEntityListener.class) // Listener
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10)
    private String username;

    @Column(length = 64)
    private String passwordHash;

    @Column(length = 30, unique = true)
    @Email
    private String email;

    @Column(length = 11)
    private String phone; // 010-5092-6683 X 01050926683 O

    @Column(length = 20)
    private String img;

    @Column(length = 15)
    @CreationTimestamp
    private Date createdAt;

    @Column(length = 15)
    private Date deletedAt;

    @Column(length = 15)
    private String token;

    public User(){}

    @Builder
    public User(String username, String passwordHash, String email, String phone, String img, String token) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.phone = phone;
        this.img = img;
        this.token = token;
    }

    public UserInfoDto toUserInfoDto() {

        return UserInfoDto.builder()
                .id(this.getId())
                .email(this.getEmail())
                .img(this.getImg())
                .phone(this.getPhone())
                .username(this.getUsername()).build();
    }

    public void updateDeleteAt(LocalDate now){
        this.deletedAt = java.sql.Date.valueOf(now);
    }
}
