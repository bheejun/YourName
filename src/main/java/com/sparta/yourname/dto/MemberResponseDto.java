package com.sparta.yourname.dto;

import com.sparta.yourname.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class MemberResponseDto {
    private UserResponseDto user;
    private List<CommentResponseDto> comments;

    public MemberResponseDto(User user, List<CommentResponseDto> comments) {
        this.user = UserResponseDto.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .specialty(user.getSpecialty())
                .mbti(user.getMbti())
                .githubUrl(user.getGithuburl())
                .blogUrl(user.getBlogurl())
                .imageUrl(user.getImageUrl())
                .build();
        this.comments = comments;
    }
}
