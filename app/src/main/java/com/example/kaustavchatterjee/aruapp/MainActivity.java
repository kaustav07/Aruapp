package com.example.kaustavchatterjee.aruapp;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kaustavchatterjee.aruapp.databinding.ActivityMainBinding;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static int REQUEST_PICK_IMAGE = 100;
    public static int REQUEST_PICK_TEXT = 101;
    ActivityMainBinding activityMainBinding;
    Bitmap imageBitmap;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);


        activityMainBinding.encryptType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.radioButton){
                    type = Constatnts.TEXT;
                    activityMainBinding.imageView.setVisibility(View.GONE);
                    activityMainBinding.browsebtn.setVisibility(View.GONE);
                    activityMainBinding.browsetxtbtn.setVisibility(View.VISIBLE);
                    activityMainBinding.textView.setVisibility(View.VISIBLE);

                }else if(i == R.id.radioButton2){
                    type = Constatnts.IMAGE;
                    activityMainBinding.imageView.setVisibility(View.VISIBLE);
                    activityMainBinding.browsebtn.setVisibility(View.VISIBLE);
                    activityMainBinding.browsetxtbtn.setVisibility(View.GONE);
                    activityMainBinding.textView.setVisibility(View.GONE);
                }
            }
        });


        activityMainBinding.browsebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture from gallery"), REQUEST_PICK_IMAGE);
            }
        });

        activityMainBinding.browsetxtbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                startActivityForResult(Intent.createChooser(intent, "Select Text"), REQUEST_PICK_TEXT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null){
            ImageLoader loader = new ImageLoader();
            loader.execute(data.getData());
            activityMainBinding.progressBar.setIndeterminate(true);
            activityMainBinding.progressBar.setVisibility(View.VISIBLE);
        }
        if(resultCode == RESULT_OK && requestCode == REQUEST_PICK_TEXT && data != null && data.getData() != null) {
            byte[] bytesArray;
            try {
                if(getContentResolver().openInputStream(data.getData()) != null) {
                    InputStream fis = getContentResolver().openInputStream(data.getData());
                    bytesArray = convertInputStreamToByteArray(fis);
                    String theString = new String(bytesArray);
                    activityMainBinding.textView.setText(theString);
                }

            }
            catch (Exception e){
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }

        }
    }

    public String inputstream2String (InputStream is) throws IOException {
        final int PKG_SIZE = 1024;
        byte[] data = new byte [PKG_SIZE];
        StringBuilder buffer = new StringBuilder(PKG_SIZE * 10);
        int size;

        size = is.read(data, 0, data.length);
        while (size > 0)
        {
            String str = new String(data, 0, size);
            buffer.append(str);
            size = is.read(data, 0, data.length);
        }
        return buffer.toString();
    }

    public void encrypt(byte[] src){

    }

    public byte[] convertInputStreamToByteArray(InputStream inputStream)
    {
        byte[] bytes= null;

        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte data[] = new byte[1024];
            int count;

            while ((count = inputStream.read(data)) != -1)
            {
                bos.write(data, 0, count);
            }

            bos.flush();
            bos.close();
            inputStream.close();

            bytes = bos.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return bytes;
    }

    public void loadImage(ImageModel model){
        if(model!=null){
            if(model.stream != null) {
                Glide.with(this)
                        .load(model.stream.toByteArray())
                        .asBitmap()
                        .error(R.drawable.ic_launcher_foreground)
                        .into(activityMainBinding.imageView);
            }

            if(model.bitmap != null){
                imageBitmap = model.bitmap;
            }

            activityMainBinding.progressBar.setVisibility(View.GONE);
        }
    }

     class ImageLoader extends AsyncTask<Uri,Integer,ImageModel>{

        @Override
        protected ImageModel doInBackground(Uri... uris) {
            Bitmap imageBitMap = null;
            ImageModel imageModel = null;
            if(uris != null && uris.length > 0){
                Uri uri = uris[0];
                try {
                    imageBitMap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), uri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    imageModel = new ImageModel();
                    imageModel.bitmap = imageBitMap;
                    imageModel.stream = stream;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return imageModel;
        }

        @Override
        protected void onPostExecute(ImageModel model) {
            if(!isCancelled()){
                loadImage(model);
            }
        }
    }
}
