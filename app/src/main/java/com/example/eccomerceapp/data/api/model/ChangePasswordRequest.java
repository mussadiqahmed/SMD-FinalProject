package com.example.eccomerceapp.data.api.model;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {
    @SerializedName("currentPassword")
    public String currentPassword;

    @SerializedName("newPassword")
    public String newPassword;

    @SerializedName("confirmPassword")
    public String confirmPassword;
}


