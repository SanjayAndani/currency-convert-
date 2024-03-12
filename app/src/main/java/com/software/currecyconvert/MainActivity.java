package com.software.currecyconvert;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Spinner fromCurrencySpinner;
    private Spinner toCurrencySpinner;
    private EditText amountEditText;
    private Button convertButton;
    private TextView resultTextView;

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD?access_key=YOUR_API_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromCurrencySpinner = findViewById(R.id.fromCurrencySpinner);
        toCurrencySpinner = findViewById(R.id.toCurrencySpinner);
        amountEditText = findViewById(R.id.amountEditText);
        convertButton = findViewById(R.id.convertButton);
        resultTextView = findViewById(R.id.resultTextView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromCurrencySpinner.setAdapter(adapter);
        toCurrencySpinner.setAdapter(adapter);

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountString = amountEditText.getText().toString();
                String fromCurrency = fromCurrencySpinner.getSelectedItem().toString();
                String toCurrency = toCurrencySpinner.getSelectedItem().toString();

                if (!amountString.isEmpty()) {
                    double amount = Double.parseDouble(amountString);
                    new CurrencyConversionTask().execute(amount, fromCurrency, toCurrency);
                }
            }
        });
    }

    private class CurrencyConversionTask extends AsyncTask<Object, Void, Double> {

        @Override
        protected Double doInBackground(Object... params) {
            double amount = (Double) params[0];
            String fromCurrency = (String) params[1];
            String toCurrency = (String) params[2];

            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse the JSON response to extract the exchange rates
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject rates = jsonObject.getJSONObject("rates");
                    double fromRate = rates.getDouble(fromCurrency);
                    double toRate = rates.getDouble(toCurrency);

                    return (amount / fromRate) * toRate;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Double result) {
            if (result != null) {
                resultTextView.setText(String.format("%.2f", result));
            } else {
                resultTextView.setText("Failed to retrieve exchange rate");
            }
        }
    }
}
