package com.test.testandroidproject.data

import com.google.gson.annotations.SerializedName

// Класс принимает данные из запроса https://jsonplaceholder.typicode.com/posts

class PostInfo {
    @SerializedName("userId")
    var userId: Int? = null

    @SerializedName("id")
    var id: Int? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("body")
    var body: String? = null
}