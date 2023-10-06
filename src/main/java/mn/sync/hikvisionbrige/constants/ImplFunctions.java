package mn.sync.hikvisionbrige.constants;

import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mn.sync.hikvisionbrige.holders.CookieHolder;
import mn.sync.hikvisionbrige.holders.ZKCookieHolder;
import mn.sync.hikvisionbrige.models.DigestResponseData;
import okhttp3.*;
import org.json.XML;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.*;
import java.net.Authenticator;
import java.util.Base64;
import java.util.Map;

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
        public DigestResponseData DigestApiService(String API, Object requestBody, String type, String requestMethod) {
            // Set Digest authentication credentials
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FinalVariables.USER_NAME, FinalVariables.PASS_WORD.toCharArray());
                }
            });

            // Create OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder().authenticator(new DigestAuthenticator(new Credentials(FinalVariables.USER_NAME, FinalVariables.PASS_WORD))).retryOnConnectionFailure(true).build();

            // Define the request body (for POST requests)
            MediaType mediaType = MediaType.parse(type);
            RequestBody body = null;
            boolean isSentBody = requestMethod.toUpperCase().equals("POST") || requestMethod.toUpperCase().equals("PUT");
            if (isSentBody) {
                System.out.println("Digest request body: " + requestBody);
                if (requestBody instanceof MultipartBody.Builder) {
                    body = ((MultipartBody.Builder) requestBody).build();
                } else {
                    body = RequestBody.create(String.valueOf(requestBody), mediaType);
                }
            }

            // Create the request
            Request.Builder requestBuilder = new Request.Builder().url(API).header("Connection", "close");

            if (requestMethod.toUpperCase().equals("POST")) {
                requestBuilder.post(body);
            } else if (requestMethod.toUpperCase().equals("PUT")) {
                requestBuilder.put(body);
            } else if (requestMethod.toUpperCase().equals("GET")) {
                requestBuilder.get();
            } else {
                throw new IllegalArgumentException("Invalid request method: " + requestMethod);
            }

            Request request = requestBuilder.build();

            ResponseBody responseBody;
            DigestResponseData digestResponseData = new DigestResponseData();
            digestResponseData.setContentType("Exception error");

            try {
                // Send the request
                Response response = client.newCall(request).execute();

                // Check if the request was successful
                if (response.isSuccessful()) {
                    // Read the response body
                    responseBody = response.body();
                    if (responseBody != null) {
                        MediaType contentType = responseBody.contentType();
                        if (contentType != null) {
                            String mediaTypeString = contentType.toString();
                            System.out.println("mediaTypeString: " + mediaTypeString);
                            digestResponseData.setContentType(mediaTypeString);
                            if (mediaTypeString.startsWith("text/") || mediaTypeString.startsWith("application/json")) {
                                digestResponseData.setBody(responseBody.string());
                            } else if (mediaTypeString.startsWith("application/xml")) {
                                digestResponseData.setBody(XML.toJSONObject(responseBody.string()));
                            } else {
                                digestResponseData.setBody(responseBody.bytes());
                            }
                        }
                    }
                    System.out.println("Digest response: " + digestResponseData.getBody());
                } else {
                    System.out.println("Request failed with status code: " + response.code());
                    digestResponseData.setContentType("Request failed");
                    digestResponseData.setBody(response.code());
                }
            } catch (SocketTimeoutException timeoutEx) {
                // Handle timeout exception
                timeoutEx.printStackTrace();
                digestResponseData.setContentType("Request failed");
                digestResponseData.setBody("Cannot connect to device!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return digestResponseData;
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
                if (auth) {
                    String cookieValue = CookieHolder.getInstance().getCookie("login");
                    connection.setRequestProperty("Authorization", "Bearer " + cookieValue);
                }

                // Create the request body
                if (!method.equals("GET") && !requestBody.isEmpty()) {
                    // Enable output and send the request body
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Length", String.valueOf(requestBody.length()));
                    OutputStream outputStream = connection.getOutputStream();
                    System.out.println("ERP requestBody: " + requestBody);
                    outputStream.write(requestBody.getBytes());
                    outputStream.flush();
                    outputStream.close();
                }

                // Get the response code
                int responseCode = connection.getResponseCode();
                System.out.println("ERP response code: " + responseCode);
                if (responseCode == 200) {
                    // Read the response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Print the response
                    System.out.println("ERP response: " + response);
                    strResponse = response.toString();

                    // Close the connection
                    connection.disconnect();
                } else {
                    strResponse = "Request failed with status code: " + responseCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return strResponse;
        }

        @Override
        public void showAlert(String title, String headerText, String msg, Alert.AlertType alertType) {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(headerText);
            alert.setContentText(msg);
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/favicon.png")));
            alert.show();
        }

        @Override
        public String convertImageToBase64(String imagePath) {
            FileInputStream fis = null;
            File file = new File(imagePath);
            try {
                fis = new FileInputStream(file);
                byte[] fileData = new byte[(int) file.length()];
                fis.read(fileData);
                byte[] fileContent = fileData;
                return Base64.getEncoder().encodeToString(fileContent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        public String convertImageUrlToBase64(String imageUrl) {
            String base64Image = "";
            try {
                // Fetch the image from the URL
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the image data
                    InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    byte[] imageBytes = outputStream.toByteArray();
                    System.out.println("imageBytes: " + imageBytes);

                    // Encode the image bytes to Base64
                    base64Image = Base64.getEncoder().encodeToString(imageBytes);

                    // Close streams
                    inputStream.close();
                    outputStream.close();

                } else {
                    System.out.println("Failed to retrieve image. Response code: " + responseCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return base64Image;
        }

        @Override
        public String ZKTecoApiService(String URL, String method, Map<String, String> formData) {
            Connection connection = Jsoup.connect(URL);
            if (!formData.isEmpty()) {
                connection.data(formData);
            }

            Connection.Method connMethod;
            switch (method) {
                case "POST": {
                    connMethod = Connection.Method.POST;
                    break;
                }
                case "PUT": {
                    connMethod = Connection.Method.PUT;
                    break;
                }
                case "DELETE": {
                    connMethod = Connection.Method.DELETE;
                    break;
                }
                case "HEAD": {
                    connMethod = Connection.Method.HEAD;
                    break;
                }
                case "OPTIONS": {
                    connMethod = Connection.Method.OPTIONS;
                    break;
                }
                case "TRACE": {
                    connMethod = Connection.Method.TRACE;
                    break;
                }
                default: {
                    connMethod = Connection.Method.GET;
                    break;
                }
            }
            Connection.Response resultPageResponse = null;
            try {
                resultPageResponse = connection.header("Cookie", ZKCookieHolder.getInstance().getCookie("cookie"))
                        .method(connMethod)
                        .execute();

                if (resultPageResponse.statusCode() == 200) {
                    return resultPageResponse.body();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultPageResponse.statusMessage() + "/" + resultPageResponse.statusCode() + "/";
        }
    };
}
