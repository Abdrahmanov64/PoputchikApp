package com.example.poputchikapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import androidx.appcompat.app.AppCompatActivity;

public class MyBookingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ListView listView = findViewById(R.id.listViewMyBookings);

        // Получаем брони для пассажира с ID = 2
        String query = "SELECT b.id AS _id, t.from_city, t.to_city, t.date, b.status, b.seats_booked " +
                "FROM bookings b JOIN trips t ON b.trip_id = t.id WHERE b.passenger_id = 2";
        Cursor cursor = db.rawQuery(query, null);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_list_item_2, cursor,
                new String[]{"from_city", "status"},
                new int[]{android.R.id.text1, android.R.id.text2},
                0
        );
        listView.setAdapter(adapter);
        Button btnBack = findViewById(R.id.btnBackFromMyBookings);
        btnBack.setOnClickListener(v -> finish()); // Возвращает на MainActivity (Поиск)
    }
}