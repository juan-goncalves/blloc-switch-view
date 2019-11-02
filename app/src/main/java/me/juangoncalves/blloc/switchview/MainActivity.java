package me.juangoncalves.blloc.switchview;

import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Switch defaultSwitch = findViewById(R.id.switch1);
        BllocSwitchView bllocSwitch = findViewById(R.id.bllocSwitch);
        bllocSwitch.setChecked(false);
    }
}
