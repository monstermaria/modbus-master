package se.monstermaria.modbusmaster.database;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Profile.class}, exportSchema = false, version = 1)
public abstract class DataBase extends RoomDatabase {
    public abstract ProfileDao profileDao();

    private static DataBase db = null;

    public static DataBase getDataBase(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context, DataBase.class, "modbus_database").build();
        }
        return db;
    }
}
