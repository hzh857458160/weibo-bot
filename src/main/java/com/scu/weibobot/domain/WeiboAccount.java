package com.scu.weibobot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WeiboAccount {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long accountId;

    private String username;

    private String password;
}
