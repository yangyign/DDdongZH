package com.example.androidzonghe1.activity.yyWork;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.androidzonghe1.ConfigUtil;
import com.example.androidzonghe1.R;
import com.example.androidzonghe1.activity.Track.TrackApplication;
import com.example.androidzonghe1.activity.lpyWork.MyTheActivity;
import com.example.androidzonghe1.adapter.lpyWork.RecycleAdapterDriver;
import com.example.androidzonghe1.adapter.lpyWork.RvAdapterNoTitleDriver;
import com.example.androidzonghe1.entity.lpyWork.Messages;
import com.example.androidzonghe1.entity.yyWork.DriverOrder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity implements View.OnClickListener {
//    private Button btnDriver;
    private ImageView driverImg;
    public   static TextView driverName;
    private TextView chooseState;
    private Button chooseDriver;
    private Button driverInfo;
    private DriverOrder order = new DriverOrder();
    private ImageView inF;
    private ImageView outS;
    private TextView from;
    private TextView to;
    private TextView inF0;
    private TextView outS0;
    private LinearLayout in;
    private LinearLayout out;
    private TextView tvGo;
    private TextView tvBack;
    private TextView tvWeek;
    private TextView tvHope;
    private TextView tvSpend;
    private double gl = 0;
    private TextView tvPrice;
    private Button add;
    private DateFormat format;
    private Calendar calendar;
    private String time;
    private String week;
    private double distance = 0;
    private String pwd;
    private RecyclerView recyclerView;
    private RvAdapterNoTitleDriver adapter;
    private ImageView ivChooseDrivedr;
    private ImageView getIvChooseDrivedrLine;
    private ImageView ivOrderSuccess;
    private TextView tvChooseD;
    private int myPosition;
    private String str;
//    private List<Driver> drivers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);
        getViews();
////        //????????????
//        for (int i = 0;i<7;i++){
//            Driver driver = new Driver();
//            driver.setName("ere");
//            driver.setAge(23);
//            driver.setCar("lawnfi");
//            driver.setPhone("12341322");
//            drivers.add(driver);
//        }

        chooseDriver.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //?????????????????????????????????????????????????????????
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(OrderDetailsActivity.this);
                View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.driver_list_item,null);
                Button btnCancel = v.findViewById(R.id.btn_cancel);
                Button btnConfirm = v.findViewById(R.id.btn_confirm);
//                ListView listView = v.findViewById(R.id.lv_driver);
                recyclerView = v.findViewById(R.id.rv_driver);
                if(ConfigUtil.drivers.size() == 0){
                    ConfigUtil.initDrivers();
                }

                Log.e("",ConfigUtil.drivers.toString());
                adapter = new RvAdapterNoTitleDriver(ConfigUtil.drivers);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(adapter);
                myPosition = adapter.getMyPosition();
//                listView.setAdapter(driverAdapter);
                //????????????
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        EventBus.getDefault().post(listView.);
                        driverName.setText("???????????????");
                        chooseState.setText("?????????");
                        bottomSheetDialog.dismiss();
                    }
                });
                recyclerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                //????????????
                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        driverInfo.setVisibility(View.VISIBLE);
                        RequestOptions options = new RequestOptions()
                                .placeholder(R.drawable.loading);
                        Glide.with(getApplicationContext())
//                    .load(drivers.get(position-1).getImg())
                                .load(ConfigUtil.xt+ConfigUtil.drivers.get(myPosition).getImg())
                                .apply(options)
                                .into(driverImg);
//                        driverImg.setImageResource(ConfigUtil.drivers.get(myPosition).getImg());
                        chooseDriver.setText("????????????");
                        ivChooseDrivedr.setImageResource(R.drawable.spot1);
                        getIvChooseDrivedrLine.setImageResource(R.drawable.hline2);
                        tvChooseD.setTextColor(getResources().getColor(R.color.red));
                        driverName.setText(ConfigUtil.drivers.get(myPosition).getName());
                        order.setDriver(ConfigUtil.drivers.get(myPosition).getPhone());
                        chooseState.setText("?????????");
                        ConfigUtil.driverPhone = ConfigUtil.drivers.get(myPosition).getPhone();
//                        TrackApplication.entityName = ConfigUtil.driverPhone;
                        Log.e("orderDertailsActivity",ConfigUtil.driverPhone);
                        Log.e("entityName", TrackApplication.entityName);
                        bottomSheetDialog.dismiss();

                    }
                });

                bottomSheetDialog.setContentView(v);
                bottomSheetDialog.setCancelable(true);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                bottomSheetDialog.show();
            }
        });

        if (ConfigUtil.flagOrder){
            //?????????FragmentLaunchRoute??????????????????????????????  ???????????????from???to?????????
            Bundle bundle =  getIntent().getExtras().getBundle("lrInfo");
            from.setText(bundle.getString("stName"));
            to.setText(bundle.getString("enName"));
            //?????????FragmentLaunchRoute?????????????????????: ????????????????????????????????????tvSpend???
//        distance = CaculateDistance.GetDistance(38.002119,114.520159,37.984026,114.528652);
            distance = bundle.getDouble("distance");
            gl = Math.round( distance / 100d) / 10d;
            ConfigUtil.flagOrder = false;
        }
        tvSpend.setText(gl+"");
        //????????????????????????15?????????tvPrice???
        tvPrice.setText(gl*15+"");
        tvHope.setOnClickListener(this);
        tvGo.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        in.setOnClickListener(this);
        out.setOnClickListener(this);
        add.setOnClickListener(this);
