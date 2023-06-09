package com.sparta.yourname.repository;

import com.sparta.yourname.entity.RefreshToken;
import com.sparta.yourname.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> deleteByUser(User user);
}
