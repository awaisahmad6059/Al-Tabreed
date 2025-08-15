package com.aak.al_tabreed.Authentication.Admin.AdminModel

data class NewOrder(
    val orderId: String = "",
    val userId: String = "",
    val category: String = "",
    val detail: String = "",
    val location: String = "",
    val status: String = "",
    val time: String = "",
    var username: String = "",
    var profileImageUrl: String = "",
    var imageUrl: String = ""
)