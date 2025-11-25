package com.example.eccomerceapp.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eccomerceapp.R;

public class ToastHelper {
    public static void showToastWithLogo(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View toastView = inflater.inflate(R.layout.custom_toast, null);
        
        TextView messageText = toastView.findViewById(R.id.toastMessage);
        messageText.setText(message);
        
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastView);
        toast.show();
    }
}

