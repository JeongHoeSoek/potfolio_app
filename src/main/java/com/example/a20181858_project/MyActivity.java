package com.example.a20181858_project;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MyActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int IMAGE_MAX_SIZE = 512;

    private ImageView imageView;
    private EditText editTextIntro;
    private EditText editTextPhone;
    private EditText editTextEmail;
    private Button btnSave;
    private Button btnReturn;

    public class myDBHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "MyDatabase.db";

        public static final String TABLE_NAME = "mytable";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_INTRO = "intro";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_IMAGE = "image_data";

        private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_INTRO + " TEXT, " +
                        COLUMN_PHONE + " TEXT, " +
                        COLUMN_EMAIL + " TEXT, " +
                        COLUMN_IMAGE + " BLOB);";

        public myDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
    private myDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        dbHelper = new myDBHelper(this);

        imageView = findViewById(R.id.imageView);
        editTextIntro = findViewById(R.id.editTextIntro);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        btnSave = findViewById(R.id.btnSave);
        btnReturn = findViewById(R.id.btnReturn);

        loadData();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });



        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                Bitmap scaledImage = scaleBitmap(selectedImage, IMAGE_MAX_SIZE);
                imageView.setImageBitmap(scaledImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void saveData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(myDBHelper.COLUMN_INTRO, editTextIntro.getText().toString());
        values.put(myDBHelper.COLUMN_PHONE, editTextPhone.getText().toString());
        values.put(myDBHelper.COLUMN_EMAIL, editTextEmail.getText().toString());

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        byte[] imageBytes = getBytes(bitmap);
        values.put(myDBHelper.COLUMN_IMAGE, imageBytes);

        long newRowId = db.insert(myDBHelper.TABLE_NAME, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }

        dbHelper.close();
    }

    private void loadData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                myDBHelper.COLUMN_INTRO,
                myDBHelper.COLUMN_PHONE,
                myDBHelper.COLUMN_EMAIL,
                myDBHelper.COLUMN_IMAGE
        };

        Cursor cursor = db.query(
                myDBHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );


        if (cursor != null && cursor.moveToFirst()) {
            do {
                String intro = cursor.getString(cursor.getColumnIndexOrThrow(myDBHelper.COLUMN_INTRO));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(myDBHelper.COLUMN_PHONE));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(myDBHelper.COLUMN_EMAIL));
                byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(myDBHelper.COLUMN_IMAGE));

                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(decodedImage);

                editTextIntro.setText(intro);
                editTextPhone.setText(phone);
                editTextEmail.setText(email);


            } while (cursor.moveToNext());

            cursor.close();
        }

        dbHelper.close();
    }
    private byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}







