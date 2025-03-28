package com.together.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserLoginId(String userLoginId);
    Optional<UserEntity> findByUserEmail(String userEmail);
    List<UserEntity> findByUserEmailContaining(String email);
}
