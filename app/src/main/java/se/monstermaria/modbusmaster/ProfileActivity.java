package se.monstermaria.modbusmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

import se.monstermaria.modbusmaster.database.DataBase;
import se.monstermaria.modbusmaster.database.Profile;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    DataBase db;
    List<Profile> profiles;
    Profile activeProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db  = DataBase.getDataBase(this);
        new GetProfilesTask().execute();
    }

    public void showProfiles() {
        Spinner profileSpinner = findViewById(R.id.profileSpinner);
        int selected = 0;

        SharedPreferences preferences = getSharedPreferences("Active profile", MODE_PRIVATE);
        if (preferences != null) {
            long id = preferences.getLong("ID", 0L);
            if (id != 0) {
                for (int i = 0; i < profiles.size(); i++) {
                    if (profiles.get(i).id == id) {
                        selected = i;
                        break;
                    }
                }
            }
        }

        profileSpinner.setAdapter(new ProfileAdapter(profiles));
        profileSpinner.setOnItemSelectedListener(this);
        if (profiles.size() > 0) {
            profileSpinner.setSelection(selected);
        }
    }

    private void saveProfileToPreferences() {
        SharedPreferences.Editor editor = getSharedPreferences("Active profile", MODE_PRIVATE).edit();
        editor.putLong("ID", activeProfile.id);
        editor.apply();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        activeProfile = (Profile) parent.getItemAtPosition(position);
        saveProfileToPreferences();
        System.out.println("Profile selected: " + activeProfile.toString() + " ID: " + activeProfile.id);

        EditText name = findViewById(R.id.editTextProfileName);
        name.setText(activeProfile.name != null ? activeProfile.name : "");

        if (activeProfile.useSerial) {
            findViewById(R.id.ipAddress).setVisibility(View.GONE);
            findViewById(R.id.ipPort).setVisibility(View.GONE);
            findViewById(R.id.serialPort).setVisibility(View.VISIBLE);

            EditText port = findViewById(R.id.editTextSerialPortNumber);
            port.setText(String.valueOf(activeProfile.serialPort));
        } else {
            findViewById(R.id.ipAddress).setVisibility(View.VISIBLE);
            findViewById(R.id.ipPort).setVisibility(View.VISIBLE);
            findViewById(R.id.serialPort).setVisibility(View.GONE);

            EditText ipAddress = findViewById(R.id.editTextIpAddress);
            ipAddress.setText(activeProfile.ipAddress != null ? activeProfile.ipAddress : "");
            EditText port = findViewById(R.id.editTextPortNumber);
            port.setText(String.valueOf(activeProfile.port));
        }

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

        EditText name = findViewById(R.id.editTextProfileName);
        activeProfile.name = name.getText().toString();

        if (activeProfile.useSerial) {
            EditText serialPort = findViewById(R.id.editTextSerialPortNumber);
            int serialPortNumber = 1;
            try {
                serialPortNumber = Integer.parseInt(serialPort.getText().toString());
            } catch (NumberFormatException ignored) {}
            activeProfile.serialPort = serialPortNumber;
        } else {
            EditText ipAddress = findViewById(R.id.editTextIpAddress);
            activeProfile.ipAddress = ipAddress.getText().toString();
            EditText port = findViewById(R.id.editTextPortNumber);
            int portNumber = 502;
            try {
                portNumber = Integer.parseInt(port.getText().toString());
            } catch (NumberFormatException ignored) {}
            activeProfile.port = portNumber;
        }

        new SaveProfileTask().execute(activeProfile);
    }

    public void deleteProfile(View view) {
        if (activeProfile != null) {
            new DeleteProfileTask().execute(activeProfile);
        }
    }

    public void addProfile(View view) {
        new AlertDialog.Builder(this).setTitle(getString(R.string.choose_communication_type))
                .setPositiveButton(getString(R.string.serial), (dialog, which) -> createProfile(true))
                .setNegativeButton(getString(R.string.ethernet), (dialog, which) -> createProfile(false))
                .show();
    }

    void createProfile(boolean serialCommunication) {
        Profile profile = new Profile();

        profile.useSerial = serialCommunication;

        new SaveProfileTask().execute(profile);
    }

    void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    private class SaveProfileTask extends AsyncTask<Profile, Void, Void> {
        @Override
        protected Void doInBackground(Profile... profiles) {
            Profile profileToBeInserted = profiles[0];
            profileToBeInserted.id = db.profileDao().insert(profileToBeInserted);
            activeProfile = profileToBeInserted;
            saveProfileToPreferences();
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
            Log.d("ProfileActivity", "read all profiles");
            return db.profileDao().getAll();
        }

        @Override
        protected void onPostExecute(List<Profile> profiles) {
            Log.d("ProfileActivity", "profiles loaded: " + profiles.size());

            setProfiles(profiles);
            showProfiles();
        }

    }
}
