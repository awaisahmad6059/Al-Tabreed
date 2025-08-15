package com.aak.al_tabreed.Authentication.User.UserModel


data class UserRecentTask(
    val taskId: String = "",
    val category: String = "",
    val detail: String = "",
    val location: String = "",
    val status: String = "",
    val time: String = "",
    val userId: String = ""
)
