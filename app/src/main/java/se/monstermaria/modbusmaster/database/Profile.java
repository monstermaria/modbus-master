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

    @ColumnInfo(name = "use_serial")
    public boolean useSerial = false;

    @ColumnInfo(name = "serial_port")
    public int serialPort;

    @Override
    public String toString() {
        if (name != null && !name.isEmpty()) {
            return name;
        }

        if (useSerial) {
            return "Com" + Integer.toString(serialPort);
        }

        if (ipAddress != null && !ipAddress.isEmpty()) {
            return ipAddress + Integer.toString(port);
        }

        return "No string representation";
    }
}
