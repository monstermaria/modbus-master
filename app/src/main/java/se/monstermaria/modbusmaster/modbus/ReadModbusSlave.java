package se.monstermaria.modbusmaster.modbus;

import android.os.AsyncTask;

import de.re.easymodbus.modbusclient.ModbusClient;

public class ReadModbusSlave extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
        ModbusClient master = new ModbusClient("192.168.1.69", 502);

        try {
            System.out.println("Connecting...");
            master.Connect();
            System.out.println("Reading...");
            boolean[] coils = master.ReadCoils(0, 10);
            System.out.println("Result:");
            for (int i = 0; i < 10; i++) {
                System.out.println(coils[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
