package com.example.java_camera_learn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class CustomCamera extends Activity implements SurfaceHolder.Callback{
    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    public static final String TAG="CustomCamera";				//表示要打印RightFragment碎片的信息
    private static final String[] m={"黄元帅","红富士","糖心","其它"};
    private TextView SpinnerText ;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;

    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    TextView Text_number;
    TextView AppleNumberTextView;
    TextView Reflectance1;
    TextView Reflectance2;
    TextView Reflectance3;
    TextView SSC;
    int Number;
    int AppleNumber=0;//苹果编号
    int AppleMeasurement1=0;
    int AppleMeasurement2=0;
    int AppleMeasurement3=0;
    double ssc;
    double reflectance_460,reflectance_510,reflectance_580;
    double [][] AppleSCC = new double[4][20];//苹果糖度

    @Override
    protected void onCreate(Bundle savedInstanceStates){
        super.onCreate(savedInstanceStates);
        setContentView(R.layout.custom);

        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);
        mPreview = findViewById(R.id.preview);
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);
        Number = 0;
        Text_number = findViewById(R.id.text_number);
        AppleNumberTextView = findViewById(R.id.AppleNumberTextView);
        Reflectance1 = findViewById(R.id.Reflectance1);
        Reflectance2 = findViewById(R.id.Reflectance2);
        Reflectance3 = findViewById(R.id.Reflectance3);
        SSC = findViewById(R.id.SSC);
        Text_number.setText(String.valueOf(Number));

        SpinnerText = (TextView) findViewById(R.id.SpinnerText);
        spinner = (Spinner) findViewById(R.id.spinner1);
        //将可选内容与ArrayAdapter连接起来
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinner.setAdapter(adapter);
        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        //设置默认值
        spinner.setVisibility(View.VISIBLE);

    }

    //使用数组形式操作
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            SpinnerText.setText(""+m[arg2]);
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private Camera.PictureCallback mPictureCallback =new Camera.PictureCallback() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if(!(Text_number.getText().toString().equals("Green") || Text_number.getText().toString().equals("Red")
                    || Text_number.getText().toString().equals("Black"))){
                Number=Integer.parseInt(Text_number.getText().toString());
                Number++;
                Text_number.setText(String.valueOf(Number));
            }

            //用CustomCamera.this替换了网络上别人的context
            File tempFile = new File(CustomCamera.this.getFilesDir().getPath() + "/temp.png");
