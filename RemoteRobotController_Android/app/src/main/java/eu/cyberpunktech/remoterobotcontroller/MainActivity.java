package eu.cyberpunktech.remoterobotcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button buttonForward, buttonBackward, buttonRight, buttonLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        buttonForward = findViewById(R.id.buttonForward);
        buttonBackward = findViewById(R.id.buttonBackward);
        buttonRight = findViewById(R.id.buttonRight);
        buttonLeft = findViewById(R.id.buttonLeft);

        
    }
}