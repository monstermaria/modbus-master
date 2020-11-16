package se.monstermaria.modbusmaster.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProfileDao {
    @Query("SELECT * FROM profile")
    List<Profile> getAll();

    @Query("SELECT * FROM profile WHERE id=:profileId")
    Profile getProfileById(long profileId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Profile profile);

    @Delete
    void delete(Profile profile);
}
