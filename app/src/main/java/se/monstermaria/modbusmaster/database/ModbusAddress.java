package se.monstermaria.modbusmaster.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ModbusAddress {
    @PrimaryKey
    public int address;

    @ColumnInfo(name = "description")
    public String description;
}
