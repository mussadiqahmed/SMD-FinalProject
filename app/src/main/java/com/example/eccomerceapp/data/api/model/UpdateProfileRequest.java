package com.example.eccomerceapp.data.api.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("fullName")
    public String fullName;

    @SerializedName("email")
    public String email;
}


