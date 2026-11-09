package com.example.poputchikapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddTripActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        EditText etFrom = findViewById(R.id.etNewFrom);
        EditText etTo = findViewById(R.id.etNewTo);
        EditText etDate = findViewById(R.id.etNewDate);
        EditText etPrice = findViewById(R.id.etNewPrice);
        EditText etSeats = findViewById(R.id.etNewSeats); // Новое поле
        Button btnSave = findViewById(R.id.btnSaveTrip);
        Button btnBack = findViewById(R.id.btnBackToRole);

        // Календарь
        etDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                month = month + 1;
                String formattedMonth = (month < 10) ? "0" + month : String.valueOf(month);
                String formattedDay = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                etDate.setText(year + "-" + formattedMonth + "-" + formattedDay);
            }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        btnSave.setOnClickListener(v -> {
            String from = etFrom.getText().toString();
            String to = etTo.getText().toString();
            String date = etDate.getText().toString();
            String priceStr = etPrice.getText().toString();
            String seatsStr = etSeats.getText().toString();

            if (from.isEmpty() || to.isEmpty() || date.isEmpty() || priceStr.isEmpty() || seatsStr.isEmpty()) {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            int seats = Integer.parseInt(seatsStr);
            if (seats <= 0) {
                Toast.makeText(this, "Мест должно быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.addTrip(from, to, date, price, seats, 1);
            if (success) {
                Toast.makeText(this, "Поездка опубликована!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка при создании", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnDriverTrips = findViewById(R.id.btnDriverTrips);
        btnDriverTrips.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, DriverTripsActivity.class));
        });

        Button btnMyPassengers = findViewById(R.id.btnMyPassengers);
        btnMyPassengers.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, PassengerListActivity.class));
        });

        btnBack.setOnClickListener(v -> finish());
    }
}