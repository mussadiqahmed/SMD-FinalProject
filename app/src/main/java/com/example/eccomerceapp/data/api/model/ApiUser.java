package com.example.eccomerceapp.data.api.model;

import com.google.gson.annotations.SerializedName;

public class ApiUser {
    @SerializedName("id")
    public Long id;

    @SerializedName("firstName")
    public String firstName;

    @SerializedName("lastName")
    public String lastName;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("email")
    public String email;

    @SerializedName("gender")
    public String gender;

    @SerializedName("createdAt")
    public String createdAt;
}


