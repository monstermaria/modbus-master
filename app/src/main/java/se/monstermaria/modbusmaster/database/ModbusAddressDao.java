package se.monstermaria.modbusmaster.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ModbusAddressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ModbusAddress> addresses);

    @Query("SELECT * FROM modbusaddress WHERE address IN(:addresses)")
    List<ModbusAddress> getAddresses(int[] addresses);
}
