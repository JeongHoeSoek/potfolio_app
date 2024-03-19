package com.example.a20181858_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ExperienceActivity extends AppCompatActivity {

    private LinearLayout container;
    private Button btnAdd,btnSave,btnReturn;
    private MyDBHelper myDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experience);

        container = findViewById(R.id.container);
        btnAdd = findViewById(R.id.btnAdd);
        btnSave = findViewById(R.id.btnSave);
        btnReturn = findViewById(R.id.btnReturn);

        myDBHelper = new MyDBHelper(this);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMemo();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMemosToDatabase();
            }
        });

        loadMemosFromDatabase();
    }

    private void addMemo() {
        LinearLayout memoLayout = new LinearLayout(this);
        memoLayout.setOrientation(LinearLayout.VERTICAL);
        container.addView(memoLayout);

        EditText titleEditText = new EditText(this);
        titleEditText.setHint("경험의 주제를 입력하시오.");
        memoLayout.addView(titleEditText);

        EditText contentEditText = new EditText(this);
        contentEditText.setHint("메모의 내용을 입력하시오.");
        memoLayout.addView(contentEditText);

        Button btnDelete = new Button(this);
        btnDelete.setText("메모 삭제");
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMemo(memoLayout);
            }
        });
        memoLayout.addView(btnDelete);
    }

    private void deleteMemo(View view) {
        container.removeView(view);
    }

    private void saveMemosToDatabase() {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();

        db.delete(MyDBHelper.TABLE_NAME, null, null);

        for (int i = 0; i < container.getChildCount(); i++) {
            View memoLayout = container.getChildAt(i);

            if (memoLayout instanceof LinearLayout) {
                EditText titleEditText = ((LinearLayout) memoLayout).getChildAt(0) instanceof EditText ?
                        (EditText) ((LinearLayout) memoLayout).getChildAt(0) : null;
                EditText contentEditText = ((LinearLayout) memoLayout).getChildAt(1) instanceof EditText ?
                        (EditText) ((LinearLayout) memoLayout).getChildAt(1) : null;

                if (titleEditText != null && contentEditText != null) {
                    String title = titleEditText.getText().toString();
                    String content = contentEditText.getText().toString();

                    ContentValues values = new ContentValues();
                    values.put(MyDBHelper.COLUMN_TITLE, title);
                    values.put(MyDBHelper.COLUMN_CONTENT, content);

                    db.insert(MyDBHelper.TABLE_NAME, null, values);
                }
            }
        }

        db.close();
        Toast.makeText(this, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void loadMemosFromDatabase() {
        SQLiteDatabase db = myDBHelper.getReadableDatabase();

        String[] projection = {
                MyDBHelper.COLUMN_TITLE,
                MyDBHelper.COLUMN_CONTENT
        };

        Cursor cursor = db.query(
                MyDBHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_CONTENT));

                addMemoWithTitleAndContent(title, content);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
    }

    private void addMemoWithTitleAndContent(String title, String content) {
        LinearLayout memoLayout = new LinearLayout(this);
        memoLayout.setOrientation(LinearLayout.VERTICAL);
        container.addView(memoLayout);

        EditText titleEditText = new EditText(this);
        titleEditText.setHint("경험의 주제를 입력하시오.");
        titleEditText.setText(title);
        memoLayout.addView(titleEditText);

        EditText contentEditText = new EditText(this);
        contentEditText.setHint("메모의 내용을 입력하시오");
        contentEditText.setText(content);
        memoLayout.addView(contentEditText);

        Button btnDelete = new Button(this);
        btnDelete.setText("메모 삭제");
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMemo(memoLayout);
            }
        });
        memoLayout.addView(btnDelete);
    }

    public class MyDBHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "memo_database";
        private static final int DATABASE_VERSION = 1;

        private static final String TABLE_NAME = "memo_table";
        private static final String COLUMN_TITLE = "title";
        private static final String COLUMN_CONTENT = "content";

        public MyDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_TITLE + " TEXT, "
                    + COLUMN_CONTENT + " TEXT)";
            db.execSQL(createTableQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
