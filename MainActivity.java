package com.example.java_camera_learn;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_1 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView describe = findViewById(R.id.describe);
        Button button = findViewById(R.id.button2);
        Typeface typeface=Typeface.createFromAsset(getAssets(),"fonts/stxingkf.ttf");
        describe.setTypeface(typeface);//设置字体
        button.setTypeface(typeface);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void customCamera(View view){
        //检查各项权限
        if ((checkSelfPermission( Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                || (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ||(checkSelfPermission(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE ,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS},50); }
        startActivity(new Intent(this,CustomCamera.class));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK)   {
            if(requestCode==REQ_1){
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
            }
        }
    }



}