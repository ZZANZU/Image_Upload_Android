package com.tistory.dayglo.node_android_test;

import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.R.attr.path;

/*
*
* 1) Initialize the Ion library with Ion.getDefault() method.(The Upload image button is hidden initially)
* 2) When the button is clicked, user selects image from gallery.
* 3) The path of the image selected is stored in a String
* 4) The image is set in ImageView as a preview and the Upload image button is displayed.
* 5) When the Upload button is pressed, the Image Upload process is carried out. (using Ion)
*
* */

public class MainActivity extends AppCompatActivity {
    Button select_image_btn, upload_btn;
    ImageView img;
    String image_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = (ImageView) findViewById(R.id.img);

        Ion.getDefault(this).configure().setLogging("ion-sample", Log.DEBUG);

        select_image_btn = (Button) findViewById(R.id.select_img_btn);

        upload_btn = (Button) findViewById(R.id.upload_img_btn);
        upload_btn.setVisibility(View.INVISIBLE);

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File(image_path);

                Future uploading = Ion.with(MainActivity.this)
                        .load("http://13.59.174.162:7579/upload") // my Server Address
                        .setMultipartFile("image", f)
                        .asString()
                        .withResponse()
                        .setCallback(new FutureCallback<Response<String>>() {
                            @Override
                            public void onCompleted(Exception e, Response<String> result) {
                                try {
                                    JSONObject jobj = new JSONObject(result.getResult());
                                    Toast.makeText(getApplicationContext(), jobj.getString("response"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });
            }
        });

        select_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fIntent.setType("image/jpeg");

                try {
                    startActivityForResult(fIntent, 100);
                } catch(ActivityNotFoundException e) {

                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null) {
            return;
        }

        switch (requestCode) {
            case 100:
                if(resultCode == RESULT_OK) { // 이 부분 디버깅함.
                    image_path = getPathFromURI(data.getData());
                    Log.d("CJ", "image path : " + image_path);

                    img.setImageURI(data.getData());
                    upload_btn.setVisibility(View.VISIBLE);
                }
        }
    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA }; // what
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }
}
