package eu.cyberpunktech.remoterobotcontroller;

import static eu.cyberpunktech.remoterobotcontroller.Variables.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ConfigurationActivity extends AppCompatActivity {
    private EditText editTextSTM32Address;
    private EditText editTextAndroidCamera;
    private Button buttonSave;
    boolean goingToMainActivity = false;

    private String backupSTM32Address = stringSTM32IP;
    private String backupAndroidCameraAddress = stringAndroidIP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        editTextSTM32Address = findViewById(R.id.editTextIP);
        editTextAndroidCamera = findViewById(R.id.editTextServerPort);
        buttonSave = findViewById(R.id.buttonBack);

        editTextSTM32Address.setText(stringSTM32IP);
        editTextAndroidCamera.setText(String.valueOf(stringAndroidIP));
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainActivity();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        goToMainActivity();
    }

    public void saveData() {
        if(editTextSTM32Address.getText().toString().length()<7)
            stringSTM32IP = backupSTM32Address;
        else
            stringSTM32IP = editTextSTM32Address.getText().toString();

        if(editTextAndroidCamera.getText().toString().length()<7)
            stringAndroidIP = backupAndroidCameraAddress;
        else
            stringAndroidIP = editTextAndroidCamera.getText().toString();


        SharedPreferences sharedPreferences = getSharedPreferences(CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(STM32_ADDRESS, stringSTM32IP);
        editor.putString(ANDROID_CAMERA_ADDRESS, stringAndroidIP);

        editor.apply();

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    void goToMainActivity()
    {
        if(!goingToMainActivity)
        {
            goingToMainActivity = true;
            saveData();
            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
