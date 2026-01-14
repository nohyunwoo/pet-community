package com.example.community.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name="user_id", nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String sex;

    @Column(name="user_date", nullable = false)
    private LocalDate userDate;

    @Column(name="reg_date", nullable = false)
    private LocalDate regDate;

    @PrePersist
    public void prePersist(){
        this.regDate = LocalDate.now();
    }
}
