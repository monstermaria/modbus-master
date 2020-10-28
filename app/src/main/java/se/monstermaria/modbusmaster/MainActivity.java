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

    ModbusRegisters getActiveReadings() {
        Spinner spinner = (Spinner) findViewById(R.id.readingSpinner);
        String readings = (String) spinner.getSelectedItem();
        ModbusRegisters activeReadings = null;

        if (readings.equals(getString(R.string.coils))) {
            activeReadings = ModbusRegisters.COILS;
        } else if (readings.equals(getString(R.string.discrete_inputs))) {
            activeReadings = ModbusRegisters.DISCRETE_INPUTS;
        } else if (readings.equals(getString(R.string.input_registers))) {
            activeReadings = ModbusRegisters.INPUT_REGISTERS;
        } else if (readings.equals(getString(R.string.holding_registers))) {
            activeReadings = ModbusRegisters.HOLDING_REGISTERS;
        }
        
        return activeReadings;
    }

    void setUpTable(ModbusRegisters registerType, int firstAddress, int numberOfAddresses) {
        String message = registerType.toString() + ", first address: ";
        message += firstAddress + ", number of addresses: ";
        message += String.valueOf(numberOfAddresses);
        Log.d("Create table", message);

        TableLayout registerTable = findViewById(R.id.readingsTable);
        resetTable(registerTable);

        for (int i = firstAddress; i < firstAddress + numberOfAddresses; i++) {
            TableRow row = makeTableRow(registerTable, registerType, i);
            registerTable.addView(row);
        }
    }

    void updateTableWithBitResults(ModbusRegisters registerType, List<Boolean> results,
                                   int firstAddress) {
        TableLayout registerTable = findViewById(R.id.readingsTable);
        int addressOffset = 0;

        if (registerType == ModbusRegisters.COILS) {
            addressOffset = firstAddress + 1;
        }
        if (registerType == ModbusRegisters.DISCRETE_INPUTS) {
            addressOffset = firstAddress + 10001;
        }

        for (int i = 0; i < results.size(); i++) {
            TableRow row = registerTable.findViewById(i + addressOffset);
            TextView value = row.findViewById(R.id.valueTextView);
            value.setText(String.valueOf(results.get(i)));
        }
    }

    void updateTableWithIntResults(ModbusRegisters registerType, List<Integer> results,
                                   int firstAddress) {
        TableLayout registerTable = findViewById(R.id.readingsTable);
        int addressOffset = 0;

        if (registerType == ModbusRegisters.INPUT_REGISTERS) {
            addressOffset = firstAddress + 30001;
        }
        if (registerType == ModbusRegisters.HOLDING_REGISTERS) {
            addressOffset = firstAddress + 40001;
        }

        for (int i = 0; i < results.size(); i++) {
            TableRow row = registerTable.findViewById(i + addressOffset);
            TextView value = row.findViewById(R.id.valueTextView);
            value.setText(String.valueOf(results.get(i)));
        }
    }

    public void initiateReadings(View view) {
        boolean readyToRead = modbusMaster != null && modbusMaster.isConnected();
        ModbusRegisters registerType = getActiveReadings();
        int firstAddress = 0;
        int numberOfAddresses = 10;

        setUpTable(registerType, firstAddress, numberOfAddresses);

        if (activeProfile == null) {
            getProfile();
        }

        if (!readyToRead) return;

        if (registerType == ModbusRegisters.COILS) {
            new ReadBitRegistersTask().execute(0, firstAddress, numberOfAddresses);
        } else if (registerType == ModbusRegisters.DISCRETE_INPUTS) {
            new ReadBitRegistersTask().execute(1, firstAddress, numberOfAddresses);
        } else if (registerType == ModbusRegisters.INPUT_REGISTERS) {
            new ReadIntRegistersTask().execute(3, firstAddress, numberOfAddresses);
        } else if (registerType == ModbusRegisters.HOLDING_REGISTERS) {
            new ReadIntRegistersTask().execute(4, firstAddress, numberOfAddresses);
        }
    }

    TableRow makeTableRow(TableLayout table, ModbusRegisters registerType, int address) {
        TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.address_table_row,
                table, false);
        TextView addressView = (TextView) row.findViewById(R.id.addressTextView);
        String addressString = "#";

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

        addressString += address;
        addressView.setText(addressString);
        row.setId(address);

        return row;
    }

    void resetTable(TableLayout table) {
        View header = findViewById(R.id.header);
        table.removeAllViews();
        table.addView(header);
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

    private class ReadBitRegistersTask extends AsyncTask<Integer, Void, List<Boolean>> {
        ModbusRegisters registerType;
        int firstAddress;

        @Override
        public List<Boolean> doInBackground(Integer... params) {
            List<Boolean> resultList = new ArrayList<>();
            boolean[] result = {};

            firstAddress = params[1];

            try {
                if (params[0] == 0) {
                    registerType = ModbusRegisters.COILS;
                    result = modbusMaster.ReadCoils(params[1], params[2]);
                }
                if (params[0] == 1) {
                    registerType = ModbusRegisters.DISCRETE_INPUTS;
                    result = modbusMaster.ReadDiscreteInputs(params[1], params[2]);
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
            updateTableWithBitResults(registerType, resultList, firstAddress);
        }
    }

    private class ReadIntRegistersTask extends AsyncTask<Integer, Void, List<Integer>> {
        ModbusRegisters registerType;
        int firstAddress;

        @Override
        public List<Integer> doInBackground(Integer... params) {
            List<Integer> resultList = new ArrayList<>();
            int[] result = {};

            firstAddress = params[1];

            try {
                if (params[0] == 3) {
                    registerType = ModbusRegisters.INPUT_REGISTERS;
                    result = modbusMaster.ReadInputRegisters(params[1], params[2]);
                }
                if (params[0] == 4) {
                    registerType = ModbusRegisters.HOLDING_REGISTERS;
                    result = modbusMaster.ReadHoldingRegisters(params[1], params[2]);
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
            updateTableWithIntResults(registerType, resultList, firstAddress);
        }
    }
}
