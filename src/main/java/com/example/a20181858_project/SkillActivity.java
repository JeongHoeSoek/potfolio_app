package com.example.a20181858_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SkillActivity extends AppCompatActivity {

    private EditText editTextExpert, editTextBusiness, editTextUnderstanding;
    private Button btnSave, btnReturn;

    private MyDBHelper myDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill);

        myDBHelper = new MyDBHelper(this);

        editTextExpert = findViewById(R.id.editTextExpert);
        editTextBusiness = findViewById(R.id.editTextBusiness);
        editTextUnderstanding = findViewById(R.id.editTextUnderstanding);
        btnSave = findViewById(R.id.btnSave);
        btnReturn = findViewById(R.id.btnReturn);


        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSkillsToDatabase();
            }
        });

        loadDataFromDatabase();
    }

    private void saveSkillsToDatabase() {
        String expertSkills = editTextExpert.getText().toString();
        String businessSkills = editTextBusiness.getText().toString();
        String understandingSkills = editTextUnderstanding.getText().toString();

        SQLiteDatabase db = null;
        try {
            db = myDBHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(MyDBHelper.COLUMN_EXPERT, expertSkills);
            values.put(MyDBHelper.COLUMN_BUSINESS, businessSkills);
            values.put(MyDBHelper.COLUMN_UNDERSTANDING, understandingSkills);

            db.insert(MyDBHelper.TABLE_NAME, null, values);

            Toast.makeText(this, "데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    private void loadDataFromDatabase() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = myDBHelper.getReadableDatabase();

            String[] projection = {
                    MyDBHelper.COLUMN_EXPERT,
                    MyDBHelper.COLUMN_BUSINESS,
                    MyDBHelper.COLUMN_UNDERSTANDING
            };

            cursor = db.query(
                    MyDBHelper.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    "1 DESC" // Sort in descending order and limit to 1 row
            );

            // Clear previous entries
            editTextExpert.setText("");
            editTextBusiness.setText("");
            editTextUnderstanding.setText("");

            // Process all rows
            while (cursor != null && cursor.moveToNext()) {
                String expertSkills = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_EXPERT));
                String businessSkills = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_BUSINESS));
                String understandingSkills = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_UNDERSTANDING));

                // Set the EditText fields with the latest skill set
                editTextExpert.setText(expertSkills);
                editTextBusiness.setText(businessSkills);
                editTextUnderstanding.setText(understandingSkills);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    public class MyDBHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "skills_database";
        private static final int DATABASE_VERSION = 2;  // 데이터베이스 버전을 올립니다.

        private static final String TABLE_NAME = "skills_table";
        private static final String COLUMN_EXPERT = "expert";
        private static final String COLUMN_BUSINESS = "business";
        private static final String COLUMN_UNDERSTANDING = "understanding";

        public MyDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_EXPERT + " TEXT, "
                    + COLUMN_BUSINESS + " TEXT, "
                    + COLUMN_UNDERSTANDING + " TEXT)";
            db.execSQL(createTableQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}