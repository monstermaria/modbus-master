package se.monstermaria.modbusmaster.database;

import androidx.room.ColumnInfo;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.RoomDatabase;

@Database(entities = {DataBase.Profile.class}, exportSchema = false, version = 1)
public abstract class DataBase extends RoomDatabase {
    public abstract ProfileDao profileDao();

    @Entity
    public static class Profile {
        @PrimaryKey
        public int id;

        @ColumnInfo(name = "name")
        public String name;

        @ColumnInfo(name = "ip_address")
        public String ipAddress;

        @ColumnInfo(name = "port")
        public String port;
    }
}
