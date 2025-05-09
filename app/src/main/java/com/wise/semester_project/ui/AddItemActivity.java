package com.wise.semester_project.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wise.semester_project.R;
import com.wise.semester_project.model.InventoryItem;
import com.wise.semester_project.repository.InventoryRepository;

public class AddItemActivity extends AppCompatActivity {
    private EditText nameInput;
    private EditText rfidInput;
    private EditText quantityInput;
    private EditText temperatureInput;
    private EditText humidityInput;
    private EditText locationInput;
    private Button saveButton;
    private InventoryRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        repository = new InventoryRepository(getApplication());

        nameInput = findViewById(R.id.nameInput);
        rfidInput = findViewById(R.id.rfidInput);
        quantityInput = findViewById(R.id.quantityInput);
        temperatureInput = findViewById(R.id.temperatureInput);
        humidityInput = findViewById(R.id.humidityInput);
        locationInput = findViewById(R.id.locationInput);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        try {
            String name = nameInput.getText().toString();
            String rfid = rfidInput.getText().toString();
            int quantity = Integer.parseInt(quantityInput.getText().toString());
            double temperature = Double.parseDouble(temperatureInput.getText().toString());
            double humidity = Double.parseDouble(humidityInput.getText().toString());
            String location = locationInput.getText().toString();

            if (name.isEmpty() || rfid.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            InventoryItem item = new InventoryItem(name, rfid, quantity, temperature, humidity, location);
            repository.insert(item);
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for quantity, temperature and humidity", Toast.LENGTH_SHORT).show();
        }
    }
} 