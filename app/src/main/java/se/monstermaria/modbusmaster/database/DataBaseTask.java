package se.monstermaria.modbusmaster.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import java.util.List;

public class DataBaseTask extends AsyncTask {
    public enum DataBaseActions {
        GET_ALL_PROFILES,
        INSERT_PROFILE,
        DELETE_PROFILE
    }

    private static boolean initialized = false;
    private static DataBase db;
    private static ProfileDao profileDao;

    private static void initialize(Context context) {
        if (!initialized) {
            db = Room.databaseBuilder(context, DataBase.class, "modbus_database").build();
            profileDao = db.profileDao();
            initialized = true;
        }
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        // make sure that the database object has been created
        if (!initialized) {
            DataBaseTask.initialize((Context) objects[0]);
        }

        // find out what action to perform
        switch ((DataBaseActions) objects[1]) {
            case GET_ALL_PROFILES:
                List<DataBase.Profile> allProfiles = profileDao.getAll();
                System.out.println("Number of profiles: " + allProfiles.size());
                return allProfiles;
            case INSERT_PROFILE:
                profileDao.insert((DataBase.Profile) objects[2]);
                break;
            case DELETE_PROFILE:
                break;
            default:
                return null;
        }
        return null;
    }
}
