package ru.tn.shinglass.dto.models

enum class Settings(val value: String) {
    URL_SETTINGS("serviceUrl"),
    USER_NAME_SETTINGS("serviceUserName"),
    USER_PASSWORD_SETTINGS("serviceUserPassword"),
}