package com.example.poputchikapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "poputchik.db";
    private static final int DATABASE_VERSION = 8; // Версия 8 из-за новых колонок

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_ROLE = "role";

    public static final String TABLE_TRIPS = "trips";
    public static final String COL_TRIP_ID = "id";
    public static final String COL_TRIP_DRIVER_ID = "driver_id";
    public static final String COL_TRIP_FROM = "from_city";
    public static final String COL_TRIP_TO = "to_city";
    public static final String COL_TRIP_DATE = "date";
    public static final String COL_TRIP_PRICE = "price";
    public static final String COL_TRIP_STATUS = "status";
    public static final String COL_TRIP_SEATS = "seats"; // Новая колонка: всего мест

    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COL_BOOK_ID = "id";
    public static final String COL_BOOK_TRIP_ID = "trip_id";
    public static final String COL_BOOK_PASSENGER_ID = "passenger_id";
    public static final String COL_BOOK_STATUS = "status"; // pending, confirmed
    public static final String COL_BOOK_SEATS = "seats_booked"; // Новая колонка: сколько мест забронировано

    public static final String TABLE_REVIEWS = "reviews";
    public static final String COL_REVIEW_ID = "id";
    public static final String COL_REVIEW_TRIP_ID = "trip_id";
    public static final String COL_REVIEW_RATING = "rating";
    public static final String COL_REVIEW_TEXT = "text";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_USER_ROLE + " TEXT)";

        String createTrips = "CREATE TABLE " + TABLE_TRIPS + " (" +
                COL_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRIP_DRIVER_ID + " INTEGER, " +
                COL_TRIP_FROM + " TEXT, " +
                COL_TRIP_TO + " TEXT, " +
                COL_TRIP_DATE + " TEXT, " +
                COL_TRIP_PRICE + " REAL, " +
                COL_TRIP_STATUS + " TEXT, " +
                COL_TRIP_SEATS + " INTEGER)"; // Новое поле

        String createBookings = "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                COL_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BOOK_TRIP_ID + " INTEGER, " +
                COL_BOOK_PASSENGER_ID + " INTEGER, " +
                COL_BOOK_STATUS + " TEXT, " +
                COL_BOOK_SEATS + " INTEGER)"; // Новое поле

        String createReviews = "CREATE TABLE " + TABLE_REVIEWS + " (" +
                COL_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REVIEW_TRIP_ID + " INTEGER, " +
                COL_REVIEW_RATING + " INTEGER, " +
                COL_REVIEW_TEXT + " TEXT)";

        db.execSQL(createUsers);
        db.execSQL(createTrips);
        db.execSQL(createBookings);
        db.execSQL(createReviews);

        // Тестовые данные (водитель с ID=1, у поездок по 2 места)
        db.execSQL("INSERT INTO users (id, name, role) VALUES (1, 'Иван (Водитель)', 'driver')");
        db.execSQL("INSERT INTO trips (driver_id, from_city, to_city, date, price, status, seats) VALUES (1, 'Москва', 'Тверь', '2026-11-20', 500, 'active', 2)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        onCreate(db);
    }

    // Поиск поездок (добавим вывод свободных мест)
    public Cursor getTrips(String from, String to, String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Сложный запрос, который считает занятые места в каждой поездке
        String query = "SELECT t.*, t." + COL_TRIP_ID + " AS _id, " +
                "(t." + COL_TRIP_SEATS + " - IFNULL((SELECT SUM(b." + COL_BOOK_SEATS + ") FROM " + TABLE_BOOKINGS + " b WHERE b." + COL_BOOK_TRIP_ID + " = t." + COL_TRIP_ID + " AND b." + COL_BOOK_STATUS + " != 'cancelled'), 0)) AS free_seats " +
                "FROM " + TABLE_TRIPS + " t WHERE t." + COL_TRIP_STATUS + " = 'active'";

        java.util.ArrayList<String> argsList = new java.util.ArrayList<>();

        if (from != null && !from.isEmpty()) {
            query += " AND t." + COL_TRIP_FROM + " LIKE ?";
            argsList.add("%" + from + "%");
        }
        if (to != null && !to.isEmpty()) {
            query += " AND t." + COL_TRIP_TO + " LIKE ?";
            argsList.add("%" + to + "%");
        }
        if (date != null && !date.isEmpty()) {
            query += " AND t." + COL_TRIP_DATE + " = ?";
            argsList.add(date);
        }

        return db.rawQuery(query, argsList.toArray(new String[0]));
    }

    // Создание поездки (водитель указывает места)
    public boolean addTrip(String from, String to, String date, double price, int seats, int driverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRIP_DRIVER_ID, driverId);
        values.put(COL_TRIP_FROM, from);
        values.put(COL_TRIP_TO, to);
        values.put(COL_TRIP_DATE, date);
        values.put(COL_TRIP_PRICE, price);
        values.put(COL_TRIP_STATUS, "active");
        values.put(COL_TRIP_SEATS, seats);

        return db.insert(TABLE_TRIPS, null, values) != -1;
    }

    // Создание брони (пассажир указывает, сколько мест берет)
    public boolean createBooking(int tripId, int passengerId, int seatsToBook) {
        // Проверяем, сколько мест свободно
        Cursor cursor = getTrips("", "", "");
        int freeSeats = 0;
        while (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_ID)) == tripId) {
                freeSeats = cursor.getInt(cursor.getColumnIndexOrThrow("free_seats"));
                break;
            }
        }
        cursor.close();

        if (seatsToBook > freeSeats) {
            return false; // Нельзя забронировать больше, чем есть
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BOOK_TRIP_ID, tripId);
        values.put(COL_BOOK_PASSENGER_ID, passengerId);
        values.put(COL_BOOK_STATUS, "pending");
        values.put(COL_BOOK_SEATS, seatsToBook);
        return db.insert(TABLE_BOOKINGS, null, values) != -1;
    }

    // Получить брони для конкретной поездки (для водителя)
    // Получить брони для конкретной поездки (для водителя)
    public Cursor getBookingsForTrip(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Используем LEFT JOIN на случай, если имя пассажира не найдется
        String query = "SELECT b." + COL_BOOK_ID + " AS _id, u." + COL_USER_NAME + ", b." + COL_BOOK_STATUS +
                " FROM " + TABLE_BOOKINGS + " b " +
                " LEFT JOIN " + TABLE_USERS + " u ON b." + COL_BOOK_PASSENGER_ID + " = u." + COL_USER_ID +
                " WHERE b." + COL_BOOK_TRIP_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(tripId)});
    }

    // Подтверждение брони водителем
    public void confirmBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BOOK_STATUS, "confirmed");
        db.update(TABLE_BOOKINGS, values, COL_BOOK_ID + " = ?", new String[]{String.valueOf(bookingId)});
    }
    // Отклонение брони
    public void rejectBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BOOK_STATUS, "rejected");
        db.update(TABLE_BOOKINGS, values, COL_BOOK_ID + " = ?", new String[]{String.valueOf(bookingId)});
    }

    // Метод для добавления отзыва
    public void addReview(int tripId, int rating, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REVIEW_TRIP_ID, tripId);
        values.put(COL_REVIEW_RATING, rating);
        values.put(COL_REVIEW_TEXT, text);
        db.insert(TABLE_REVIEWS, null, values);
    }
}