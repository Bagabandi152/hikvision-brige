package mn.sync.hikvisionbrige.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mn.sync.hikvisionbrige.constants.FinalVariables;
import mn.sync.hikvisionbrige.holders.UserHolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 17/07/2023 - 3:23 PM
 * @purpose
 * @definition
 */
public class Device {
    Integer id;
    String ipAddress;
    String name;
    String location;

    public Device(Integer id, String ipAddress, String name, String location) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.name = name;
        this.location = location;
    }

    public Device(){
        this(1,"","","");
    }

    /**
     * get field
     *
     * @return id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * set field
     *
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * get field
     *
     * @return ipAddress
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * set field
     *
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * get field
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * set field
     *
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get field
     *
     * @return location
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * set field
     *
     */
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ObservableList<Device> getDeviceList() {
        ActiveUser activeUser = UserHolder.getInstance().getActiveUser();
        try {
            // Create the URL object
            URL url = new URL(FinalVariables.ERP_URL + "timerpt/device");

            // Create the HttpURLConnection object
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Set request headers
            connection.setRequestProperty("Content-Type", "application/json");
            System.out.println("activeUser.getAccessToken(): " + activeUser.getAccessToken());
            connection.setRequestProperty("Authorization", "Bearer " + activeUser.getAccessToken());

            // Create the request body
            String requestBody = "";

            // Enable output and send the request body
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if(responseCode == 200){
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Print the response
                System.out.println("Response23: " + response);

                // Close the connection
                connection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Device d1 = new Device(1,"172.24.30.13", "DS-K1T343MWX", "UB Office");
        Device d2 = new Device(2,"172.24.30.137", "DS-7604NI-Q1/4P", "Site Office");

        ObservableList<Device> list = FXCollections.observableArrayList(d1,d2);

        return list;
    }
}
