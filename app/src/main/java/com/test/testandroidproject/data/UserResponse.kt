package com.test.testandroidproject.data

import com.google.gson.annotations.SerializedName

// Класс принимает данные из запроса https://jsonplaceholder.typicode.com/users

class UserInfo {
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("username")
    var username: String? = null

    @SerializedName("email")
    var email: String? = null

    @SerializedName("address")
    var address: UserAddress? = null

    @SerializedName("phone")
    var phone: String? = null

    @SerializedName("website")
    var website: String? = null

    @SerializedName("company")
    var company: UserCompany? = null
}

class UserGeo {
    @SerializedName("lat")
    var lat: String? = null

    @SerializedName("lng")
    var lng: String? = null
}

class UserAddress {
    @SerializedName("street")
    var street: String? = null

    @SerializedName("suite")
    var suite: String? = null

    @SerializedName("city")
    var city: String? = null

    @SerializedName("zipcode")
    var zipcode: String? = null

    @SerializedName("geo")
    var userGeo: UserGeo? = null
}

class UserCompany {
    @SerializedName("name")
    var name: String? = null

    @SerializedName("catchPhrase")
    var catchPhrase: String? = null

    @SerializedName("bs")
    var bs: String? = null
}