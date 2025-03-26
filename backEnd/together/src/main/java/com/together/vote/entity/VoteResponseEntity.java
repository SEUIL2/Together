package com.together.vote.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vote_response_entity",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "vote_id"})  // 유저 + 투표 조합 중복 방지
        })
public class VoteResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private Long responseId;  // 응답 ID

    @ManyToOne
    @JoinColumn(name = "vote_item_id")
    @JsonIgnore
    private VoteItemEntity voteItem;  // 선택된 투표 항목

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;  // 투표한 사용자

    @ManyToOne
    @JoinColumn(name = "vote_id")
    @JsonIgnore
    private VoteEntity vote;  // 해당 투표

}
