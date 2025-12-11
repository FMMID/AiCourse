package com.example.aicourse.domain.settings.model

enum class HistoryStrategy {

    //TODO используем когда учитывается вся история переписки
    PAIN,

    //TODO в этом режиме не отправляем историю, только сообщение пользователя
    ONE_MESSAGE,

    //TODO в этом режиме работает "ужимка" старой части беседы, для поддержания актулаьности контекстного окна
    SUMMERIZE
}