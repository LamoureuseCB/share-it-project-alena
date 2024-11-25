package com.practice.shareitprojectalena.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum State {
    ALL("Все"),
    CURRENT("текущие"),
    PAST("завершённые"),
    FUTURE("будущие"),
    WAITING("ожидающие подтверждения"),
    REJECTED("отклонённые");


    private final String message;
}
