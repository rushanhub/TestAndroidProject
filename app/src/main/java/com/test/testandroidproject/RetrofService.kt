package com.test.testandroidproject

import com.test.testandroidproject.data.PostInfo
import com.test.testandroidproject.data.UserInfo
import retrofit2.Call
import retrofit2.http.GET

// Интерфейс для использования библиотеки Retrofit

interface RetrofService {

    /* К основной ссылке "https://jsonplaceholder.typicode.com/" добавляется "users" в результате
    получается такой запрос https://jsonplaceholder.typicode.com/users */

    @GET("users")
    fun getUsers() : Call<List<UserInfo>>

    // Аналогично получается такой запрос https://jsonplaceholder.typicode.com/posts
    @GET("posts")
    fun getPosts() : Call<List<PostInfo>>

}