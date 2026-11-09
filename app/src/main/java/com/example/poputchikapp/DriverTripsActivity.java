package com.example.poputchikapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DriverTripsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_trips);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        ListView listView = findViewById(R.id.listViewDriverTrips);

        // Получаем все поездки (у нас тестовый ID водителя = 1)
        Cursor cursor = dbHelper.getTrips("", "", "");

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, R.layout.item_trip, cursor,
                new String[]{DatabaseHelper.COL_TRIP_FROM, DatabaseHelper.COL_TRIP_TO, DatabaseHelper.COL_TRIP_DATE},
                new int[]{R.id.tvItemRoute, R.id.tvItemRoute, R.id.tvItemDate},
                0
        );

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(android.view.View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.tvItemRoute) {
                    String from = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_FROM));
                    String to = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_TO));
                    ((TextView) view).setText(from + " ➔ " + to);
                    return true;
                }
                return false;
            }
        });
        listView.setAdapter(adapter);

        // --- ВОТ ЗДЕСЬ БЫЛА ОШИБКА. ДОЛЖЕН БЫТЬ ПЕРЕХОД НА BookingListActivity ---
        listView.setOnItemClickListener((parent, view, position, id) -> {
            android.util.Log.d("TRIP_DEBUG", "Нажата поездка с ID: " + id);
            Intent intent = new Intent(DriverTripsActivity.this, BookingListActivity.class);
            intent.putExtra("trip_id", (int) id); // Передаем ID поездки
            startActivity(intent);
        });
        // Кнопка Назад
        Button btnBack = findViewById(R.id.btnBackFromDriverTrips);
        btnBack.setOnClickListener(v -> finish()); // Закрывает этот экран и возвращает на предыдущий
    }
}