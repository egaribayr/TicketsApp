package com.tickets.model;


import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "users")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

 
    public static enum Role {
        ROLE_USER, ROLE_SUPPORT, ROLE_PRODUCT;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String userName;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}
