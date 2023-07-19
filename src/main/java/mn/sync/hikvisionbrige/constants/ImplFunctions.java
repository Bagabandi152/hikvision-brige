package mn.sync.hikvisionbrige.constants;

import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import mn.sync.hikvisionbrige.holders.CookieHolder;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Objects;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 5:37 PM
 * @purpose
 * @definition
 */
public class ImplFunctions {
    public static Functions functions = new Functions() {
        @Override
        public String DigestApiService(String API, String requestBody, String type) {
            // Set Digest authentication credentials
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FinalVariables.USER_NAME, FinalVariables.PASS_WORD.toCharArray());
                }
            });

            // Create OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder()
                    .authenticator(new DigestAuthenticator(new Credentials(FinalVariables.USER_NAME, FinalVariables.PASS_WORD)))
                    .build();

            // Define the request body
            MediaType mediaType = MediaType.parse(type);

            // Create the request
            RequestBody body = RequestBody.create(requestBody, mediaType);
            Request request = new Request.Builder()
                    .url(API)
                    .post(body)
                    .build();

            String responseBody = null;
            try {
                // Send the request
                Response response = client.newCall(request).execute();

                // Check if the request was successful
                if (response.isSuccessful()) {
                    // Read the response body
                    responseBody = Objects.requireNonNull(response.body()).string();
                    System.out.println(responseBody);
                } else {
                    System.out.println("Request failed with status code: " + response.code());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return requestBody;
        }

        @Override
        public String ErpApiService(String API, String method, String type, String requestBody, Boolean auth) {
            String strResponse = null;
            try {
                // Create the URL object
                URL url = new URL(FinalVariables.ERP_URL + API);

                // Create the HttpURLConnection object
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the request method to POST
                connection.setRequestMethod(method);

                // Set request headers
                connection.setRequestProperty("Content-Type", type);
                if(auth){
                    String cookieValue = CookieHolder.getInstance().getCookie("login");
                    System.out.println("cookieValue: " + cookieValue);
                    connection.setRequestProperty("Authorization", "Bearer " + cookieValue);
                }

                // Create the request body
                if(!requestBody.isEmpty()){
                    // Enable output and send the request body
                    System.out.println("requestBody: " + requestBody);
                    connection.setDoOutput(true);
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(requestBody.getBytes());
                    outputStream.flush();
                    outputStream.close();
                }

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
                    System.out.println("Response: " + response);
                    strResponse = response.toString();

                    // Close the connection
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return strResponse;
        }
    };
}