//            File tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/CustomCamera/temp.png");
            String path = tempFile.getAbsolutePath();
            try {
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(data);
                fos.close();
//                CustomCamera.this.finish();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
            FileInputStream fis ;
            try {
                fis = new FileInputStream(path);

                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                Matrix matrix = new Matrix();
                matrix.setRotate(90);
                bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
//                int w=bitmap.getWidth(); //w: 3472
//                int h=bitmap.getHeight(); //h: 4640
                //剪切Bitmap
//                bitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()*3/20, bitmap.getHeight()*3/20, null,false);
                //显示bitmap
//                Bitmap2Reflectance(bitmap,Number);
                Bitmap2Reflectance2(bitmap,Number);

                //重新启动相机
                setStartPreview(mCamera ,mHolder);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }
    };

    public void OpenLamp(View view){
        try{
            Camera.Parameters mParameters;
            mParameters = mCamera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParameters);
        } catch(Exception ex){
            Toast.makeText(this,"闪光灯打开失败",Toast.LENGTH_SHORT).show();
        }
    }
    public void releaseLamp(View view){
        try{
            Camera.Parameters mParameters;
            mParameters = mCamera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
        } catch(Exception ex){
            Toast.makeText(this,"闪光灯关闭失败",Toast.LENGTH_SHORT).show();
        }
    }

    private void ViewImage(int Number,Bitmap bitmap_tmp){
        if (Number == 1) {
            imageView1.setImageBitmap(bitmap_tmp);
        } else if(Number == 2){
            imageView2.setImageBitmap(bitmap_tmp);
        }else if(Number == 3){
            imageView3.setImageBitmap(bitmap_tmp);
        }else{
            Toast.makeText(this,"图片编号呢？！",Toast.LENGTH_SHORT).show();
        }
    }


    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//允许用户选择特殊种类的数据，并返回（特殊种类的数据：照一张相片或录一段音）
        int PICK_IMAGE_REQUEST = 1;
        startActivityForResult(Intent.createChooser(intent,"选择图像..."), PICK_IMAGE_REQUEST);//启动另外一个活动
    }

    public void EditCode(View view){
        final EditText inputServer = new EditText(CustomCamera.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomCamera.this);
        builder.setTitle("请输入Green或Red或Black或0").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String text = inputServer.getText().toString();
                Text_number.setText(text);
            }
            });
        builder.show();
    }

    public void ReturnZero(View view){
        int id = view.getId();
        if(id == R.id.Zero){
            Number=0;
            Text_number.setText(String.valueOf(Number));
        }else if(id == R.id.Add){
            Number++;
            Text_number.setText(String.valueOf(Number));
        }else if(id == R.id.Subtract){
            Number--;
            Text_number.setText(String.valueOf(Number));
        }
    }

    public void Which_apple(View view){
        int id = view.getId();
        String str="苹果编号："+AppleNumber;
        if(id == R.id.Apple_Add){
            AppleNumber++;
        }else if(id == R.id.Apple_Subtract){
            AppleNumber--;
        }
        str = "苹果编号："+AppleNumber;
        AppleNumberTextView.setText(str);
    }

    public void capture(View view){
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setPreviewSize(600,600);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.autoFocus((success, camera) -> {
            if(success){
                mCamera.takePicture(null,null,mPictureCallback);
            }
        });
        //上一行按照提示修改了
    }

    //自动提取白板并计算反射率
    private void Bitmap2Reflectance2(Bitmap bitmap, int Number){
        Bitmap bitmap_Apple = Bitmap.createBitmap(bitmap, bitmap.getWidth()*340/750, bitmap.getHeight()*542/1000, bitmap.getWidth()*70/750, bitmap.getHeight()*90/1000, null,false);
//        Bitmap bitmap_Apple = Bitmap.createBitmap(bitmap, bitmap.getWidth()*310/750, bitmap.getHeight()*534/1000, bitmap.getWidth()*100/750, bitmap.getHeight()*100/1000, null,false);
//        Bitmap bitmap_WhiteBoard = Bitmap.createBitmap(bitmap, bitmap.getWidth()*220/750, bitmap.getHeight()*450/1000, bitmap.getWidth()*300/750, bitmap.getHeight()*300/1000, null,false);
//        Bitmap bitmap_WhiteBoard = Bitmap.createBitmap(bitmap, bitmap.getWidth()*240/750, bitmap.getHeight()*455/1000, bitmap.getWidth()*300/750, bitmap.getHeight()*300/1000, null,false);
        Bitmap bitmap_WhiteBoard = Bitmap.createBitmap(bitmap, bitmap.getWidth()*240/750, bitmap.getHeight()*440/1000, bitmap.getWidth()*300/750, bitmap.getHeight()*300/1000, null,false);

        try {
            Mat Apple = BitmapGray(bitmap_Apple);//标定图
            Mat Whiteboard = BitmapGray(bitmap_WhiteBoard);//反射图

            ViewImage(Number,bitmap_Apple);
            imageView4.setImageBitmap(bitmap_WhiteBoard);//显示位图
//计算白板照片平均亮度
            Mat tmp= new Mat();
//            Mat Whiteboard1 = new Mat(Whiteboard, new Rect(Whiteboard.cols()/6, 0, 100, 50));
//            Mat Whiteboard2 = new Mat(Whiteboard, new Rect(0, Whiteboard.rows()/6, 50, 100));
//            Mat Whiteboard3 = new Mat(Whiteboard, new Rect(Whiteboard.cols()*5/6, 0, 100, 50));
//            Mat Whiteboard4 = new Mat(Whiteboard, new Rect(0, Whiteboard.rows()*5/6, 50, 100));
            Mat Whiteboard1 = new Mat(Whiteboard, new Rect(Whiteboard.cols()/6, 0, 200, 50));
            Mat Whiteboard2 = new Mat(Whiteboard, new Rect(0, Whiteboard.rows()/6, 50, 200));
            Mat Whiteboard3 = new Mat(Whiteboard, new Rect(Whiteboard.cols()/6, Whiteboard.rows()*5/6, 200, 50));
            Mat Whiteboard4 = new Mat(Whiteboard, new Rect(Whiteboard.cols()*5/6, Whiteboard.rows()/6, 50, 200));
            MatOfDouble mu_wb1 = new MatOfDouble(); MatOfDouble mu_wb2 = new MatOfDouble();
            MatOfDouble mu_wb3 = new MatOfDouble(); MatOfDouble mu_wb4 = new MatOfDouble();
            MatOfDouble sigma_wb = new MatOfDouble();
            Core.meanStdDev(Whiteboard1, mu_wb1, sigma_wb);   Core.meanStdDev(Whiteboard2, mu_wb2, sigma_wb);
            Core.meanStdDev(Whiteboard3, mu_wb3, sigma_wb);   Core.meanStdDev(Whiteboard4, mu_wb4, sigma_wb);
            double Mean_wb1 = mu_wb1.get(0,0)[0]; double Mean_wb2 = mu_wb2.get(0,0)[0];
            double Mean_wb3 = mu_wb3.get(0,0)[0]; double Mean_wb4 = mu_wb4.get(0,0)[0];
            double Mean_wb = (Mean_wb1+Mean_wb2+Mean_wb3+Mean_wb4)/4;
            //计算苹果照片平均亮度
            MatOfDouble mu_apple = new MatOfDouble();
            MatOfDouble sigma_apple = new MatOfDouble();
            Core.meanStdDev(Apple, mu_apple, sigma_apple);
            double Mean_apple = mu_apple.get(0,0)[0];

//            double brightness_apple = (1 - Mean_apple/255)*100;
//            double brightness_wb = (1 - Mean_wb/255)*100;

            String str =String.format(Locale.CHINA,"反射率:%.3f",Mean_apple/Mean_wb);
//            +String.format(Locale.CHINA,"光强:\n%.1f \n%.1f\n",Mean_apple,Mean_wb)+
//                    String.format(Locale.CHINA,"光强差:%.1f",Mean_wb-Mean_apple)
            switch (Number){
                case 1:
                    Reflectance1.setText(str);
                    reflectance_460=Mean_apple/Mean_wb;
                    break;
                case 2:
                    Reflectance2.setText(str);
                    reflectance_510=Mean_apple/Mean_wb;
                    break;
                case 3:
                    Reflectance3.setText(str);
                    reflectance_580=Mean_apple/Mean_wb;
                    SSC_calculate(SSC);
                    break;
                default:
                    Toast.makeText(this, "无法显示反射率", Toast.LENGTH_SHORT).show();
                    break;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SSC_calculate(View view){
        try{
            String str=SpinnerText.getText().toString();
            switch (str) {
                case "黄元帅":
                    ssc = 14.7021 - 3.4673 * reflectance_460 - 0.5152 * reflectance_510 - 1.1032 * reflectance_580;
//                    ssc = 11.0457 - 8.9023 * reflectance_460 + 0.6028 * reflectance_510 + 5.7294 * reflectance_580;
                    if(ssc>11.5){
                        ssc=11.5 ; }
                    if(ssc<10.5){
                        ssc=10.5 ;
                    }
                    break;
                case "红富士":
                    ssc = 12.6277 - 1.2470 * reflectance_460 + 2.5517 * reflectance_510 - 1.9018 * reflectance_580;
                    if(ssc>12.8){
                        ssc=12.8 ; }
                    if(ssc<11.2){
                        ssc=11.2 ;
                    }
                    break;
                case "糖心":
                    ssc = 8.217 + 17.4847 * reflectance_460 - 10.5142 * reflectance_510 - 1.1367 * reflectance_580;
                    if(ssc>14.6){
                        ssc=14.6 ; }
                    if(ssc<12.8){
                        ssc=12.8 ;
                    }
                    break;
                case "其它":
                    ssc = 13.8615 + 5.7346 * reflectance_460 - 0.028 * reflectance_510 - 5.3416 * reflectance_580;
                    if(ssc>14.2){
                        ssc=14.2; }
                    if(ssc<12.1){
                        ssc=12.1;
                    }
                    Toast.makeText(this, "其它苹果默认为红富士！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "未选择苹果种类？！", Toast.LENGTH_SHORT).show();
                    break;
            }
            //double ssc = 11.0457-8.9023*reflectance_460+0.6028*reflectance_510+5.7294*reflectance_580;//黄元帅模型
//                    double ssc = 13.8615+5.7346*reflectance_460-0.028*reflectance_510-5.3416*reflectance_580;
            if(AppleNumber > 0 && AppleNumber < 4){
                if(AppleMeasurement1 == 0 && AppleNumber == 1){
                    AppleSCC[0][0]=ssc;
                }else if(AppleMeasurement2 == 0 && AppleNumber == 2){
                    AppleSCC[1][0]=ssc;
                }else if(AppleMeasurement3 == 0 && AppleNumber == 3){
                    AppleSCC[2][0]=ssc;
                }else if(AppleMeasurement1 > 0.5 || AppleMeasurement2 > 0.5 || AppleMeasurement3 > 0.5){//表示都已经测过一次
                    if(AppleNumber == 1){
                        AppleSCC[AppleNumber-1][AppleMeasurement1] = ssc;//记录糖度值
                        ssc = Summer(AppleSCC[AppleNumber-1])/(AppleMeasurement1 + 1);//自适应取值
                    }else if(AppleNumber == 2){
                        AppleSCC[1][AppleMeasurement2] = ssc;//记录糖度值
                        ssc = Summer(AppleSCC[1])/(AppleMeasurement2 + 1);//自适应取值
                    }else if(AppleNumber == 3){
                        AppleSCC[2][AppleMeasurement3] = ssc;//记录糖度值
                        ssc = Summer(AppleSCC[2])/(AppleMeasurement3 + 1);//自适应取值
                    }
                }
                if(AppleNumber == 1){AppleMeasurement1++;}
                if(AppleNumber == 2){AppleMeasurement2++;}
                if(AppleNumber == 3){AppleMeasurement3++;}
            }
            SSC.setText(String.format(Locale.CHINA, "%.2f", ssc));
            AlertDialog alertDialog1 = new AlertDialog.Builder(this)
                    .setMessage("第" + (AppleNumber>0.5? Summer_number(AppleSCC[AppleNumber-1]):0)+"次糖度测量值：" + SSC.getText().toString())//内容
//                            .setIcon(R.mipmap.ic_launcher)//图标
                    .create();
            alertDialog1.show();
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "哪出错了！", Toast.LENGTH_SHORT).show();
        }
    }

    public double Summer(double[] list){
        double sum=0;
        for (int i = 0; i < list.length; i++) {
            sum=list[i]+sum;
        }
        return sum;
    }
    public int Summer_number(double[] list){
        int num=0;
        for (int i = 0; i < list.length; i++) {
            if(list[i] != 0){num++;}
        }
        return num;
    }
    public void Show_sugar(View view){
        if(AppleNumber > 0.5 && AppleNumber < 4){
            double[] list = AppleSCC[AppleNumber-1];
            AlertDialog alertDialog1 = new AlertDialog.Builder(this)
                .setMessage("历次糖度测量值：" + Arrays.toString(list) + "\n平均值：" + String.format(Locale.CHINA, "%.2f", Summer(list)/Summer_number(list)))//内容
//                            .setIcon(R.mipmap.ic_launcher)//图标
                .create();
        alertDialog1.show();
        }else {
            Toast.makeText(this, "没有记录！", Toast.LENGTH_SHORT).show();
        }
    }

    public static Mat BitmapGray(Bitmap bitmap_new){
        //Bitmap转换灰度Bitmap
        Mat src = new Mat();//原图
        Mat temp = new Mat();//RGB图
        Mat dst = new Mat();//灰度图
        Utils.bitmapToMat(bitmap_new, src);//将位图转换为Mat数据。而对于位图，其由A、R、G、B通道组成
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);//转换为BGR（opencv中数据存储方式）
        Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
        //灰度处理
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);//灰度化处理。
        //进行直方图均衡
//        Mat gray = new Mat();//直方图均衡化后的灰度图
//        Imgproc.equalizeHist(dst,gray);
        //中值滤波，消除孤立噪点
        Mat dst_medianBlur = new Mat();
        Imgproc.medianBlur(dst, dst_medianBlur, 5);
        //均值滤波
        Mat dst_blur = new Mat();
        Imgproc.blur(dst, dst_blur,new Size(5,5),new Point(1, 1),Core.BORDER_DEFAULT);
        //再将mat转换为位图
        Utils.matToBitmap(dst_blur, bitmap_new);
        return dst_blur;
    }
    //Bitmap图像处理
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void Bitmap2Reflectance(Bitmap bitmap, int Number){
        //Bitmap转换灰度Bitmap
        Mat src = new Mat();//原图
        Mat temp = new Mat();//RGB图
        Mat dst = new Mat();//灰度图
        Utils.bitmapToMat(bitmap, src);//将位图转换为Mat数据。而对于位图，其由A、R、G、B通道组成
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);//转换为BGR（opencv中数据存储方式）
        Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
        //灰度处理
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);//灰度化处理。
        //进行直方图均衡
