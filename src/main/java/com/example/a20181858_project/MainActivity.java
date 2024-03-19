package com.example.a20181858_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnMyActivity = (Button) findViewById(R.id.button1);
        Button btnSkillActivity = (Button) findViewById(R.id.button2);
        Button btnProjectActivity = (Button) findViewById(R.id.button3);
        Button btnExperienceActivity = (Button) findViewById(R.id.button4);
        Button btnEnd = findViewById(R.id.btnEnd);


        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });

        btnMyActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(getApplicationContext(), MyActivity.class);
                startActivity(intent);
            }
        });

        btnSkillActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(getApplicationContext(), SkillActivity.class);
                startActivity(intent);
            }
        });


        btnProjectActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(getApplicationContext(), ProjectActivity.class);
                startActivity(intent);
            }
        });


        btnExperienceActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(getApplicationContext(), ExperienceActivity.class);
                startActivity(intent);
            }
        });


    }
}