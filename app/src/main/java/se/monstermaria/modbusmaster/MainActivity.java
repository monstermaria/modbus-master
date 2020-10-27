package se.monstermaria.modbusmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import se.monstermaria.modbusmaster.database.DataBase;
import se.monstermaria.modbusmaster.database.Profile;
import se.monstermaria.modbusmaster.modbus.ReadModbusSlave;

public class MainActivity extends AppCompatActivity {
    DataBase db;
    Profile activeProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DataBase.getDataBase(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getSharedPreferences("Active profile", MODE_PRIVATE);
        if (preferences != null) {
            long id = preferences.getLong("ID", 0L);
            if (id != 0) {
                new GetProfileTask().execute(id);
            }
        }
    }

    public void chooseProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void getReadings(View view) {
        new ReadModbusSlave().execute("null");
    }

    private class GetProfileTask extends AsyncTask<Long, Void, Profile> {
        @Override
        public Profile doInBackground(Long... ids) {
            return db.profileDao().getProfileById(ids[0]);
        }

        @Override
        public void onPostExecute(Profile profile) {
            activeProfile = profile;
            Log.d("Profile loaded", activeProfile != null ? activeProfile.toString() : "No profile loaded");
        }
    }
}