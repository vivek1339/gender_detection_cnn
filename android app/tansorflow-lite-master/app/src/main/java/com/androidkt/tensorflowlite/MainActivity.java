package com.androidkt.tensorflowlite;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
//import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import com.androidkt.tensorflowlite.R;

import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    Interpreter tflite;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    Bitmap photo;
    public static final String TAG = "sample tag";
    private static final String modelFile="model.tflite";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);
        MappedByteBuffer a;
        try{
            a=loadModelFile(this);
        }
        catch (Exception e){
            Log.e(TAG, "onCreate: file not found" );
        }
        try {
            tflite = new Interpreter(loadModelFile(this));
            Toast.makeText(this,"tflite created",Toast.LENGTH_LONG).show();
            Log.e(TAG, "onCreate: model built" );
        }
        catch (IOException e) {
            Log.e(TAG, "onCreate: model not built" );
            e.printStackTrace();
        }
        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
        final Button predict = (Button) this.findViewById(R.id.button);
        predict.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(photo!=null)
                    predict(photo);
                else
                    Toast.makeText(MainActivity.this, "Click an image first", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void predict(Bitmap photo)
    {
        photo = Bitmap.createScaledBitmap(photo,100,100, true);
        int[] inp=new int[100*100];
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(photo);
        photo.getPixels(inp,0,photo.getWidth(),0,0,photo.getWidth(),photo.getHeight());
        Log.e(TAG, "predict: photo is"+inp[9999]);
        float[][] out = new float[1][1];
        tflite.run(byteBuffer,out);
        if(out[0][0]>0.5){
            Toast.makeText(this,"Female",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this,"Male",Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, "predict:prediction is "+Arrays.deepToString(out) );
    }
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        int inputSize=100;
        float IMAGE_MEAN=1;
        float IMAGE_STD=127.5f;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 1 * 100 * inputSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat((((val >> 16) & 0xFF))/IMAGE_STD-1);
                byteBuffer.putFloat((((val >> 8) & 0xFF))/IMAGE_STD-1);
                byteBuffer.putFloat((((val) & 0xFF))/IMAGE_STD-1);
            }
        }
        return byteBuffer;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }
}

