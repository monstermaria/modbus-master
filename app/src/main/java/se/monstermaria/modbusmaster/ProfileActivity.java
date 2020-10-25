package se.monstermaria.modbusmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.List;

import se.monstermaria.modbusmaster.database.DataBase;
import se.monstermaria.modbusmaster.database.DataBaseTask;

public class ProfileActivity extends AppCompatActivity {
    List<DataBase.Profile> allProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        new DataBaseTask().execute(this, DataBaseTask.DataBaseActions.GET_ALL_PROFILES);
    }
}