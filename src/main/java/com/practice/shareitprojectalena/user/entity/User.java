package com.practice.shareitprojectalena.user.entity;


import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder

public class User {

    private Long id;
    private String name;
    private String email;


}
