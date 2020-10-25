package se.monstermaria.modbusmaster.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Profile {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "ip_address")
    public String ipAddress;

    @ColumnInfo(name = "port")
    public int port;
}
