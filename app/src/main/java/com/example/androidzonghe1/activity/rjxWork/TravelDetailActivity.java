package com.example.androidzonghe1.activity.rjxWork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.androidzonghe1.ConfigUtil;
import com.example.androidzonghe1.R;
import com.example.androidzonghe1.activity.yjWork.ViedoActivity;
import com.example.androidzonghe1.entity.lpyWork.Driver;
import com.example.androidzonghe1.entity.yyWork.DriverOrder;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TravelDetailActivity extends AppCompatActivity {
    private ImageView driverImg;
    public  TextView driverName;
    private TextView chooseState;
    private Button callDriver;
    private Button driverVideo;
    private ImageView over_line;
    private ImageView iv_over;
    private TextView tv_over;
    private TextView tvMile;
    private TextView tvPrice;
    private TextView tvWeek;
    private TextView from;
    private TextView to;
    private ImageView inF;
    private ImageView outS;
    private TextView inF0;
    private TextView outS0;
    private TextView tvGo;
    private TextView tvBack;
    private Button over;
    private TextView tvHope;
    private Bundle bundle;
    private Driver driver;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 10:
                    String str = (String) msg.obj;
                    Gson gson = new Gson();
                    driver = gson.fromJson(str,Driver.class);
                    Glide.with(getBaseContext())
                            .load(ConfigUtil.xt+driver.getImg())
                            .into(driverImg);
                    driverName.setText(driver.getName());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_detail);
        findViews();

        //???????????????
        Intent intent = getIntent();
        bundle = intent.getBundleExtra("bundle");
        setViews();
        //??????????????????
        getDriverInfo(ConfigUtil.xt+"ShowDriverServlet?id="+bundle.get("id"));

        setListener();
    }

    private void findViews(){
        driverImg = findViewById(R.id.iv_order_driver_img);
        driverName = findViewById(R.id.tv_order_driver_name);
        callDriver = findViewById(R.id.btn_call_driver);
        driverVideo = findViewById(R.id.btn_look_driver);
        from = findViewById(R.id.tv_from);
        to = findViewById(R.id.tv_to);
        over_line = findViewById(R.id.iv_over_driver_line);
        iv_over = findViewById(R.id.iv_over);
        tv_over = findViewById(R.id.tv_over);
        tvMile = findViewById(R.id.tv_mile);
        tvPrice = findViewById(R.id.tv_price);
        tvWeek = findViewById(R.id.tv_week);
        inF = findViewById(R.id.iv_in_f);
        outS = findViewById(R.id.iv_in_s);
        inF0 = findViewById(R.id.tv_in_f);
        outS0=findViewById(R.id.tv_in_s);
        tvGo = findViewById(R.id.tv_to_sch);
        tvBack = findViewById(R.id.tv_left_sch);
        tvHope = findViewById(R.id.tv_hope);
        over = findViewById(R.id.btn_over_order);
    }

    private void setViews(){
        from.setText(bundle.get("from").toString());
        to.setText(bundle.get("to").toString());
        tvGo.setText(bundle.get("time").toString());
//        tvBack.setText(bundle.get("endTime").toString());
        tvHope.setText(bundle.get("date").toString());
        if (bundle.get("state").equals("?????????")){
            over_line.setImageResource(R.drawable.hline2);
            iv_over.setImageResource(R.drawable.spot1);
            tv_over.setTextColor(Color.RED);
            over.setText("???????????????");
            over.setBackground(null);
        }
        if (bundle.get("goOrCome").equals("??????")){
            inF.setImageResource(R.drawable.yes);
            inF0.setTextColor(getResources().getColor(R.color.myColor));
        }else {
            outS.setImageResource(R.drawable.yes);
            outS0.setTextColor(getResources().getColor(R.color.myColor));
        }


    }

    //???????????????
    private void setListener(){
        //????????????
        callDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+driver.getPhone()));
                startActivity(callIntent);
            }
        });
        //????????????
        driverVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViedoActivity.class);
                startActivity(intent);
            }
        });
//        //????????????
//        if (!bundle.get("state").equals("?????????")){
//            over.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
//        }

    }

    //
    private void getDriverInfo(String s){
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL(s);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //??????http???????????????get???post???put???...(??????get??????)
                    connection.setRequestMethod("POST");//??????????????????

                    //???????????????????????????
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String resp = reader.readLine();
//                    byte[] bytes = new byte[256];
//                    int len = is.read(bytes);//??????????????????bytes?????????????????????len???
//                    String resp = new String(bytes,0,len);

                    //??????Message????????????
                    Message message = new Message();
                    //??????Message???????????????
                    message.what = 10;
                    message.obj = resp;
                    //??????Message
                    handler.sendMessage(message);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

}