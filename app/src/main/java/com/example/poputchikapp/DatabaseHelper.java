package com.example.poputchikapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "poputchik.db";
    private static final int DATABASE_VERSION = 2; // Увеличили версию для добавления таблицы отзывов

    // Таблица пользователей
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_ROLE = "role"; // driver / passenger

    // Таблица поездок (маршруты)
    public static final String TABLE_TRIPS = "trips";
    public static final String COL_TRIP_ID = "id";
    public static final String COL_TRIP_DRIVER_ID = "driver_id";
    public static final String COL_TRIP_FROM = "from_city";
    public static final String COL_TRIP_TO = "to_city";
    public static final String COL_TRIP_DATE = "date";
    public static final String COL_TRIP_PRICE = "price";
    public static final String COL_TRIP_STATUS = "status"; // active, booked

    // Таблица бронирований
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COL_BOOK_ID = "id";
    public static final String COL_BOOK_TRIP_ID = "trip_id";
    public static final String COL_BOOK_PASSENGER_ID = "passenger_id";
    public static final String COL_BOOK_STATUS = "status"; // pending, confirmed

    // Таблица отзывов (Добавлено для 3-й диаграммы)
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
        // Создание таблиц
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
                COL_TRIP_STATUS + " TEXT)";

        String createBookings = "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                COL_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BOOK_TRIP_ID + " INTEGER, " +
                COL_BOOK_PASSENGER_ID + " INTEGER, " +
                COL_BOOK_STATUS + " TEXT)";

        String createReviews = "CREATE TABLE " + TABLE_REVIEWS + " (" +
                COL_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REVIEW_TRIP_ID + " INTEGER, " +
                COL_REVIEW_RATING + " INTEGER, " +
                COL_REVIEW_TEXT + " TEXT)";

        db.execSQL(createUsers);
        db.execSQL(createTrips);
        db.execSQL(createBookings);
        db.execSQL(createReviews);

        // Тестовые данные
        db.execSQL("INSERT INTO users (name, role) VALUES ('Иван (Водитель)', 'driver')");
        db.execSQL("INSERT INTO trips (driver_id, from_city, to_city, date, price, status) VALUES (1, 'Москва', 'Тверь', '2023-11-20', 500, 'active')");
        db.execSQL("INSERT INTO trips (driver_id, from_city, to_city, date, price, status) VALUES (1, 'Москва', 'Тверь', '2023-11-21', 450, 'active')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        onCreate(db);
    }

    // Выборка поездок по маршруту и дате
    public Cursor getTrips(String from, String to, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, " + COL_TRIP_ID + " AS _id FROM " + TABLE_TRIPS + " WHERE " +
                COL_TRIP_FROM + " LIKE ? AND " +
                COL_TRIP_TO + " LIKE ? AND " +
                COL_TRIP_STATUS + " = 'active'";
        String[] args = new String[]{"%" + from + "%", "%" + to + "%"};

        if (date != null && !date.isEmpty()) {
            query += " AND " + COL_TRIP_DATE + " = ?";
            args = new String[]{"%" + from + "%", "%" + to + "%", date};
        }
        return db.rawQuery(query, args);
    }

    // Запрос на бронирование
    public boolean createBooking(int tripId, int passengerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BOOK_TRIP_ID, tripId);
        values.put(COL_BOOK_PASSENGER_ID, passengerId);
        values.put(COL_BOOK_STATUS, "pending");

        long result = db.insert(TABLE_BOOKINGS, null, values);
        return result != -1;
    }

    // Водитель подтверждает
    public void confirmBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BOOK_STATUS, "confirmed");
        db.update(TABLE_BOOKINGS, values, COL_BOOK_ID + " = ?", new String[]{String.valueOf(bookingId)});
    }

    // Пассажир оставляет отзыв
    public void addReview(int tripId, int rating, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REVIEW_TRIP_ID, tripId);
        values.put(COL_REVIEW_RATING, rating);
        values.put(COL_REVIEW_TEXT, text);
        db.insert(TABLE_REVIEWS, null, values);
    }

    // Проверить статус брони
    public String getBookingStatus(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_BOOK_STATUS + " FROM " + TABLE_BOOKINGS +
                " WHERE " + COL_BOOK_TRIP_ID + " = ?", new String[]{String.valueOf(tripId)});
        if (cursor.moveToFirst()) {
            String status = cursor.getString(0);
            cursor.close();
            return status;
        }
        return "none";
    }
}