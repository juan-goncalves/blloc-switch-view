package me.juangoncalves.blloc.switchview;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button toggleButton;
    private Button checkButton;
    private Button uncheckButton;
    private BllocSwitchView bllocSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bllocSwitch = findViewById(R.id.bllocSwitch);
        toggleButton = findViewById(R.id.button);
        checkButton = findViewById(R.id.checkButton);
        uncheckButton = findViewById(R.id.uncheckButton);
        initView();
    }

    private void initView() {
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bllocSwitch.toggle();
            }
        });
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bllocSwitch.setChecked(true);
            }
        });
        uncheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bllocSwitch.setChecked(false);
            }
        });
    }

}
