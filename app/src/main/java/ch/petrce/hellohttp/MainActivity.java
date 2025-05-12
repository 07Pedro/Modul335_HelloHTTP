package ch.petrce.hellohttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        buttonPress();
    }


    private void buttonPress() {
        TextView responseTextView = findViewById(R.id.responseCode); // response code
        TextView responseMessageView = findViewById(R.id.responseMessage); // response message

        View myButton = findViewById(R.id.buttonHttp); // Button
        View progress = findViewById(R.id.progressBar); // Progress circle
        myButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                myButton.setEnabled(false); // Disable button while in HTTP request
                progress.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    try {
                        // Prepare JSON object
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                        String currentDateAndTime = sdf.format(new Date()); // Current timestamp
                        String androidVersion = Build.VERSION.RELEASE; // Android version

                            JSONObject main = new JSONObject(); // JSON object
                            main.put("timestamp", currentDateAndTime); // Add timestamp to JSON
                            main.put("Version", androidVersion); // Add version to JSON

                            // Create HTTP request
                            URL url = new URL("https://httpbin.org/post"); // Target URL
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(); // Create connection
                            urlConnection.setRequestMethod("POST"); // Set method to POST
                            urlConnection.setRequestProperty("Content-Type", "application/json; utf-8"); // Set Content-Type
                            urlConnection.setRequestProperty("Accept", "application/json");
                            urlConnection.setDoOutput(true);

                            // Send JSON object in request body
                            try (OutputStream os = urlConnection.getOutputStream()) {
                                byte[] input = main.toString().getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }

                        int responseCode = urlConnection.getResponseCode(); // Get response code
                        StringBuilder response = new StringBuilder();
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                        }

                        // Handle response on the main thread
                        runOnUiThread(() -> {
                            String responseCodeMessage = "Response Code: " + responseCode;
                            String responseMessage = "Response Message: " + response.toString();
                            responseTextView.setText(responseCodeMessage); // Set response code
                            responseMessageView.setText(responseMessage); // Set response message
                            myButton.setEnabled(true); // Enable button after the request is complete
                            progress.setVisibility(View.INVISIBLE);
                        });

                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                responseTextView.setText("Error: " + e.getMessage());
                                myButton.setEnabled(true); // Enable button after error
                                progress.setVisibility(View.INVISIBLE);
                            });
                        }
                    }).start();
                } else {
                        Toast errorToast = Toast.makeText(MainActivity.this, "Check your internet connection!", Toast.LENGTH_SHORT);
                        errorToast.show();
                }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
