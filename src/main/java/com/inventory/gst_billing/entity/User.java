package com.inventory.gst_billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id  //primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY) // ESTABLISH RELATIONSHIP, 1 ROLE SHARED BY MANY USERS. LAZY FETCH FOR OTPIMIZATION.
    @JoinColumn(name = "role_id", nullable = false)  // ROLE IS FOREGIN KEY HERE, ROLE TABLE SEPERATE
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id")
    private Store store; // Nullable because Owner/Dev are not store specific, eager fetch so can avoid transactional

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;


}