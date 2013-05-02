package com.rcplatform.phototalk.utils;

import android.database.sqlite.SQLiteStatement;

public abstract class SQLiteUtil {

    public static void bindString(SQLiteStatement statement, int index, String value) {
        if (statement != null) {
            if (value != null) {
                statement.bindString(index, value);
            } else {
                statement.bindNull(index);
            }
        }
    }

}
