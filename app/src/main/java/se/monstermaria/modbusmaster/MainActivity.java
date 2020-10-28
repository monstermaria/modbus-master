package se.monstermaria.modbusmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

import se.monstermaria.modbusmaster.database.DataBase;
import se.monstermaria.modbusmaster.database.Profile;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    DataBase db;
    Profile activeProfile;
    ModbusClient modbusMaster;
    ModbusRegisters activeReadings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DataBase.getDataBase(this);

        Spinner readingSpinner = (Spinner) findViewById(R.id.readingSpinner);
        readingSpinner.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.address_types, android.R.layout.simple_spinner_item));
        readingSpinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getProfile();
    }

    void getProfile() {
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        initiateReadings(view);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void initiateReadings(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.readingSpinner);
        String readings = (String) spinner.getSelectedItem();
        boolean readyToRead = modbusMaster != null && modbusMaster.isConnected();

        Log.d("Chosen readings", readings);


        if (activeProfile == null) {
            getProfile();
        }

        if (!readyToRead) return;

        if (readings.equals(getString(R.string.coils))) {
            activeReadings = ModbusRegisters.COILS;
            new ReadBitRegistersTask().execute();
        } else if (readings.equals(getString(R.string.discrete_inputs))) {
            activeReadings = ModbusRegisters.DISCRETE_INPUTS;
            new ReadBitRegistersTask().execute();
        } else if (readings.equals(getString(R.string.input_registers))) {
            activeReadings = ModbusRegisters.INPUT_REGISTERS;
            new ReadIntRegistersTask().execute();
        } else if (readings.equals(getString(R.string.holding_registers))) {
            activeReadings = ModbusRegisters.HOLDING_REGISTERS;
            new ReadIntRegistersTask().execute();
        } else {
            activeReadings = null;
        }
    }

    TableRow makeTableRow(TableLayout table, ModbusRegisters registerType, int address, String value, String description) {
        TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.address_table_row,
                table, false);
        TextView addressView = (TextView) row.findViewById(R.id.addressTextView);
        TextView valueView = (TextView) row.findViewById(R.id.valueTextView);
        EditText descriptionView = (EditText) row.findViewById(R.id.descriptionTextView);

        if (registerType == ModbusRegisters.COILS) {
            address += 1;
        } else if (registerType == ModbusRegisters.DISCRETE_INPUTS) {
            address += 10001;
        } else if (registerType == ModbusRegisters.INPUT_REGISTERS) {
            address += 30001;
        } else if (registerType == ModbusRegisters.HOLDING_REGISTERS) {
            address += 40001;
        } else {
            Log.e("Modbus", "Unknown Modbus register registerType");
        }

        addressView.setText("#" + String.valueOf(address));
        valueView.setText(value);
        descriptionView.setText(description);

        return row;
    }

    void resetTable(TableLayout table) {
        View header = findViewById(R.id.header);
        table.removeAllViews();
        table.addView(header);
    }

    void populateBitReadingsTable(List<Boolean> results, ModbusRegisters registerType) {
        System.out.println(registerType + ": " + results);

        TableLayout readingsTable = findViewById(R.id.readingsTable);
        resetTable(readingsTable);

        for (int i = 0; i < results.size(); i++) {
            TableRow row = makeTableRow(readingsTable, registerType, i, String.valueOf(results.get(i)), "");
            readingsTable.addView(row);
        }
    }

    void populateIntReadingsTable(List<Integer> results, ModbusRegisters registerType) {
        System.out.println(results);

        TableLayout readingsTable = findViewById(R.id.readingsTable);
        resetTable(readingsTable);

        for (int i = 0; i < results.size(); i++) {
            TableRow row = makeTableRow(readingsTable, registerType, i, String.valueOf(results.get(i)), "");
            readingsTable.addView(row);
        }
    }

    enum ModbusRegisters {
        COILS,
        DISCRETE_INPUTS,
        INPUT_REGISTERS,
        HOLDING_REGISTERS
    }

    private class GetProfileTask extends AsyncTask<Long, Void, Profile> {
        @Override
        public Profile doInBackground(Long... ids) {
            return db.profileDao().getProfileById(ids[0]);
        }

        @Override
        public void onPostExecute(Profile profile) {
            if (profile == null) {
                Log.d("Profile loaded", "Failed to load profile");
            } else {
                Log.d("Profile loaded", profile.toString());
                activeProfile = profile;
                modbusMaster = new ModbusClient(profile.ipAddress, profile.port);
                new ConnectToServerTask().execute();
            }
        }
    }

    private class ConnectToServerTask extends AsyncTask<Void, Void, Void> {
        @Override
        public Void doInBackground(Void... nothings) {
            try {
                modbusMaster.Connect();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(Void nothing) {
            initiateReadings(null);
        }
    }

    private class ReadBitRegistersTask extends AsyncTask<Void, Void, List<Boolean>> {
        ModbusRegisters registerType;

        @Override
        public List<Boolean> doInBackground(Void... nothings) {
            List<Boolean> resultList = new ArrayList<>();
            boolean[] result = {};

            try {
                if (activeReadings == ModbusRegisters.COILS) {
                    registerType = ModbusRegisters.COILS;
                    result = modbusMaster.ReadCoils(0, 10);
                }
                if (activeReadings == ModbusRegisters.DISCRETE_INPUTS) {
                    registerType = ModbusRegisters.DISCRETE_INPUTS;
                    result = modbusMaster.ReadDiscreteInputs(0, 10);
                }
            } catch (IOException | ModbusException e) {
                e.printStackTrace();
            }

            // convert array of primitive values to a list, because return value must be an object
            for (boolean bit : result) {
                resultList.add(bit);
            }

            return resultList;
        }

        @Override
        public void onPostExecute(List<Boolean> resultList) {
            populateBitReadingsTable(resultList, registerType);
        }
    }

    private class ReadIntRegistersTask extends AsyncTask<Void, Void, List<Integer>> {
        ModbusRegisters registerType;

        @Override
        public List<Integer> doInBackground(Void... nothings) {
            List<Integer> resultList = new ArrayList<>();
            int[] result = {};

            try {
                if (activeReadings == ModbusRegisters.INPUT_REGISTERS) {
                    registerType = ModbusRegisters.INPUT_REGISTERS;
                    result = modbusMaster.ReadInputRegisters(0, 10);
                }
                if (activeReadings == ModbusRegisters.HOLDING_REGISTERS) {
                    registerType = ModbusRegisters.HOLDING_REGISTERS;
                    result = modbusMaster.ReadHoldingRegisters(0, 10);
                }
            } catch (IOException | ModbusException e) {
                e.printStackTrace();
            }

            // convert array of primitive values to a list, because return value must be an object
            for (int value : result) {
                resultList.add(value);
            }

            return resultList;
        }

        @Override
        public void onPostExecute(List<Integer> resultList) {
            populateIntReadingsTable(resultList, registerType);
        }
    }
}
