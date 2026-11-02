package com.example.poputchikapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    EditText etFrom, etTo, etDate;
    Button btnSearch;
    ListView listViewTrips;
    TextView tvNoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        etFrom = findViewById(R.id.etFrom);
        etTo = findViewById(R.id.etTo);
        etDate = findViewById(R.id.etDate);
        btnSearch = findViewById(R.id.btnSearch);
        listViewTrips = findViewById(R.id.listViewTrips);
        tvNoResults = findViewById(R.id.tvNoResults);

        // Кнопка Поиска
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTrips();
            }
        });

        // Для теста сразу загрузим все поездки при старте
        searchTrips();

        // --- ЛОГИКА ДЛЯ ТЕСТИРОВАНИЯ ДИАГРАММ 2 И 3 ---
        Button btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        Button btnLeaveReview = findViewById(R.id.btnLeaveReview);

        // Симуляция действия Водителя (Диаграмма 2)
        btnConfirmBooking.setOnClickListener(v -> {
            dbHelper.confirmBooking(1); // Подтверждаем бронь с ID 1
            Toast.makeText(this, "Водитель подтвердил! Статус: confirmed. Push-уведомление отправлено пассажиру.", Toast.LENGTH_LONG).show();
        });

        // Симуляция действия Пассажира - Отзыв (Диаграмма 3)
        btnLeaveReview.setOnClickListener(v -> {
            int testTripId = 1;
            String status = dbHelper.getBookingStatus(testTripId);

            if (status.equals("confirmed")) {
                dbHelper.addReview(testTripId, 5, "Отличная поездка, довез быстро!");
                Toast.makeText(this, "Отзыв добавлен в БД! Рейтинг водителя обновлен.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Нельзя оставить отзыв! Сначала нажмите 'Подтвердить бронь'.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchTrips() {
        String from = etFrom.getText().toString().trim();
        String to = etTo.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        Cursor cursor = dbHelper.getTrips(from, to, date);

        if (cursor.getCount() == 0) {
            // Ветка "Нет"
            listViewTrips.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            // Ветка "Да"
            listViewTrips.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);

            String[] fromColumns = {
                    DatabaseHelper.COL_TRIP_FROM,
                    DatabaseHelper.COL_TRIP_TO,
                    DatabaseHelper.COL_TRIP_DATE,
                    DatabaseHelper.COL_TRIP_PRICE
            };

            int[] toViews = {
                    R.id.tvItemRoute,
                    R.id.tvItemDate,
                    R.id.tvItemPrice
            };

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    R.layout.item_trip,
                    cursor,
                    new String[]{
                            DatabaseHelper.COL_TRIP_FROM,
                            DatabaseHelper.COL_TRIP_TO,
                            DatabaseHelper.COL_TRIP_DATE,
                            DatabaseHelper.COL_TRIP_PRICE
                    },
                    new int[]{
                            R.id.tvItemRoute,
                            R.id.tvItemRoute,
                            R.id.tvItemDate,
                            R.id.tvItemPrice
                    },
                    0
            );

            // Красиво склеиваем "Москва ➔ Тверь"
            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (view.getId() == R.id.tvItemRoute) {
                        String fromCity = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_FROM));
                        String toCity = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_TO));
                        ((TextView) view).setText(fromCity + " ➔ " + toCity);
                        return true;
                    }
                    return false;
                }
            });

            listViewTrips.setAdapter(adapter);

            // Клик по поездке = создание брони
            listViewTrips.setOnItemClickListener((parent, view, position, id) -> {
                boolean success = dbHelper.createBooking((int)id, 2);
                if(success){
                    Toast.makeText(MainActivity.this, "Бронирование создано! Ожидайте подтверждения.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}