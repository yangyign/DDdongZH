package com.example.androidzonghe1.activity.lpyWork;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.fence.FenceAlarmPushInfo;
import com.baidu.trace.api.fence.MonitoredAction;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.LocationMode;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.example.androidzonghe1.ConfigUtil;
import com.example.androidzonghe1.R;
import com.example.androidzonghe1.activity.Track.BitmapUtil;
import com.example.androidzonghe1.activity.Track.CommonUtil;
import com.example.androidzonghe1.activity.Track.Constants;
import com.example.androidzonghe1.activity.Track.CurrentLocation;
import com.example.androidzonghe1.activity.Track.MapUtil;
import com.example.androidzonghe1.activity.Track.TracingOptionsActivity;
import com.example.androidzonghe1.activity.Track.TrackApplication;
import com.example.androidzonghe1.activity.Track.TrackReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.os.PowerManager.*;
import static com.example.androidzonghe1.activity.Track.BitmapUtil.bmStart;

/**
 * ????????????
 */
public class TracingActivity extends BaseActivity implements View.OnClickListener {

    private TrackApplication trackApp = null;

    private ViewUtil viewUtil = null;

    private Button traceBtn = null;

    private Button gatherBtn = null;

    private NotificationManager notificationManager = null;

    private PowerManager powerManager = null;

    private PowerManager.WakeLock wakeLock = null;

    private TrackReceiver trackReceiver = null;

    /**
     * ????????????
     */
    private MapUtil mapUtil = null;

    private BitmapUtil bitmapUtil = null;

    /**
     * ?????????????????????
     */
    private OnTraceListener traceListener = null;

    /**
     * ???????????????(???????????????????????????????????????)
     */
    private OnTrackListener trackListener = null;

    /**
     * Entity?????????(??????????????????????????????)
     */
    private OnEntityListener entityListener = null;

    /**
     * ??????????????????
     */
    private RealTimeHandler realTimeHandler = new RealTimeHandler();

    private RealTimeLocRunnable realTimeLocRunnable = null;

    private boolean isRealTimeRunning = true;

    private int notifyId = 0;

    /**
     * ????????????
     */
    public int packInterval = Constants.DEFAULT_PACK_INTERVAL;

