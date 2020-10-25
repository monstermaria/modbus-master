package se.monstermaria.modbusmaster.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProfileDao {
    @Query("SELECT * FROM profile")
    List<DataBase.Profile> getAll();

    @Insert
    void insert(DataBase.Profile profile);
}
