package com.example.poputchikapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookingListActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    ListView listView;
    int tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_list);

        tripId = getIntent().getIntExtra("trip_id", -1);
        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.listViewBookings);

        Button btnBack = findViewById(R.id.btnBackFromBookings);
        btnBack.setOnClickListener(v -> finish());

        loadBookings();
    }

    private void loadBookings() {
        Cursor cursor = dbHelper.getBookingsForTrip(tripId);

        CursorAdapter adapter = new CursorAdapter(this, cursor, 0) {
            @Override
            public View newView(android.content.Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
            }

            @Override
            public void bindView(View view, android.content.Context context, Cursor cursor) {
                TextView tvName = view.findViewById(R.id.tvPassengerName);
                TextView tvStatus = view.findViewById(R.id.tvBookingStatus);
                Button btnConfirm = view.findViewById(R.id.btnConfirmBooking);
                Button btnReject = view.findViewById(R.id.btnRejectBooking);

                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_STATUS));
                int bookingId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));

                tvName.setText(name);
                tvStatus.setText("Статус: " + status);

                btnConfirm.setOnClickListener(v -> {
                    dbHelper.confirmBooking(bookingId);
                    Toast.makeText(context, "Заявка подтверждена!", Toast.LENGTH_SHORT).show();
                    loadBookings();
                });

                btnReject.setOnClickListener(v -> {
                    dbHelper.rejectBooking(bookingId);
                    Toast.makeText(context, "Заявка отклонена!", Toast.LENGTH_SHORT).show();
                    loadBookings();
                });
            }
        };

        listView.setAdapter(adapter);
    }
}