    //??????????????????
    private List<LatLng> pointList = new ArrayList<>();
    private MapStatusUpdate msUpdate = null;
    private OverlayOptions polyline=null;
    private OverlayOptions overlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.tracing_title);
        setOnClickListener(this);
        init();
    }

    private void init() {
        TrackApplication.entityName = ConfigUtil.phone;
        TrackApplication.initTrace();
        bitmapUtil = new BitmapUtil();
        BitmapUtil.init();
        initListener();
        trackApp = (TrackApplication) getApplicationContext();
        viewUtil = new ViewUtil();
        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) findViewById(R.id.tracing_mapView));
        mapUtil.setCenter(trackApp);
        //?????????????????????????????????runnable???????????????runnable???????????????handler???????????????runnable?????????????????????TrackApplication???getCurrentLocation()??????????????????
        startRealTimeLoc(Constants.LOC_INTERVAL);
        powerManager = (PowerManager) trackApp.getSystemService(Context.POWER_SERVICE);

        traceBtn = (Button) findViewById(R.id.btn_trace);
        gatherBtn = (Button) findViewById(R.id.btn_gather);

        traceBtn.setOnClickListener(this);
        gatherBtn.setOnClickListener(this);
        setTraceBtnStyle();
        setGatherBtnStyle();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // ??????????????????
            case R.id.btn_activity_options:
                ViewUtil.startActivityForResult(this, TracingOptionsActivity.class, Constants
                        .REQUEST_CODE);
                break;

            case R.id.btn_trace:
                if (trackApp.isTraceStarted) {
                    trackApp.mClient.stopTrace(trackApp.mTrace, traceListener);
                    stopRealTimeLoc();
//                    pointList.clear();
                } else {
                    pointList.clear();
                    trackApp.mClient.startTrace(trackApp.mTrace, traceListener);
                    if (Constants.DEFAULT_PACK_INTERVAL != packInterval) {
                        stopRealTimeLoc();
                        startRealTimeLoc(packInterval);
                    }
                }
                break;

            case R.id.btn_gather:
                if (trackApp.isGatherStarted) {
                    trackApp.mClient.stopGather(traceListener);
                } else {
                    trackApp.mClient.startGather(traceListener);
                }
                break;

            default:
                break;
        }

    }

    /**
     * ????????????????????????
     */
    private void setTraceBtnStyle() {
        boolean isTraceStarted = trackApp.trackConf.getBoolean("is_trace_started", false);
        if (isTraceStarted) {
            traceBtn.setText(R.string.stop_trace);
            traceBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color
                    .white, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            } else {
                traceBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            }
        } else {
            traceBtn.setText(R.string.start_trace);
            traceBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.layout_title, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            } else {
                traceBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            }
        }
    }

    /**
     * ????????????????????????
     */
    private void setGatherBtnStyle() {
        boolean isGatherStarted = trackApp.trackConf.getBoolean("is_gather_started", false);
        if (isGatherStarted) {
            gatherBtn.setText(R.string.stop_gather);
            gatherBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            } else {
                gatherBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            }
        } else {
            gatherBtn.setText(R.string.start_gather);
            gatherBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.layout_title, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            } else {
                gatherBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            }
        }
    }

    /**
     * ??????????????????
     *
     * @author baidu
     */
    class RealTimeLocRunnable implements Runnable {

        private int interval = 0;

        public RealTimeLocRunnable(int interval) {
            this.interval = interval;
        }

        @Override
        public void run() {
            if (isRealTimeRunning) {
                trackApp.getCurrentLocation(entityListener, trackListener);
                realTimeHandler.postDelayed(this, interval * 1000);
            }
        }
    }

    public void startRealTimeLoc(int interval) {
        isRealTimeRunning = true;
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
        realTimeHandler.post(realTimeLocRunnable);
    }

    public void stopRealTimeLoc() {
        isRealTimeRunning = false;
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
        trackApp.mClient.stopRealTimeLoc();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null == data) {
            return;
        }

        if (data.hasExtra("locationMode")) {
            LocationMode locationMode = LocationMode.valueOf(data.getStringExtra("locationMode"));
            trackApp.mClient.setLocationMode(locationMode);
        }

        if (data.hasExtra("isNeedObjectStorage")) {
            boolean isNeedObjectStorage = data.getBooleanExtra("isNeedObjectStorage", false);
            trackApp.mTrace.setNeedObjectStorage(isNeedObjectStorage);
        }

        if (data.hasExtra("gatherInterval") || data.hasExtra("packInterval")) {
            int gatherInterval = data.getIntExtra("gatherInterval", Constants.DEFAULT_GATHER_INTERVAL);
            int packInterval = data.getIntExtra("packInterval", Constants.DEFAULT_PACK_INTERVAL);
            TracingActivity.this.packInterval = packInterval;
            trackApp.mClient.setInterval(gatherInterval, packInterval);
        }

        //        if (data.hasExtra("supplementMode")) {
        //            mSupplementMode = SupplementMode.valueOf(data.getStringExtra("supplementMode"));
        //        }
    }

    private void initListener() {

        trackListener = new OnTrackListener() {

            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    return;
                }

                LatestPoint point = response.getLatestPoint();
                if (null == point || CommonUtil.isZeroPoint(point.getLocation().getLatitude(), point.getLocation()
                        .getLongitude())) {
                    return;
                }

                LatLng currentLatLng = mapUtil.convertTrace2Map(point.getLocation());
                if (null == currentLatLng) {
                    return;
                }

                CurrentLocation.locTime = point.getLocTime();
                Log.e("locTime",CurrentLocation.locTime+"");

                CurrentLocation.latitude = currentLatLng.latitude;
                Log.e("latitude",CurrentLocation.latitude+"");
                CurrentLocation.longitude = currentLatLng.longitude;
                Log.e("longitude",CurrentLocation.longitude+"");

                LatLng latLng = new LatLng(currentLatLng.latitude,currentLatLng.longitude);
                if(latLng != null){
                    pointList.add(latLng);
                    drawRealtimePoint(latLng);
                }
                else {
                    Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_LONG).show();
                }


                    if (null != mapUtil) {
//                    Toast.makeText(this,"",Toast.LENGTH_LONG).show();
                    mapUtil.updateStatus(currentLatLng, true);
                }
            }
        };

        entityListener = new OnEntityListener() {

            @Override
            public void onReceiveLocation(TraceLocation location) {

                if (StatusCodes.SUCCESS != location.getStatus() || CommonUtil.isZeroPoint(location.getLatitude(),
                        location.getLongitude())) {
                    return;
                }
                LatLng currentLatLng = mapUtil.convertTraceLocation2Map(location);
                if (null == currentLatLng) {
                    return;
                }
                CurrentLocation.locTime = CommonUtil.toTimeStamp(location.getTime());
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;
                Log.e("locTime",CurrentLocation.locTime+"");

                Log.e("latitude",CurrentLocation.latitude+"");

                Log.e("longitude",CurrentLocation.longitude+"");

                if (null != mapUtil) {
                    mapUtil.updateStatus(currentLatLng, true);
                }
            }

        };

        traceListener = new OnTraceListener() {

            /**
             * ????????????????????????
             * @param errorNo  ?????????
             * @param message ??????
             *                <p>
             *                <pre>0????????? </pre>
             *                <pre>1?????????</pre>
             */
            @Override
            public void onBindServiceCallback(int errorNo, String message) {
                Log.e("bindService","??????????????????");
                Log.e("errorNo",errorNo+"");
                Log.e("message",message+"");
                viewUtil.showToast(TracingActivity.this,
                        String.format("onBindServiceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * ????????????????????????
             * @param errorNo ?????????
             * @param message ??????
             *                <p>
             *                <pre>0????????? </pre>
             *                <pre>10000?????????????????????</pre>
             *                <pre>10001?????????????????????</pre>
             *                <pre>10002???????????????</pre>
             *                <pre>10003?????????????????????</pre>
             *                <pre>10004??????????????????</pre>
             *                <pre>10005?????????????????????</pre>
             *                <pre>10006??????????????????</pre>
             */
            @Override
            public void onStartTraceCallback(int errorNo, String message) {
                Log.e("TAG","??????????????????");
                Log.e("TAG",errorNo+"");
                Log.e("TAG",message);
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= errorNo) {
                    trackApp.isTraceStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_trace_started", true);
                    editor.apply();
                    setTraceBtnStyle();
                    registerReceiver();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * ????????????????????????
             * @param errorNo ?????????
             * @param message ??????
             *                <p>
             *                <pre>0?????????</pre>
             *                <pre>11000?????????????????????</pre>
             *                <pre>11001?????????????????????</pre>
             *                <pre>11002??????????????????</pre>
             *                <pre>11003?????????????????????</pre>
             */
            @Override
            public void onStopTraceCallback(int errorNo, String message) {
                Log.e("TAG","??????????????????");
                Log.e("TAG",errorNo+"");
                Log.e("TAG",message+"");
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.CACHE_TRACK_NOT_UPLOAD == errorNo) {
                    trackApp.isTraceStarted = false;
                    trackApp.isGatherStarted = false;
                    // ??????????????????????????????is_trace_started??????????????????????????????????????????????????????????????????????????????
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_trace_started");
                    editor.remove("is_gather_started");
                    editor.apply();
                    setTraceBtnStyle();
                    setGatherBtnStyle();
                    unregisterPowerReceiver();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStopTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * ????????????????????????
             * @param errorNo ?????????
             * @param message ??????
             *                <p>
             *                <pre>0?????????</pre>
             *                <pre>12000?????????????????????</pre>
             *                <pre>12001?????????????????????</pre>
             *                <pre>12002??????????????????</pre>
             */
            @Override
            public void onStartGatherCallback(int errorNo, String message) {
                Log.e("TAG","??????????????????");
                Log.e("TAG",errorNo+"");
                Log.e("TAG",message+"");
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STARTED == errorNo) {
                    trackApp.isGatherStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_gather_started", true);
                    editor.apply();
                    setGatherBtnStyle();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStartGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * ????????????????????????
             * @param errorNo ?????????
             * @param message ??????
             *                <p>
             *                <pre>0?????????</pre>
             *                <pre>13000?????????????????????</pre>
             *                <pre>13001?????????????????????</pre>
             *                <pre>13002??????????????????</pre>
             */
            @Override
            public void onStopGatherCallback(int errorNo, String message) {
                Log.e("TAG","??????????????????");
                Log.e("TAG",errorNo+"");
                Log.e("TAG",message+"");
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STOPPED == errorNo) {
                    trackApp.isGatherStarted = false;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_gather_started");
                    editor.apply();
                    setGatherBtnStyle();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStopGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * ????????????????????????
             *
             * @param messageType ?????????
             * @param pushMessage ??????
             *                  <p>
             *                  <pre>0x01???????????????</pre>
             *                  <pre>0x02???????????????</pre>
             *                  <pre>0x03??????????????????????????????</pre>
             *                  <pre>0x04???????????????????????????</pre>
             *                  <pre>0x05~0x40???????????????</pre>
             *                  <pre>0x41~0xFF?????????????????????</pre>
             */
            @Override
            public void onPushCallback(byte messageType, PushMessage pushMessage) {
                Log.e("TAG","??????????????????");
                Log.e("TAG",messageType+"");
                Log.e("TAG",pushMessage+"");
                if (messageType < 0x03 || messageType > 0x04) {
                    viewUtil.showToast(TracingActivity.this, pushMessage.getMessage());
                    return;
                }
                FenceAlarmPushInfo alarmPushInfo = pushMessage.getFenceAlarmPushInfo();
                if (null == alarmPushInfo) {
                    viewUtil.showToast(TracingActivity.this,
                            String.format("onPushCallback, messageType:%d, messageContent:%s ", messageType,
                                    pushMessage));
                    return;
                }
                StringBuffer alarmInfo = new StringBuffer();
                alarmInfo.append("??????")
                        .append(CommonUtil.getHMS(alarmPushInfo.getCurrentPoint().getLocTime() * 1000))
                        .append(alarmPushInfo.getMonitoredAction() == MonitoredAction.enter ? "??????" : "??????")
                        .append(messageType == 0x03 ? "??????" : "??????")
                        .append("?????????").append(alarmPushInfo.getFenceName());

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    Notification notification = new Notification.Builder(trackApp)
                            .setContentTitle(getResources().getString(R.string.alarm_push_title))
                            .setContentText(alarmInfo.toString())
                            .setSmallIcon(R.mipmap.icon_app)
                            .setWhen(System.currentTimeMillis()).build();
                    notificationManager.notify(notifyId++, notification);
                }
            }

            @Override
            public void onInitBOSCallback(int errorNo, String message) {
                viewUtil.showToast(TracingActivity.this,
                        String.format("onInitBOSCallback, errorNo:%d, message:%s ", errorNo, message));
            }
        };

    }

    static class RealTimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    /**
     * ???????????????????????????GPS?????????
     */
    @SuppressLint("InvalidWakeLockTag")
    private void registerReceiver() {
        if (trackApp.isRegisterReceiver) {
            return;
        }

        if (null == wakeLock) {
            Toast.makeText(this,"wakeLock",Toast.LENGTH_LONG).show();
            wakeLock = powerManager.newWakeLock(PARTIAL_WAKE_LOCK, "track upload");
        }
        if (null == trackReceiver) {
            Toast.makeText(this,"trackReceiver",Toast.LENGTH_LONG).show();
            trackReceiver = new TrackReceiver(wakeLock);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(StatusCodes.GPS_STATUS_ACTION);
        trackApp.registerReceiver(trackReceiver, filter);
        trackApp.isRegisterReceiver = true;

    }

    private void unregisterPowerReceiver() {
        if (!trackApp.isRegisterReceiver) {
            return;
        }
        if (null != trackReceiver) {
            trackApp.unregisterReceiver(trackReceiver);
        }
        trackApp.isRegisterReceiver = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startRealTimeLoc(packInterval);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapUtil.onResume();

        // ???Android 6.0??????????????????????????????????????????doze?????????????????????????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = trackApp.getPackageName();
            boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName);
            if (!isIgnoring) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapUtil.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRealTimeLoc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapUtil.clear();
        stopRealTimeLoc();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_tracing;
    }

    /**
     * ?????????????????????
     * @param point
     */
    private void drawRealtimePoint(LatLng point){
        mapUtil.baiduMap.clear();
//        if (pointList == null || pointList.size() == 0) {
//            if (null != mapUtil.polylineOverlay) {
//                mapUtil.polylineOverlay.remove();
//                mapUtil.polylineOverlay = null;
//            }
//            return;
//        }

        if (pointList.size() == 1) {
            OverlayOptions startOptions = new MarkerOptions().position(pointList.get(0)).icon(bmStart).zIndex(9).draggable(true);
            mapUtil.baiduMap.addOverlay(startOptions);
            mapUtil.animateMapStatus(pointList.get(0), 18.0f);
            return;
        }

        MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(18).build();
        msUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        BitmapDescriptor realtimeBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_st_map);
        overlay = new MarkerOptions().position(point)
                .icon(realtimeBitmap).zIndex(9).draggable(true);

        if(pointList.size() >= 2  && pointList.size() <= 1000){
            polyline = new PolylineOptions().width(10).color(Color.RED).points(pointList);
        }

//        // ??????????????????
//        OverlayOptions startOptions = new MarkerOptions()
//                .position(pointList.get(0)).icon(bmStart)
//                .zIndex(9).draggable(true);
//        mapUtil.baiduMap.addOverlay(startOptions);
        addMarker();

    }

    private void addMarker(){

        if(msUpdate != null){
            mapUtil.baiduMap.setMapStatus(msUpdate);
        }

        if(polyline != null){
            mapUtil.baiduMap.addOverlay(polyline);
        }

        if(overlay != null){
            mapUtil.baiduMap.addOverlay(overlay);
        }

    }
}
