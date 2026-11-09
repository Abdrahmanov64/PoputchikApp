package com.example.poputchikapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_select);

        Button btnDriver = findViewById(R.id.btnRoleDriver);
        Button btnPassenger = findViewById(R.id.btnRolePassenger);

        // Переход на экран Водителя
        btnDriver.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTripActivity.class));
        });

        // Переход на экран Пассажира (это будет наш старый MainActivity)
        btnPassenger.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });
    }
}