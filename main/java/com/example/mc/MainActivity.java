package com.example.mc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private Button hhh;

    private ImageView mImageView;
    private String path, picpath;
    private Uri imageUri,uri;
    private Bitmap bitmap;
    private Button btn;

    //用于在接收返回结果时区分不同的请求。 可以使用任意整数作为请求代码。
    public static final int TAKE_PHOTO = 1;//声明一个请求码，用于识别返回结果
    private static final int SCAN_OPEN_PHONE =2;//相册
    private PopupWindow mPopWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hhh = findViewById(R.id.take_photo);
        askPermission();
        hhh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow();
            }
        });
        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowActivity.class);
                intent.putExtra("path", picpath);
                startActivity(intent);

            }
        });
    }

        public void showPopupWindow() {
            //设置contentView
            View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popupwindow, null);
            PopupWindow mPopWindow = new PopupWindow(contentView,
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
            mPopWindow.setContentView(contentView);
            mPopWindow.setAnimationStyle(R.style.contextMenuAnim);

            //设置各个控件的点击响应
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btn_pop_album = contentView.findViewById(R.id.btn_pop_album);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btn_pop_camera = contentView.findViewById(R.id.btn_pop_camera);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btn_pop_cancel = contentView.findViewById(R.id.btn_pop_cancel);

            btn_pop_album.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGallery();
                }
            });
            btn_pop_camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        openCamera();
                    }
                }
            });
            btn_pop_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            });
            View rootview = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);
            mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
        }

        private void openGallery(){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //intent.setType("image/*");
            startActivityForResult(intent, SCAN_OPEN_PHONE);

        }

        private void openCamera(){
            /**相机打开失败*/
            String imageName = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(new Date());
//        File outputImage=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/data/com.example.woundapplication/"+imageName+".jpg");

            File outputImage = new File(getExternalCacheDir(), imageName+".jpg");

            Objects.requireNonNull(outputImage.getParentFile()).mkdirs();
//        Log.e("", outputImage.getAbsolutePath());
                /*
                创建一个File文件对象，用于存放摄像头拍下的图片，
                把它存放在应用关联缓存目录下，调用getExternalCacheDir()可以得到这个目录，为什么要
                用关联缓存目录呢？由于android6.0开始，读写sd卡列为了危险权限，使用的时候必须要有权限，
                应用关联目录则可以跳过这一步
                 */
            try//判断图片是否存在，存在则删除在创建，不存在则直接创建
            {
                if(outputImage.exists())
                {
                    outputImage.delete();
                }
                boolean a = outputImage.createNewFile();
                Log.e("createNewFile", String.valueOf(a));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if(Build.VERSION.SDK_INT>=24)
                //判断安卓的版本是否高于7.0，高于则调用高于的方法，低于则调用低于的方法
                //把文件转换成Uri对象
                    /*
                    因为android7.0以后直接使用本地真实路径是不安全的，会抛出异常。
                    FileProvider是一种特殊的内容提供器，可以对数据进行保护
                     */
            {
                imageUri= FileProvider.getUriForFile(MainActivity.this,
                        "com.example.MC.file-provider",outputImage);
                //对应Mainfest中的provider
//            imageUri=Uri.fromFile(outputImage);
                path = imageUri.getPath();
                Log.e(">7:",path);
            }
            else {
                imageUri = Uri.fromFile(outputImage);
                path= imageUri.getPath();

                Log.e("<7:",imageUri.getPath());

            }

            //使用隐示的Intent，系统会找到与它对应的活动，即调用摄像头，并把它存储
            Intent intent0=new Intent("android.media.action.IMAGE_CAPTURE");
            intent0.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
            startActivityForResult(intent0,TAKE_PHOTO);
        }
        private void store(String pipath){
            File photoFile = new File(Environment.getExternalStorageDirectory(), "MyPicture.jpg");
            try {
                FileOutputStream fos = new FileOutputStream(photoFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "MyPicture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "This is my picture");
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, photoFile.getAbsolutePath());

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(photoFile)));
            Toast.makeText(this, "读", Toast.LENGTH_SHORT).show();

        }

        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            ImageView img_result=(ImageView) findViewById(R.id.imageView);

            switch (requestCode) {
                case TAKE_PHOTO:
                    if (resultCode == RESULT_OK) {
                        //将图片解析成Bitmap对象，并把它显现出来
                        String filePath = getFilesDir().getAbsolutePath()+"/image.jpeg";
                        bitmap = BitmapFactory.decodeFile(filePath);
                        //注意bitmap，后面再decode就会为空
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
//                        bitmap = BitmapFactory.decodeFile(picpath);
                        picpath = imageUri.getPath().toString();
                        Log.e("", imageUri.getAuthority());

                        Log.e("picpath",picpath);
                        @SuppressLint("SdCardPath") String fileName = picpath;
                        store(picpath);
                        img_result.setImageBitmap(bitmap);
                        img_result.invalidate();
                    }break;
                case SCAN_OPEN_PHONE:
                    if (resultCode == RESULT_OK){

                        Uri selectImage=data.getData();
                        String[] FilePathColumn={MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectImage,
                                FilePathColumn, null, null, null);
                        cursor.moveToFirst();
                        //从数据视图中获取已选择图片的路径
                        int columnIndex = cursor.getColumnIndex(FilePathColumn[0]);
                        picpath = cursor.getString(columnIndex);
                        Log.e("picpath",picpath);
                        cursor.close();
                        bitmap = BitmapFactory.decodeFile(picpath);
                        img_result.setImageBitmap(bitmap);
                        img_result.invalidate();

                    }
                    break;
                default:
                    break;
            }
        }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 0);
    }
}