//        btnDriver.setOnClickListener(this);
        //2
        order.setFrom(from.getText()+"");
        //3
        order.setTo(to.getText()+"");
        //4????????????????????????
//        order.setDriver(tvDriver.getText()+"");
        order.setPrice(Double.parseDouble(tvPrice.getText()+""));

    }

    private void getViews() {
        from = findViewById(R.id.tv_from);
        to = findViewById(R.id.tv_to);
        inF = findViewById(R.id.iv_in_f);
        outS = findViewById(R.id.iv_in_s);
        inF0 = findViewById(R.id.tv_in_f);
        outS0=findViewById(R.id.tv_in_s);
        in = findViewById(R.id.lin_in_sch);
        out = findViewById(R.id.lin_out_sch);
        tvGo = findViewById(R.id.tv_to_sch);
        tvBack = findViewById(R.id.tv_left_sch);
        tvWeek = findViewById(R.id.tv_week);
        tvHope = findViewById(R.id.tv_hope);
        format = DateFormat.getDateTimeInstance();
        calendar = Calendar.getInstance(Locale.CHINA);
        add = findViewById(R.id.btn_add_order);
//        btnDriver = findViewById(R.id.btn_find_driver);
        driverImg = findViewById(R.id.iv_order_driver_img);
        driverName = findViewById(R.id.tv_order_driver_name);
        chooseDriver = findViewById(R.id.btn_find_driver);
        chooseState = findViewById(R.id.tv_order_choose_state);
//        btnDriver
        driverInfo = findViewById(R.id.btn_order_driver_info);
        tvSpend = findViewById(R.id.tv_spend);
        tvPrice = findViewById(R.id.tv_price);
        ivChooseDrivedr = findViewById(R.id.iv_choose_driver);
        getIvChooseDrivedrLine = findViewById(R.id.iv_choose_driver_line);
        tvChooseD = findViewById(R.id.tv_driver_choose);
        ivOrderSuccess = findViewById(R.id.iv_run);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_hope://????????????
                showDatePickerDialog(this, 4, tvHope,tvWeek, calendar);
                //????????????
                break;
            case R.id.tv_to_sch:
                showTimePickerDialog(this,2,tvGo,calendar);
                break;
            case R.id.tv_left_sch:
                showTimePickerDialog(this,2,tvBack,calendar);
                break;
            case R.id.lin_in_sch:
                //??????
                order.setAddress("??????");
                outS.setImageResource(R.drawable.yes1);
                outS0.setTextColor(getResources().getColor(R.color.gray));
                inF.setImageResource(R.drawable.yes);
                inF0.setTextColor(getResources().getColor(R.color.myColor));
                //5
                break;
            case R.id.lin_out_sch:
                //??????
                order.setAddress("??????");
                inF.setImageResource(R.drawable.yes1);
                inF0.setTextColor(getResources().getColor(R.color.gray));
                outS.setImageResource(R.drawable.yes);
                outS0.setTextColor(getResources().getColor(R.color.myColor));
                break;
            case R.id.btn_add_order:
                //?????????????????????????????????????????????????????????????????????
                //?????????????????????????????????????????????????????????????????????????????????????????????TestActivity??????????????????????????? pwd
                FragmentManager manager = getSupportFragmentManager();
                final PayPasswordDialog dialog=new PayPasswordDialog(OrderDetailsActivity.this,R.style.mydialog,pwd,manager,this,3);
                dialog.setDialogClick(new PayPasswordDialog.DialogClick() {
                    @Override
                    public void doConfirm(String password) {
                        dialog.dismiss();
                        if(ConfigUtil.isLogin){
                            if(ConfigUtil.pwd.equals(password)){
                                showCustomeDialog();
                                ivOrderSuccess.setImageResource(R.drawable.spot1);
                                //???????????????????????????
                                commitOrder();
                                //??????????????????
                                addMessage(ConfigUtil.xt+"AddMessageSerlvet");
                                Log.e("??????",order.toString());
                                Log.e("??????????????????",password);
                            }else {
                                showErrorDialog();
                            }
                        }
                    }
                });
                dialog.show();
                break;
            case R.id.btn_find_driver:
                //?????????????????????????????????????????????????????????
                ConfigUtil.flagChooseDriver = true;
                Intent intent = new Intent(OrderDetailsActivity.this, MyTheActivity.class);
                startActivityForResult(intent,1);
                break;
            case R.id.btn_order_driver_info:
                //??????????????????
                break;
        }
    }

    private void showErrorDialog() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        ErrorDialog errorDialog = new ErrorDialog();
        if(!errorDialog.isAdded()){
            transaction.add(errorDialog,"dialog_tag");
        }
        transaction.show(errorDialog);
        transaction.commit();
    }

    private void commitOrder() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(ConfigUtil.xt+"AddOrderServlet");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    OutputStream os = connection.getOutputStream();
                    os.write(toStr(order).getBytes());
                    InputStream is = connection.getInputStream();
                    byte[] b = new byte[512];
                    int len = 0;
                    if((len = is.read(b))!=-1){
                        str = new String(b,0,len,"UTF-8");
                    }
                    Log.e("??????",str);
                    if(str.equals("true")){
                        //????????????????????????FragmentLaunchRoute
                        //????????????????????????
                        reduceBalance();
                    }
                    os.close();
                    is.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //????????????
    public void addMessage(String s){
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL(s);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //??????http???????????????get???post???put???...(??????get??????)
                    connection.setRequestMethod("POST");//??????????????????
                    Messages messages = new Messages();
                    messages.setTitle("????????????????????????");
                    messages.setType("????????????");
                    messages.setUserId(ConfigUtil.parent.getId());
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(System.currentTimeMillis()));
                    messages.setDate(date);

                    Gson gson = new Gson();
                    String jsonStr = gson.toJson(messages);
                    //?????????????????????
                    OutputStream os = connection.getOutputStream();
                    os.write(jsonStr.getBytes());

                    //???????????????????????????
                    InputStream is = connection.getInputStream();
                    byte[] bytes = new byte[256];
                    int len = is.read(bytes);//??????????????????bytes?????????????????????len???
                    Log.e("???????????????",new String(bytes,0,len));

                    os.close();
                    is.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    private String toBalance(){
        JSONObject object = new JSONObject();
        try {
            object.put("id",ConfigUtil.parent.getId());
            object.put("price",20+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    private void reduceBalance() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(ConfigUtil.xt+"OrderChangeMoneyServlet");
                    //??????id?????????????????????
                    //??????id ??? ????????????
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    OutputStream os = connection.getOutputStream();
                    os.write(toBalance().getBytes());
                    InputStream is = connection.getInputStream();
                    int len = 0;
                    byte[] b = new byte[512];
                    if((len=is.read(b))!=-1){
                        str = new String(b,0,len,"UTF-8");
                        Log.e("??????????????????",str);
                    }
                    if(str.equals("true")){
                        //????????????activity
                        finish();
                    }
                    is.close();
                    os.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void showCustomeDialog() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        PaySucc dialog = new PaySucc();
        if(!dialog.isAdded()){
            transaction.add(dialog,"dialog_tag");
        }
        transaction.show(dialog);
        transaction.commit();
        //??????OrderDetailsActivity
    }

    //??????????????????json?????????
    public String toStr(DriverOrder order){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from",order.getFrom());
            jsonObject.put("to",order.getTo());
            jsonObject.put("driver",order.getDriver());
            jsonObject.put("time",order.getTime());
            jsonObject.put("address",order.getAddress());
            jsonObject.put("date",order.getDate());
            jsonObject.put("price",order.getPrice());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * ????????????
     *
     * @param activity
     * @param themeResId
     * @param tv
     * @param calendar
     */
    public void showDatePickerDialog(Activity activity, int themeResId, final TextView tv,final TextView week0,Calendar calendar) {
        // ??????????????????DatePickerDialog???????????????????????????????????????
        new DatePickerDialog(activity, themeResId, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                tv.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                order.setDate(year + "-" + (month + 1) + "-" + dayOfMonth);
                Log.e("??????",order.getDate());
                week = "???";
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                try {
                    c.setTime(format.parse(year+"-"+(month+1)+"-"+dayOfMonth));
                    week = getWeek(week,c);
                    week0.setText(week);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private static String getWeek(String week,Calendar c) {
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            week += "???";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 2) {
            week += "???";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 3) {
            week += "???";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 4) {
            week += "???";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 5) {
            week += "???";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 6) {
            week += "???";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 7) {
            week += "???";
        }
        return week;
    }

    /**
     * ????????????
     *
     * @param activity
     * @param themeResId
     * @param tv
     * @param calendar
     */
    public void showTimePickerDialog(Activity activity, int themeResId, final TextView tv, Calendar calendar) {
        new TimePickerDialog(activity, themeResId, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if(minute==0){
                    tv.setText(hourOfDay+":"+minute+"0");
                }else if(minute>0&&minute<10){
                    tv.setText(hourOfDay+":0"+minute);
                }else{
                    tv.setText(hourOfDay+":"+minute);
                }
                //6
                order.setTime(tv.getText()+"");
                Log.e("??????",order.getTime());
            }
        }, calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}