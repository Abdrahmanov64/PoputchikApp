package com.example.poputchikapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PassengerListActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_list);

        dbHelper = new DatabaseHelper(this);
        ListView listView = findViewById(R.id.listViewPassengers);
        Button btnBack = findViewById(R.id.btnBackFromPassengers);

        btnBack.setOnClickListener(v -> finish());

        // Получаем список пассажиров, чьи заявки были ПОДТВЕРЖДЕНЫ
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT b.id AS _id, u.name, t.from_city, t.to_city, t.date " +
                "FROM bookings b " +
                "JOIN users u ON b.passenger_id = u.id " +
                "JOIN trips t ON b.trip_id = t.id " +
                "WHERE b.status = 'confirmed'";

        Cursor cursor = db.rawQuery(query, null);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_list_item_2, cursor,
                new String[]{"name", "from_city"},
                new int[]{android.R.id.text1, android.R.id.text2},
                0
        );

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor c, int columnIndex) {
                if (view.getId() == android.R.id.text2) {
                    String from = c.getString(c.getColumnIndexOrThrow("from_city"));
                    String to = c.getString(c.getColumnIndexOrThrow("to_city"));
                    String date = c.getString(c.getColumnIndexOrThrow("date"));
                    ((TextView) view).setText("Поездка: " + from + " ➔ " + to + " (" + date + ")");
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(adapter);

        // Клик по пассажиру -> диалог отзыва
        listView.setOnItemClickListener((parent, view, position, id) -> {
            showReviewDialog();
        });
    }

    private void showReviewDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Введите текст отзыва...");

        new AlertDialog.Builder(this)
                .setTitle("Оставить отзыв")
                .setMessage("Напишите отзыв о пассажире:")
                .setView(input)
                .setPositiveButton("Отправить (5 звезд)", (dialog, which) -> {
                    String text = input.getText().toString();
                    if (text.isEmpty()) text = "Хороший пассажир";

                    // Оставляем отзыв (ID поездки ставим 1 для простоты)
                    dbHelper.addReview(1, 5, text);
                    Toast.makeText(this, "Отзыв отправлен!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}