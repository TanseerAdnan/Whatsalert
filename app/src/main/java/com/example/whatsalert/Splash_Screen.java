package com.example.whatsalert;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.whatsalert.Util.Constant;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.splash_screen);

        // Set the status bar color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.Secondary_Color));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.Secondary_Color));

        // Find the TextViews
        TextView appName = findViewById(R.id.app_name);
        TextView tagline = findViewById(R.id.tagline);

        // Load animations
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        // Start animations
        appName.startAnimation(slideInLeft);
        tagline.startAnimation(slideInRight);

        // Change splash screen to home screen after delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash_Screen.this, Home_Screen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, Constant.SPLASH_SCREEN_TIMEOUT);

        // Adjust for system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }
}