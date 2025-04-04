package com.example.whatsalert.Repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper class for managing SQLite database operations.
 * This class follows the Singleton pattern to ensure only one instance of the database is used.
 *
 * Features:
 * - Stores reminders with attributes such as date, time, message, contact details, and notification status.
 * - Supports data encryption for sensitive fields like messages and contact numbers.
 * - Tracks deletion timestamps for record management.
 * - Automatically upgrades the database when the schema changes.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reminder.db";
    private static final int DATABASE_VERSION = 2;
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Table and column names
    public static final String TABLE_REMINDERS = "reminders";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_CONTACT_NUMBER = "contact_number";
    public static final String COLUMN_IS_NOTIFIED = "is_notified";
    public static final String COLUMN_DELETION_TIME = "deletion_time";
    public static final String COLUMN_CONTACT_NAME = "contact_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create the reminders table
        String CREATE_REMINDERS_TABLE = "CREATE TABLE " + TABLE_REMINDERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT NOT NULL, " +       // Store the date in YYYY-MM-DD format
                COLUMN_TIME + " TEXT NOT NULL, " +       // Store the time in HH:MM AM/PM format
                COLUMN_MESSAGE + " TEXT NOT NULL, " +// Encrypted message text
                COLUMN_CONTACT_NUMBER + " TEXT NOT NULL," +  // Encrypted contact number
                COLUMN_CONTACT_NAME + " TEXT, " +
                COLUMN_IS_NOTIFIED + " INTEGER DEFAULT 0, " +    // New column for notification tracking
                COLUMN_DELETION_TIME + " INTEGER" +  // Add the deletion_time column
                ")";
        db.execSQL(CREATE_REMINDERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old table if it exists and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }
}