//        Mat gray = new Mat();//直方图均衡化后的灰度图
//        Imgproc.equalizeHist(dst,gray);
        //中值滤波，消除孤立噪点
        Mat dst_medianBlur = new Mat();
        Imgproc.medianBlur(dst, dst_medianBlur, 5);
        //均值滤波
        Mat dst_blur = new Mat();
        Imgproc.blur(dst, dst_blur,new Size(5,5),new Point(1, 1),Core.BORDER_DEFAULT);
        //再将mat转换为位图
        Utils.matToBitmap(dst_blur, bitmap);
        //矩阵剪裁
//        Mat newMat = new Mat(srcMat, rect);
        //矩阵相减和相除
//        Matrix minus = A.minus(B);
//        Matrix division = A.arrayLeftDivide(B);
        //矩阵元素均值or中值？？：http://www.voidcn.com/article/p-roqwjudq-bwh.html
//        MatOfDouble mu = new MatOfDouble();
//        MatOfDouble sigma = new MatOfDouble();
//        Core.meanStdDev(m, mu, sigma);
        if(!(Text_number.getText().toString().equals("Green") || Text_number.getText().toString().equals("Red")
                || Text_number.getText().toString().equals("Black"))){
            String Filepath = null;
            switch (Number){
                case 1:
                    Filepath = CustomCamera.this.getFilesDir().getPath() + "/CustomCamera/WhiteBoard_Green.png";
                    break;
                case 2:
                    Filepath = CustomCamera.this.getFilesDir().getPath() + "/CustomCamera/WhiteBoard_Red.png";
                    break;
                case 3:
                    Filepath = CustomCamera.this.getFilesDir().getPath() + "/CustomCamera/WhiteBoard_Black.png";
                    break;
                default:
                    break;
            }
            try {
                Mat src2 = new Mat();//标定图
                Mat tmp2 = new Mat();//反射图

                FileInputStream fis = new FileInputStream(Filepath);
                Bitmap bitmap_WhiteBoard = BitmapFactory.decodeStream(fis);

                Utils.bitmapToMat(bitmap_WhiteBoard, src2);//将位图转换为Mat数据。而对于位图，其由A、R、G、B通道组成
                Imgproc.cvtColor(src2, temp, Imgproc.COLOR_BGRA2BGR);//转换为BGR（opencv中数据存储方式）
                //灰度处理
                Imgproc.cvtColor(temp, src2, Imgproc.COLOR_BGR2GRAY);//灰度化处理。
                Core.subtract(dst_blur,src2,tmp2);//Mat相减

                //计算平均值作为反射率
                MatOfDouble mu = new MatOfDouble();
                MatOfDouble sigma = new MatOfDouble();
                Core.meanStdDev(tmp2, mu, sigma);
                double Mean = mu.get(0,0)[0];
//                double Mean_=new BigDecimal(Mean).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();;
                String str=String.format(Locale.CHINA,"%.3f",Mean);
                switch (Number){
                    case 1:
                        Reflectance1.setText(str);
                        break;
                    case 2:
                        Reflectance2.setText(str);
                        break;
                    case 3:
                        Reflectance3.setText(str);
                        break;
                    default:
                        Toast.makeText(this, "无法显示反射率", Toast.LENGTH_SHORT).show();
                        break;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Text_number.getText().toString().equals("Green") || Text_number.getText().toString().equals("Red")
                || Text_number.getText().toString().equals("Black")){
            SaveBitmap(bitmap,Text_number.getText().toString());
            imageView4.setImageBitmap(bitmap);//显示位图
        }else{
            Text_number.setText(String.valueOf(Number));
            ViewImage(Number , bitmap);
        }
    }


//    public void Check_WhiteBoard(View view) {
//        String Filepath = null;
//        int id = view.getId();
//        if (id == R.id.Green) {
//            Filepath = CustomCamera.this.getFilesDir().getPath() + "/CustomCamera/WhiteBoard_Green.png";
//        } else if (id == R.id.Red) {
//            Filepath = CustomCamera.this.getFilesDir().getPath() + "/CustomCamera/WhiteBoard_Red.png";
//        } else if (id == R.id.Black) {
//            Filepath = CustomCamera.this.getFilesDir().getPath() + "/CustomCamera/WhiteBoard_Black.png";
//        }
////        String Filepath = CustomCamera.this.getFilesDir().getPath() + "/CustomCamera/WhiteBoard.png";
//        try {
//            FileInputStream fis = new FileInputStream(Filepath);
//            Bitmap bitmap_WhiteBoard  = BitmapFactory.decodeStream(fis);
////            Bitmap bitmap_WhiteBoard = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/WhiteBoard.png"));
//            if(bitmap_WhiteBoard != null){
//                if (id == R.id.Green) {
//                    Toast.makeText(this, "绿色白板照片已存在", Toast.LENGTH_SHORT).show();
//                } else if (id == R.id.Red) {
//                    Toast.makeText(this, "红色白板照片已存在", Toast.LENGTH_SHORT).show();
//                } else if (id == R.id.Black) {
//                    Toast.makeText(this, "黑色白板照片已存在", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(this, "照片已存在但是很奇怪？？", Toast.LENGTH_SHORT).show();
//                }
//                imageView4.setImageBitmap(bitmap_WhiteBoard);//显示位图
//            } else{
//                Toast.makeText(this, "白板照片为空", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (id == R.id.Green) {
//                Toast.makeText(this, "绿色白板照片不存在", Toast.LENGTH_SHORT).show();
//            } else if (id == R.id.Red) {
//                Toast.makeText(this, "红色白板照片不存在", Toast.LENGTH_SHORT).show();
//            } else if (id == R.id.Black) {
//                Toast.makeText(this, "黑色白板照片不存在", Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(this, "出错了", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


    //保存为RGB图像
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void SaveBitmap(Bitmap bitmap, String strFileName){
        if(Objects.equals(strFileName, "Green")){
            strFileName = "WhiteBoard_Green";
        }else if (Objects.equals(strFileName, "Red")){
            strFileName = "WhiteBoard_Red";
        }
        else if (Objects.equals(strFileName, "Black")){
            strFileName = "WhiteBoard_Black";
        }else{
            strFileName = "WhiteBoard";
            Toast.makeText(this, "您没有指定白板照片的对应波长！", Toast.LENGTH_SHORT).show();
        }
        String save_path = CustomCamera.this.getFilesDir().getPath()+"/CustomCamera";
//        String image_filePath = CustomCamera.this.getFilesDir().getPath() +"/res/drawable/WhiteBoard.png";
        File fileFolder = new File(save_path);
        if(!fileFolder.exists()){
            boolean wasSuccessful = fileFolder.mkdirs();
            if (!wasSuccessful) {
                Toast.makeText(this,"文件夹创建失败",Toast.LENGTH_SHORT).show();
            }
        }
        String file_name = strFileName + ".png";
        File png_file = new File(fileFolder,file_name);
        FileOutputStream f_out =null;
        try {
            try {
                f_out = new FileOutputStream(png_file);
            } catch (FileNotFoundException e) {
                Toast.makeText(this,"图片创建失败",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, f_out);//把Bitmap对象解析成流
            Toast.makeText(this,file_name+"保存成功",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (f_out != null) {
                f_out.flush();
                f_out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //识别音量键信息
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown : "+ keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.d(TAG, "KeyEvent.KEYCODE_VOLUME_UP");
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.d(TAG, "KeyEvent.KEYCODE_VOLUME_DOWN");
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    //实现蓝牙拍照功能

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Log.d(TAG, "onKeyUp", new RuntimeException());
        Log.d(TAG, "onKeyUp : "+ keyCode);
        capture(mPreview);
        return super.onKeyUp(keyCode, event);
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(mCamera == null){
            mCamera = getCamera();
            if(mHolder != null){
                setStartPreview(mCamera,mHolder);
            }
        }
        //初始化Opencv
        if (!OpenCVLoader.initDebug()) {
            Log.e("TAG", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.e("TAG", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    // OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    System.out.println("OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    @Override
    protected void onPause(){
        super.onPause();
        releaseCamera();
    }
    //获取Camera对象
    private Camera getCamera(){
        Camera camera;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }
    /*
    开始预览相机内容
     */
    private void setStartPreview(Camera camera,SurfaceHolder holder){
        try {
            camera.setPreviewDisplay(holder);
            // 将系统Camera预览角度进行调整
            camera.setDisplayOrientation(90);
            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    释放系统的Camera
     */
    private void releaseCamera(){
        if(mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        setStartPreview(mCamera, mHolder);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
        mCamera.stopPreview();
        setStartPreview(mCamera,mHolder);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        releaseCamera();
    }



}

