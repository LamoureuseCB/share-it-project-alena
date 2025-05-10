package com.practice.shareitprojectalena.utils;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("checkstyle:Regexp")
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
