package com.sparta.yourname.service;

import com.sparta.yourname.dto.CommentResponseDto;
import com.sparta.yourname.dto.MemberResponseDto;
import com.sparta.yourname.dto.MemberSummaryDto;
import com.sparta.yourname.entity.Comment;
import com.sparta.yourname.entity.CommentLike;
import com.sparta.yourname.entity.User;
import com.sparta.yourname.exception.CustomError;
import com.sparta.yourname.repository.CommentLikeRepository;
import com.sparta.yourname.repository.CommentRepository;
import com.sparta.yourname.repository.UserRepository;
import com.sparta.yourname.security.UserDetailsImpl;
import com.sparta.yourname.util.CustomErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    public List<MemberSummaryDto> getAllMembers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> MemberSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .specialty(user.getSpecialty())
                        .mbti(user.getMbti())
                        .imageUrl(user.getImageUrl())
                        .build())
                .toList();
    }

    public MemberResponseDto getMemberDetails(Long userId, User myself) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomError(CustomErrorMessage.USER_NOT_EXIST));
        List<Comment> comments = commentRepository.findAllByUserId(userId);

        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

        for (Comment comment : comments) {
            CommentResponseDto commentResponseDto = new CommentResponseDto(comment,false, comment.getAuthor_imageUrl());
            for (CommentLike commentLike : comment.getCommentLikeList()) {
                if (myself.getUserId().equals(commentLike.getUser().getUserId())) {
                    commentResponseDto.setEnabled(commentLike.isEnable());
                    break;
                }
            }
            commentResponseDtoList.add(commentResponseDto);
        }

//        List<CommentResponseDto> commentResponseDtoList = comments.stream()
//                .map(CommentResponseDto::new)
//                .collect(Collectors.toList());

        return new MemberResponseDto(user, commentResponseDtoList);
    }

    public Comment createComment(Long userId, String content, Authentication authentication) {

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomError(CustomErrorMessage.USER_NOT_EXIST));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String currentUser = userDetails.getUsername();
        String author_imageUrl = userDetails.getUser().getImageUrl();


        Comment comment = new Comment(user, content, currentUser, author_imageUrl);
        return commentRepository.save(comment);
    }

//    public Comment updateComment(Long commentId, String content, Authentication authentication) {
//        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//        Long currentUserId = userDetails.getUser().getId();
//
//        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));
//        Long commentAuthorId = comment.getUser().getId();
//
//        if (!currentUserId.equals(commentAuthorId)) {
//            throw new IllegalStateException("작성자가 아닙니다");
//        }
//
//        comment.updateContent(content);
//        return commentRepository.save(comment);
//    }

    public void deleteComment(Long commentId, Authentication authentication) {
        //여기가 현재 로그인한 사람의 정보
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String currentUserId = userDetails.getUsername();

        //
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomError(CustomErrorMessage.COMMENT_NOT_EXIST));
        String commentAuthorId = comment.getUsername();

        if (!currentUserId.equals(commentAuthorId)) {
            throw new CustomError(CustomErrorMessage.WRONG_USER);
        }

        commentRepository.delete(comment);
    }

    public void likeComment(Long commentId, Authentication authentication) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new CustomError(CustomErrorMessage.COMMENT_NOT_EXIST)
        );
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        CommentLike commentLike = commentLikeRepository.findByCommentAndUser(comment, userDetails.getUser());
        if(commentLike == null ){
            CommentLike newCommentLike = new CommentLike(userDetails.getUser(), comment);
            commentLikeRepository.saveAndFlush(newCommentLike);
            comment.updateLike(true);
            newCommentLike.setEnable();
            comment.addCommentLike(newCommentLike);
        }
        else{
            if(!commentLike.isEnable()){
                comment.updateLike(true);
                commentLike.setEnable();
            }
            else {
                comment.updateLike(false);
                commentLike.setEnable();
            }
        }
    }

}
