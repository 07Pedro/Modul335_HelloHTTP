package ch.petrce.hellohttp;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

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
        TextView responseTextView = findViewById(R.id.textHttp); // response code
        AtomicReference<Boolean> requestRunning = new AtomicReference<>(false); // boolean access button?

        View myButton = findViewById(R.id.buttonHttp); // Button
        View progress = findViewById(R.id.progressBar); // progress circle
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRunning.set(false);
                myButton.setEnabled(requestRunning.get()); // Disable button while in HTTP request
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
                        String response = "Response Code: " + responseCode; // Set response code

                        // Handle response on the main thread
                        runOnUiThread(() -> {
                            responseTextView.setText(response);
                            requestRunning.set(true);
                            myButton.setEnabled(requestRunning.get()); // Enable button after the request is complete
                            progress.setVisibility(View.INVISIBLE);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            responseTextView.setText("Error: " + e.getMessage());
                            requestRunning.set(true);
                            myButton.setEnabled(requestRunning.get()); // Enable button after error
                            progress.setVisibility(View.INVISIBLE);
                        });
                    }
                }).start();
            }
        });
    }
}