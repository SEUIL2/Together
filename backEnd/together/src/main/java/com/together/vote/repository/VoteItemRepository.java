package com.together.vote.repository;

import com.together.vote.entity.VoteItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteItemRepository extends JpaRepository<VoteItemEntity, Long> {
}
