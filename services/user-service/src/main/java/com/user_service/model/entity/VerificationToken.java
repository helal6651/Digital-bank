package com.user_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "verification_tokens")
public class VerificationToken implements Serializable {
    @Id
    @Column(name = "token")
    private String token;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "created_on", nullable = false)
    private ZonedDateTime createdOn;

    @Column(name = "expiration_time", nullable = false)
    private Long expirationTime;

    @Column(name = "service-name", nullable = false)
    private String serviceName;

    @Column(name = "token_type")
    private Integer tokenType;
}
