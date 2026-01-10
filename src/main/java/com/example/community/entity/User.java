package com.example.community.entity;


import jakarta.persistence.*;
import lombok.*;

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
    private String displayName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;
}
