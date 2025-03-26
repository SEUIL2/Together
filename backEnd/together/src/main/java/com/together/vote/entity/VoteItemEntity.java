package com.together.vote.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vote_item_entity")
public class VoteItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_item_id")
    private Long voteItemId; // 항목 ID

    private String options;  // 항목 내용

    @ManyToOne
    @JoinColumn(name = "vote_id")
    @JsonIgnore  // 순환 참조 방지
    @ToString.Exclude
    private VoteEntity vote; // 해당 투표와 연결

    @OneToMany(mappedBy = "voteItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<VoteResponseEntity> voteResponse = new ArrayList<>();

}
