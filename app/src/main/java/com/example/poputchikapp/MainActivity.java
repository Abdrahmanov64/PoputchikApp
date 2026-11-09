package com.example.poputchikapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    EditText etFrom, etTo, etDate;
    Button btnSearch, btnMyBookings;
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
        btnMyBookings = findViewById(R.id.btnMyBookings);
        listViewTrips = findViewById(R.id.listViewTrips);
        tvNoResults = findViewById(R.id.tvNoResults);

        etDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                month = month + 1;
                String formattedMonth = (month < 10) ? "0" + month : String.valueOf(month);
                String formattedDay = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                etDate.setText(year + "-" + formattedMonth + "-" + formattedDay);
            }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        btnSearch.setOnClickListener(v -> searchTrips());

        btnMyBookings.setOnClickListener(v -> {
            startActivity(new Intent(this, MyBookingsActivity.class));
        });
        Button btnBackToRole = findViewById(R.id.btnBackToRoleSelect);
        btnBackToRole.setOnClickListener(v -> finish()); // finish() закрывает экран и возвращает на предыдущий (RoleSelectActivity)

        searchTrips();
    }

    private void searchTrips() {
        String from = etFrom.getText().toString().trim();
        String to = etTo.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        Cursor cursor = dbHelper.getTrips(from, to, date);

        if (cursor.getCount() == 0) {
            listViewTrips.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            listViewTrips.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this, R.layout.item_trip, cursor,
                    new String[]{DatabaseHelper.COL_TRIP_FROM, DatabaseHelper.COL_TRIP_TO, DatabaseHelper.COL_TRIP_DATE, "free_seats"},
                    new int[]{R.id.tvItemRoute, R.id.tvItemRoute, R.id.tvItemDate, R.id.tvItemPrice},
                    0
            );

            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor c, int columnIndex) {
                    if (view.getId() == R.id.tvItemRoute) {
                        String f = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_FROM));
                        String t = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_TO));
                        ((TextView) view).setText(f + " ➔ " + t);
                        return true;
                    }
                    if (view.getId() == R.id.tvItemPrice) {
                        // Показываем цену и свободные места
                        int free = c.getInt(c.getColumnIndexOrThrow("free_seats"));
                        double price = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_PRICE));
                        ((TextView) view).setText(price + " руб. (Свободно: " + free + ")");
                        return true;
                    }
                    return false;
                }
            });

            listViewTrips.setAdapter(adapter);

            // Обработка клика - диалог выбора количества мест
            listViewTrips.setOnItemClickListener((parent, view, position, id) -> {
                int tripId = (int) id;
                showSeatSelectionDialog(tripId);
            });
        }
    }

    // Диалог для выбора количества мест
    private void showSeatSelectionDialog(int tripId) {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Введите количество мест");

        new AlertDialog.Builder(this)
                .setTitle("Бронирование")
                .setMessage("Сколько мест вы хотите забронировать?")
                .setView(input)
                .setPositiveButton("Забронировать", (dialog, which) -> {
                    String seatsStr = input.getText().toString();
                    if (seatsStr.isEmpty()) {
                        Toast.makeText(this, "Введите количество!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int seats = Integer.parseInt(seatsStr);

                    // Пытаемся создать бронь
                    boolean success = dbHelper.createBooking(tripId, 2, seats); // 2 - тестовый пассажир

                    if (success) {
                        Toast.makeText(this, "Забронировано " + seats + " мест! Ожидайте подтверждения.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Ошибка: не хватает свободных мест!", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}