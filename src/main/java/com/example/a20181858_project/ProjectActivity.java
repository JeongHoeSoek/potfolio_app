package com.example.a20181858_project;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class ProjectActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREF_LAST_POSITION = "last_position";

    private int position = -1;
    Button btnAddImage, btnReturn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        setTitle("Project");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        position = preferences.getInt(PREF_LAST_POSITION, -1);

        final GridView gv = findViewById(R.id.gridView1);
        MyGridAdapter gAdapter = new MyGridAdapter(this);
        gv.setAdapter(gAdapter);

        btnAddImage = findViewById(R.id.btnAddImage);
        btnReturn = findViewById(R.id.btnReturn);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAddImage.setOnClickListener(v -> addImageFromGallery());
    }

    private void addImageFromGallery() {
        MyGridAdapter adapter = (MyGridAdapter) ((GridView) findViewById(R.id.gridView1)).getAdapter();

        pickImageFromGallery();

        position++;
        if (position >= adapter.getCount()) {
            position = 0;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_LAST_POSITION, position);
        editor.apply();
    }    private void resetImages() {
        MyGridAdapter adapter = (MyGridAdapter) ((GridView) findViewById(R.id.gridView1)).getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            adapter.setImage(i, null);
        }
        position = -1;
    }

    private void pickImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());

                MyGridAdapter adapter = (MyGridAdapter) ((GridView) findViewById(R.id.gridView1)).getAdapter();
                if (selectedImage != null) {
                    adapter.setImage(position, selectedImage);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyGridAdapter extends BaseAdapter {
        Context context;
        private MyDBHelper dbHelper;

        public MyGridAdapter(Context c) {
            context = c;
            dbHelper = new MyDBHelper(context);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            position = preferences.getInt(PREF_LAST_POSITION, -1);

            loadProjectData();
        }

        private void loadProjectData() {
            Arrays.fill(projectID, null);
            Arrays.fill(projectDescription, null);

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] columns = {MyDBHelper.COLUMN_IMAGE, MyDBHelper.COLUMN_DESCRIPTION};
            Cursor cursor = db.query(MyDBHelper.TABLE_NAME, columns, null, null, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int imageIndex = cursor.getColumnIndex(MyDBHelper.COLUMN_IMAGE);
                    int descriptionIndex = cursor.getColumnIndex(MyDBHelper.COLUMN_DESCRIPTION);

                    if (imageIndex != -1 && descriptionIndex != -1) {
                        byte[] imageBytes = cursor.getBlob(imageIndex);
                        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        String description = cursor.getString(descriptionIndex);

                        int currentPosition = cursor.getPosition();
                        if (currentPosition < projectID.length) {
                            projectID[currentPosition] = image;
                            projectDescription[currentPosition] = description;
                        }
                    }
                }
                cursor.close();
            }
            db.close();
        }

        private void saveProjectData(int position, Bitmap image, String description) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + MyDBHelper.TABLE_NAME + " WHERE " + BaseColumns._ID + " = ?",
                    new String[]{String.valueOf(position)});

            ContentValues values = new ContentValues();
            values.put(MyDBHelper.COLUMN_IMAGE, getBytes(image));
            values.put(MyDBHelper.COLUMN_DESCRIPTION, description);

            if (cursor.getCount() > 0) {
                db.update(MyDBHelper.TABLE_NAME, values, BaseColumns._ID + " = ?", new String[]{String.valueOf(position)});
            } else {
                values.put(BaseColumns._ID, position);
                db.insert(MyDBHelper.TABLE_NAME, null, values);
            }

            cursor.close();
            db.close();
        }
        private byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        Bitmap[] projectID = new Bitmap[16];
        String[] projectDescription = new String[16];

        public int getCount() {
            return projectID.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public void setImage(int position, Bitmap bitmap) {
            projectID[position] = bitmap;
            saveProjectData(position, bitmap, projectDescription[position]);
            notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageview = new ImageView(context);
            imageview.setLayoutParams(new GridView.LayoutParams(200, 300));
            imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageview.setPadding(5, 5, 5, 5);

            if (projectID[position] != null) {
                imageview.setImageBitmap(projectID[position]);
            } else {
                imageview.setImageResource(R.drawable.default_project);
            }

            final int pos = position;
            imageview.setOnClickListener(v -> showImageDialog(pos));

            return imageview;
        }

        private void showImageDialog(int position) {
            View dialogView = View.inflate(ProjectActivity.this, R.layout.dialog, null);
            AlertDialog.Builder dlg = new AlertDialog.Builder(ProjectActivity.this);
            ImageView ivProject = dialogView.findViewById(R.id.ivProject);
            EditText etDescription = dialogView.findViewById(R.id.etDescription);

            if (projectID[position] != null) {
                ivProject.setImageBitmap(projectID[position]);
                etDescription.setText(projectDescription[position]);
            } else {
                ivProject.setImageResource(R.drawable.default_project);
            }

            dlg.setTitle("프로젝트 정보");
            dlg.setIcon(R.drawable.ic_launcher);
            dlg.setView(dialogView);

            dlg.setPositiveButton("저장", (dialog, which) -> {
                String description = etDescription.getText().toString();
                projectDescription[position] = description;

                saveProjectData(position, projectID[position], description);
            });

            dlg.setNegativeButton("닫기", null);

            dlg.setOnDismissListener(dialog -> notifyDataSetChanged());

            dlg.show();
        }
    }



    public class MyDBHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "my_database.db";
        private static final int DATABASE_VERSION = 1;

        public static final String TABLE_NAME = "project_table";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_DESCRIPTION = "description";

        private static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY," +
                        COLUMN_IMAGE + " BLOB," +
                        COLUMN_DESCRIPTION + " TEXT)";

        private static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public MyDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_TABLE);
            onCreate(db);
        }
    }

}

