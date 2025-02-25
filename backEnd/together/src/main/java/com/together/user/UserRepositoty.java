package com.together.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepositoty extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserLoginId(String userLoginId);
    Optional<UserEntity> findByEmail(String userEmail);
}
