package se.monstermaria.modbusmaster.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Profile {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "ip_address")
    public String ipAddress;

    @ColumnInfo(name = "port")
    public int port = 502;

    @Override
    public String toString() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (ipAddress != null && !ipAddress.isEmpty()) {
            return ipAddress;
        }
        return null;
    }
}
