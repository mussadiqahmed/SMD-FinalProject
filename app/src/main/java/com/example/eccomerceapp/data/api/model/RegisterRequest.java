package com.example.eccomerceapp.data.api.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("firstName")
    public String firstName;

    @SerializedName("lastName")
    public String lastName;

    @SerializedName("gender")
    public String gender;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("confirmPassword")
    public String confirmPassword;
}

