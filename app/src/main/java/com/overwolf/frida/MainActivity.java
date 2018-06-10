package com.overwolf.frida;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private TextView mStatus;
    private Button mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatus = findViewById(R.id.status);
        mToggle = findViewById(R.id.toggle);

        mToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFridaRunning()) {
                    doCmd("su -c killall -9 frida", true);
                } else {
                    doCmd("su -c frida &", true);
                }

                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        invalidateStuffs();
                    }
                }, 250);
            }
        });

        invalidateStuffs();
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateStuffs();
    }

    private void invalidateStuffs() {
        if (isFridaRunning()) {
            mStatus.setText("Frida is running");
            mToggle.setText("Stop frida");
        } else {
            mStatus.setText("Frida is not running");
            mToggle.setText("Start frida");
        }
    }

    private boolean isFridaRunning() {
        String check = doCmd("su -c ps | grep frida", false);
        return check.contains("root");
    }

    private String doCmd(String cmd, boolean ignoreStdout) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            if (ignoreStdout) {
                return "";
            }
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
