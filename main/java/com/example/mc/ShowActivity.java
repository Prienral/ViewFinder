package com.example.mc;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ShowActivity extends AppCompatActivity {
    private ImageView imageView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        String imagePath = getIntent().getStringExtra("path");
        imageView = findViewById(R.id.imageView2);
        Uri uri = Uri.fromFile(new File(imagePath));
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "读取图片失败：打开输入流失败", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "读取图片失败：图片文件不存在", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
}