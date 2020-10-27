package se.monstermaria.modbusmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

import se.monstermaria.modbusmaster.database.DataBase;
import se.monstermaria.modbusmaster.database.Profile;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    DataBase db;
    Profile activeProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db  = DataBase.getDataBase(this);
        new GetProfilesTask().execute();
    }

    public void showProfiles(List<Profile> profiles) {
        System.out.println(profiles.size());

        Spinner profileSpinner = findViewById(R.id.profileSpinner);
        profileSpinner.setAdapter(new ProfileAdapter(profiles));
        profileSpinner.setOnItemSelectedListener(this);
    }

    private void saveProfileToPreferences() {
        SharedPreferences.Editor editor = getSharedPreferences("Active profile", MODE_PRIVATE).edit();
        editor.putString("ID", String.valueOf(activeProfile.id));
        editor.apply();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        activeProfile = (Profile) parent.getItemAtPosition(position);
        saveProfileToPreferences();
        System.out.println("Profile selected: " + activeProfile.toString() + " ID: " + activeProfile.id);
        EditText name = findViewById(R.id.editTextProfileName);
        name.setText(activeProfile.name != null ? activeProfile.name : "");
        EditText ipAddress = findViewById(R.id.editTextIpAddress);
        ipAddress.setText(activeProfile.ipAddress != null ? activeProfile.ipAddress : "");
        EditText port = findViewById(R.id.editTextPortNumber);
        port.setText(String.valueOf(activeProfile.port));
        findViewById(R.id.profileTable).setVisibility(View.VISIBLE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        findViewById(R.id.profileTable).setVisibility(View.GONE);
        activeProfile = null;
    }

    public void saveProfile(View view) {
        if (activeProfile == null) {
            return;
        }
//        Spinner spinner = findViewById(R.id.profiles_spinner);
//        Profile profile = (Profile) spinner.getSelectedItem();
        EditText name = findViewById(R.id.editTextProfileName);
        activeProfile.name = name.getText().toString();
        EditText ipAddress = findViewById(R.id.editTextIpAddress);
        activeProfile.ipAddress = ipAddress.getText().toString();
        EditText port = findViewById(R.id.editTextPortNumber);
        int portNumber = 502;
        try {
            portNumber = Integer.parseInt(port.getText().toString());
        } catch (NumberFormatException ignored) {}
        activeProfile.port = portNumber;

        new SaveProfileTask().execute(activeProfile);
    }

    public void deleteProfile(View view) {
        if (activeProfile != null) {
            new DeleteProfileTask().execute(activeProfile);
        }
    }

    private class SaveProfileTask extends AsyncTask<Profile, Void, Void> {
        @Override
        protected Void doInBackground(Profile... profiles) {
            db.profileDao().insert(profiles[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            new GetProfilesTask().execute();
        }

    }

    private class DeleteProfileTask extends AsyncTask<Profile, Void, Void> {
        @Override
        protected Void doInBackground(Profile... profiles) {
            db.profileDao().delete(profiles[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            new GetProfilesTask().execute();
        }

    }

    private class GetProfilesTask extends AsyncTask<Void, Void, List<Profile>> {
        @Override
        protected List<Profile> doInBackground(Void... nothings) {
            return db.profileDao().getAll();
        }

        @Override
        protected void onPostExecute(List<Profile> profiles) {
            showProfiles(profiles);
        }
    }
}