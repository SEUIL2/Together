package com.together.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "notification_entity")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message; //알림 내용

    private String linkUrl; // 알림 클릭 시 이동 경로

    private boolean isRead = false; //읽었는지에 대한 여부

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date(); //알림 생성 날짜

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity receiver; //알림 수신자

}
