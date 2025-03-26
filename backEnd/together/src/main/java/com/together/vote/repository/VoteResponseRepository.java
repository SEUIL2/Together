package com.together.vote.repository;

import com.together.vote.entity.VoteResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteResponseRepository extends JpaRepository<VoteResponseEntity, Long> {
    boolean existsByUserUserIdAndVoteVoteId(Long userId, Long voteId);
}
