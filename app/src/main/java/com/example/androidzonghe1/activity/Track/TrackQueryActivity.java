package com.example.androidzonghe1.activity.Track;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.analysis.DrivingBehaviorRequest;
import com.baidu.trace.api.analysis.DrivingBehaviorResponse;
import com.baidu.trace.api.analysis.HarshAccelerationPoint;
import com.baidu.trace.api.analysis.HarshBreakingPoint;
import com.baidu.trace.api.analysis.HarshSteeringPoint;
import com.baidu.trace.api.analysis.OnAnalysisListener;
import com.baidu.trace.api.analysis.SpeedingInfo;
import com.baidu.trace.api.analysis.SpeedingPoint;
import com.baidu.trace.api.analysis.StayPoint;
import com.baidu.trace.api.analysis.StayPointRequest;
import com.baidu.trace.api.analysis.StayPointResponse;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.Point;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.api.track.SupplementMode;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TransportMode;
import com.baidu.trace.model.CoordType;
import com.example.androidzonghe1.R;
import com.example.androidzonghe1.activity.lpyWork.BaseActivity;
import com.example.androidzonghe1.activity.lpyWork.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * ????????????
 */
public class TrackQueryActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, BaiduMap.OnMarkerClickListener , BaiduMap.OnMapClickListener {

    private TrackApplication trackApp = null;

    private ViewUtil viewUtil = null;

    /**
     * ????????????
     */
    private MapUtil mapUtil = null;

    /**
     * ??????????????????
     */
    private HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest();

    /**
     * ???????????????????????????????????????????????????
     */
    private OnTrackListener mTrackListener = null;

    /**
     * ?????????????????????
     */
    private TrackAnalysisDialog trackAnalysisDialog = null;

    /**
     * ???????????????????????????
     */
    private TrackAnalysisInfoLayout trackAnalysisInfoLayout = null;

    /**
     * ????????????????????????????????????marker
     */
    private Marker analysisMarker = null;

    /**
     * ??????????????????
     */
    private DrivingBehaviorRequest drivingBehaviorRequest = new DrivingBehaviorRequest();

    /**
     * ???????????????
     */
    private StayPointRequest stayPointRequest = new StayPointRequest();

    /**
     * ?????????????????????
     */
    private OnAnalysisListener mAnalysisListener = null;

    /**
     * ???????????????????????????
     */
    private long startTime = CommonUtil.getCurrentTime();

    /**
     * ???????????????????????????
     */
    private long endTime = CommonUtil.getCurrentTime();

    /**
     * ???????????????
     */
    private List<LatLng> trackPoints = new ArrayList<>();

    /**
     * ????????????  ???????????????
     */
    private List<Point> speedingPoints = new ArrayList<>();

    /**
     * ????????????  ??????????????????
     */
    private List<Point> harshAccelPoints = new ArrayList<>();

    /**
     * ????????????  ??????????????????
     */
    private List<Point> harshBreakingPoints = new ArrayList<>();

    /**
     * ????????????  ??????????????????
     */
    private List<Point> harshSteeringPoints = new ArrayList<>();

    /**
     * ????????????  ???????????????
     */
    private List<Point> stayPoints = new ArrayList<>();

    /**
     * ???????????? ????????????????????????
     */
    private List<Marker> speedingMarkers = new ArrayList<>();

    /**
     * ???????????? ???????????????????????????
     */
    private List<Marker> harshAccelMarkers = new ArrayList<>();

    /**
     * ????????????  ???????????????????????????
     */
    private List<Marker> harshBreakingMarkers = new ArrayList<>();

    /**
     * ????????????  ???????????????????????????
     */
    private List<Marker> harshSteeringMarkers = new ArrayList<>();

    /**
     * ????????????  ????????????????????????
     */
    private List<Marker> stayPointMarkers = new ArrayList<>();

    /**
     * ?????????????????????
     */
    private boolean isSpeeding = false;

    /**
     * ????????????????????????
     */
    private boolean isHarshAccel = false;

    /**
     * ????????????????????????
     */
    private boolean isHarshBreaking = false;

    /**
     * ????????????????????????
     */
    private boolean isHarshSteering = false;

    /**
     * ?????????????????????
     */
    private boolean isStayPoint = false;

    /**
     * ??????????????????
     */
    private SortType sortType = SortType.asc;

    private int pageIndex = 1;

    /**
     * ?????????????????????????????????
     */
    private long lastQueryTime = 0;

    /**
     * ??????????????????????????????
     */
    private TextView mHistoryTrackView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.track_query_title);
        setOnClickListener(this);
        trackApp = (TrackApplication) getApplicationContext();
        init();
    }

    /**
     * ?????????
     */
    private void init() {
        BitmapUtil.init();
        viewUtil = new ViewUtil();
        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) findViewById(R.id.track_query_mapView));
        mapUtil.baiduMap.setOnMarkerClickListener(this);
        mapUtil.baiduMap.setOnMapClickListener(this);
        mapUtil.setCenter(trackApp);
        trackAnalysisInfoLayout = new TrackAnalysisInfoLayout(this, mapUtil.baiduMap);
        initListener();
    }

    /**
     * ????????????
     *
     * @param v
     */
    public void onTrackAnalysis(View v) {
        if (null != mapUtil.mapView) {
            mapUtil.mapView.removeView(mHistoryTrackView);
            mapUtil.baiduMap.setViewPadding(0, 0, 0, 0);
        }
        if (null == trackAnalysisDialog) {
            trackAnalysisDialog = new TrackAnalysisDialog(this);
        }
        // ????????????
        trackAnalysisDialog.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        // ??????PopupWindow???Android N???????????????????????????
        if (Build.VERSION.SDK_INT < 24) {
            trackAnalysisDialog.update(trackAnalysisDialog.getWidth(), trackAnalysisDialog.getHeight());
        }
        if (CommonUtil.getCurrentTime() - lastQueryTime > Constants.ANALYSIS_QUERY_INTERVAL) {
            lastQueryTime = CommonUtil.getCurrentTime();
            speedingPoints.clear();
            harshAccelPoints.clear();
            harshBreakingPoints.clear();
            stayPoints.clear();
            queryDrivingBehavior();
            queryStayPoint();
        }

    }

    /**
     * ????????????????????????
     *
     * @param historyTrackRequestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int historyTrackRequestCode, int resultCode, Intent data) {
        if (null == data) {
            return;
        }

        trackPoints.clear();
        pageIndex = 1;

        if (data.hasExtra("startTime")) {
            startTime = data.getLongExtra("startTime", CommonUtil.getCurrentTime());
        }
        if (data.hasExtra("endTime")) {
            endTime = data.getLongExtra("endTime", CommonUtil.getCurrentTime());
        }

//        if (data.hasExtra("entityName")) {
//            Log.e("entityName", TrackApplication.entityName);
//            TrackApplication.entityName = data.getStringExtra("entityName");
//            Log.e("entityName", TrackApplication.entityName);
//            TrackApplication.initTrace();
//        }

        ProcessOption processOption = new ProcessOption();
        if (data.hasExtra("radius")) {
            processOption.setRadiusThreshold(data.getIntExtra("radius", Constants.DEFAULT_RADIUS_THRESHOLD));
        }
        if (data.hasExtra("transportMode")) {
            processOption.setTransportMode(TransportMode.valueOf(data.getStringExtra("transportMode")));
        }
        if (data.hasExtra("denoise")) {
            processOption.setNeedDenoise(data.getBooleanExtra("denoise", true));
        }
        if (data.hasExtra("vacuate")) {
            processOption.setNeedVacuate(data.getBooleanExtra("vacuate", true));
        }
        if (data.hasExtra("mapmatch")) {
            processOption.setNeedMapMatch(data.getBooleanExtra("mapmatch", true));
        }
        historyTrackRequest.setProcessOption(processOption);

        if (data.hasExtra("lowspeedthreshold")) {
            historyTrackRequest.setLowSpeedThreshold(data.getIntExtra("lowspeedthreshold",
                    Constants.DEFAULT_RADIUS_THRESHOLD));
        }
        if (data.hasExtra("supplementMode")) {
            historyTrackRequest.setSupplementMode(SupplementMode.valueOf(data.getStringExtra("supplementMode")));
        }
        if (data.hasExtra("sortType")) {
            sortType = SortType.valueOf(data.getStringExtra("sortType"));
            historyTrackRequest.setSortType(sortType);
        }
        if (data.hasExtra("coordTypeOutput")) {
            historyTrackRequest.setCoordTypeOutput(CoordType.valueOf(data.getStringExtra("coordTypeOutput")));
        }
        if (data.hasExtra("processed")) {
            historyTrackRequest.setProcessed(data.getBooleanExtra("processed", true));
        }

//        // ??????????????????
//        historyTrackRequest.setProcessed(true);
//// ????????????????????????
////        ProcessOption processOption = new ProcessOption();
//// ??????????????????
//        processOption.setNeedDenoise(true);
//// ??????????????????
//        processOption.setNeedVacuate(true);
//// ??????????????????
//        processOption.setNeedMapMatch(true);
//// ?????????????????????(??????????????????100???????????????)
//        processOption.setRadiusThreshold(100);
//// ???????????????????????????
//        processOption.setTransportMode(TransportMode.walking);
//// ??????????????????
//        historyTrackRequest.setProcessOption(processOption);
        queryHistoryTrack();
    }

    /**
     * ??????????????????
     */
    private void queryHistoryTrack() {
        trackApp.initRequest(historyTrackRequest);
//        Log.e("entityName", trackApp.entityName);
        historyTrackRequest.setEntityName(trackApp.entityName);
        historyTrackRequest.setStartTime(startTime);
        historyTrackRequest.setEndTime(endTime);
        historyTrackRequest.setPageIndex(pageIndex);
        historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
        trackApp.mClient.queryHistoryTrack(historyTrackRequest, mTrackListener);
    }

    /**
     * ??????????????????
     */
    private void queryDrivingBehavior() {
        trackApp.initRequest(drivingBehaviorRequest);
        drivingBehaviorRequest.setEntityName(trackApp.entityName);
        drivingBehaviorRequest.setStartTime(startTime);
        drivingBehaviorRequest.setEndTime(endTime);
        trackApp.mClient.queryDrivingBehavior(drivingBehaviorRequest, mAnalysisListener);
    }

    /**
     * ???????????????
     */
    private void queryStayPoint() {
        trackApp.initRequest(stayPointRequest);
        stayPointRequest.setEntityName(trackApp.entityName);
        stayPointRequest.setStartTime(startTime);
        stayPointRequest.setEndTime(endTime);
        stayPointRequest.setStayTime(Constants.STAY_TIME);
        trackApp.mClient.queryStayPoint(stayPointRequest, mAnalysisListener);
    }

    /**
     * ????????????????????? ??????????????????
     *
     * @param compoundButton
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

        switch (compoundButton.getId()) {
            case R.id.chk_speeding:
                isSpeeding = isChecked;
                handleMarker(speedingMarkers, isSpeeding);
                break;

            case R.id.chk_harsh_breaking:
                isHarshBreaking = isChecked;
                handleMarker(harshBreakingMarkers, isHarshBreaking);
                break;

            case R.id.chk_harsh_accel:
                isHarshAccel = isChecked;
                handleMarker(harshAccelMarkers, isHarshAccel);
                break;

            case R.id.chk_harsh_steering:
                isHarshSteering = isChecked;
                handleMarker(harshSteeringMarkers, isHarshSteering);
                break;

            case R.id.chk_stay_point:
                isStayPoint = isChecked;
                handleMarker(stayPointMarkers, isStayPoint);
                break;

            default:
                break;
        }
    }

    /**
     * ??????????????????
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // ??????????????????
            case R.id.btn_activity_options:
                ViewUtil.startActivityForResult(this, TrackQueryOptionsActivity.class, Constants.REQUEST_CODE);
                break;

            default:
                break;
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param marker
     *
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Bundle bundle = marker.getExtraInfo();
        // ??????bundle????????????marker?????????????????????????????????
        if (null == bundle || !marker.isVisible()) {
            return false;
        }
        int type = bundle.getInt("type");
        switch (type) {
            case R.id.chk_speeding:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_speeding_title);
                trackAnalysisInfoLayout.key1.setText(R.string.actual_speed);
                trackAnalysisInfoLayout.value1.setText(String.valueOf(bundle.getDouble("actualSpeed")));
                trackAnalysisInfoLayout.key2.setText(R.string.limit_speed);
                trackAnalysisInfoLayout.value2.setText(String.valueOf(bundle.getDouble("limitSpeed")));
                break;

            case R.id.chk_harsh_accel:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_accel_title);
                trackAnalysisInfoLayout.key1.setText(R.string.acceleration);
                trackAnalysisInfoLayout.value1.setText(String.valueOf(bundle.getDouble("acceleration")));
                trackAnalysisInfoLayout.key2.setText(R.string.initial_speed_2);
                trackAnalysisInfoLayout.value2.setText(String.valueOf(bundle.getDouble("initialSpeed")));
                trackAnalysisInfoLayout.key3.setText(R.string.end_speed_2);
                trackAnalysisInfoLayout.value3.setText(String.valueOf(bundle.getDouble("endSpeed")));
                break;

            case R.id.chk_harsh_breaking:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_breaking_title);
                trackAnalysisInfoLayout.key1.setText(R.string.acceleration);
                trackAnalysisInfoLayout.value1.setText(String.valueOf(bundle.getDouble("acceleration")));
                trackAnalysisInfoLayout.key2.setText(R.string.initial_speed_1);
                trackAnalysisInfoLayout.value2.setText(String.valueOf(bundle.getDouble("initialSpeed")));
                trackAnalysisInfoLayout.key3.setText(R.string.end_speed_1);
                trackAnalysisInfoLayout.value3.setText(String.valueOf(bundle.getDouble("endSpeed")));
                break;

            case R.id.chk_harsh_steering:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_steering_title);
                trackAnalysisInfoLayout.key1.setText(R.string.centripetal_acceleration);
                trackAnalysisInfoLayout.value1.setText(String.valueOf(bundle.getDouble("centripetalAcceleration")));
                trackAnalysisInfoLayout.key2.setText(R.string.turn_type);
                trackAnalysisInfoLayout.value2.setText(String.valueOf(bundle.getDouble("turnType")));
                trackAnalysisInfoLayout.key3.setText(R.string.turn_speed);
                trackAnalysisInfoLayout.value3.setText(String.valueOf(bundle.getDouble("turnSpeed")));
                break;

            case R.id.chk_stay_point:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_stay_title);
                trackAnalysisInfoLayout.key1.setText(R.string.stay_start_time);
                trackAnalysisInfoLayout.value1.setText(CommonUtil.formatTime(bundle.getLong("startTime") * 1000));
                trackAnalysisInfoLayout.key2.setText(R.string.stay_end_time);
                trackAnalysisInfoLayout.value2.setText(CommonUtil.formatTime(bundle.getLong("endTime") * 1000));
                trackAnalysisInfoLayout.key3.setText(R.string.stay_duration);
                trackAnalysisInfoLayout.value3.setText(CommonUtil.formatSecond(bundle.getInt("duration")));
                break;

            default:
                break;
        }
        //  ?????????????????????marker
        analysisMarker = marker;

        //??????InfoWindow , ?????? view??? ??????????????? y ????????????
        InfoWindow trackAnalysisInfoWindow = new InfoWindow(trackAnalysisInfoLayout.mView, marker.getPosition(), -47);
        //??????InfoWindow
        mapUtil.baiduMap.showInfoWindow(trackAnalysisInfoWindow);

        return false;
    }

    private void clearAnalysisList() {
        if (null != speedingPoints) {
            speedingPoints.clear();
        }
        if (null != harshAccelPoints) {
            harshAccelPoints.clear();
        }
        if (null != harshBreakingPoints) {
            harshBreakingPoints.clear();
        }
        if (null != harshSteeringPoints) {
            harshSteeringPoints.clear();
        }
    }

    private void initListener() {
        mTrackListener = new OnTrackListener() {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                int total = response.getTotal();
                StringBuffer sb = new StringBuffer(256);
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    viewUtil.showToast(TrackQueryActivity.this, response.getMessage());
                } else if (0 == total) {
                    viewUtil.showToast(TrackQueryActivity.this, getString(R.string.no_track_data));
                } else {
                    List<TrackPoint> points = response.getTrackPoints();
                    if (null != points) {
                        for (TrackPoint trackPoint : points) {
                            if (!CommonUtil.isZeroPoint(trackPoint.getLocation().getLatitude(),
                                    trackPoint.getLocation().getLongitude())) {
                                trackPoints.add(MapUtil.convertTrace2Map(trackPoint.getLocation()));
                            }
                        }
                    }
                    sb.append("????????????");
                    sb.append(response.getDistance());
                    sb.append("???");
                    sb.append("\n???????????????");
                    sb.append(response.getTollDistance());
                    sb.append("???");
                    sb.append("\n???????????????");
                    sb.append(response.getLowSpeedDistance());
                    sb.append("???");
                    addView(mapUtil.mapView);
                    mHistoryTrackView.setText(sb.toString());
                }

                if (total > Constants.PAGE_SIZE * pageIndex) {
                    historyTrackRequest.setPageIndex(++pageIndex);
                    queryHistoryTrack();
                } else {
                    mapUtil.drawHistoryTrack(trackPoints, sortType);
                }
            }

            @Override
            public void onDistanceCallback(DistanceResponse response) {
                super.onDistanceCallback(response);
            }

            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                super.onLatestPointCallback(response);
            }
        };

        mAnalysisListener = new OnAnalysisListener() {
            @Override
            public void onStayPointCallback(StayPointResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    lastQueryTime = 0;
                    viewUtil.showToast(TrackQueryActivity.this, response.getMessage());
                    return;
                }
                if (0 == response.getStayPointNum()) {
                    return;
                }
                stayPoints.addAll(response.getStayPoints());
                handleOverlays(stayPointMarkers, stayPoints, isStayPoint);
            }

            @Override
            public void onDrivingBehaviorCallback(DrivingBehaviorResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    lastQueryTime = 0;
                    viewUtil.showToast(TrackQueryActivity.this, response.getMessage());
                    return;
                }

                if (0 == response.getSpeedingNum() && 0 == response.getHarshAccelerationNum()
                        && 0 == response.getHarshBreakingNum() && 0 == response.getHarshSteeringNum()) {
                    return;
                }

                clearAnalysisList();
                clearAnalysisOverlay();

                List<SpeedingInfo> speedingInfos = response.getSpeedings();
                for (SpeedingInfo info : speedingInfos) {
                    speedingPoints.addAll(info.getPoints());
                }
                harshAccelPoints.addAll(response.getHarshAccelerationPoints());
                harshBreakingPoints.addAll(response.getHarshBreakingPoints());
                harshSteeringPoints.addAll(response.getHarshSteeringPoints());

                handleOverlays(speedingMarkers, speedingPoints, isSpeeding);
                handleOverlays(harshAccelMarkers, harshAccelPoints, isHarshAccel);
                handleOverlays(harshBreakingMarkers, harshBreakingPoints, isHarshBreaking);
                handleOverlays(harshSteeringMarkers, harshSteeringPoints, isHarshSteering);
            }
        };
    }

    /**
     * ???????????????????????????
     *
     * @param markers
     * @param points
     * @param isVisible
     */
    private void handleOverlays(List<Marker> markers, List<? extends Point> points, boolean
            isVisible) {
        if (null == markers || null == points) {
            return;
        }
        for (Point point : points) {
            OverlayOptions overlayOptions = new MarkerOptions()
                    .position(MapUtil.convertTrace2Map(point.getLocation()))
                    .icon(BitmapUtil.bmGcoding).zIndex(9).draggable(true);
            Marker marker = (Marker) mapUtil.baiduMap.addOverlay(overlayOptions);
            Bundle bundle = new Bundle();

            if (point instanceof SpeedingPoint) {
                SpeedingPoint speedingPoint = (SpeedingPoint) point;
                bundle.putInt("type", R.id.chk_speeding);
                bundle.putDouble("actualSpeed", speedingPoint.getActualSpeed());
                bundle.putDouble("limitSpeed", speedingPoint.getLimitSpeed());

            } else if (point instanceof HarshAccelerationPoint) {
                HarshAccelerationPoint accelPoint = (HarshAccelerationPoint) point;
                bundle.putInt("type", R.id.chk_harsh_accel);
                bundle.putDouble("acceleration", accelPoint.getAcceleration());
                bundle.putDouble("initialSpeed", accelPoint.getInitialSpeed());
                bundle.putDouble("endSpeed", accelPoint.getEndSpeed());

            } else if (point instanceof HarshBreakingPoint) {
                HarshBreakingPoint breakingPoint = (HarshBreakingPoint) point;
                bundle.putInt("type", R.id.chk_harsh_breaking);
                bundle.putDouble("acceleration", breakingPoint.getAcceleration());
                bundle.putDouble("initialSpeed", breakingPoint.getInitialSpeed());
                bundle.putDouble("endSpeed", breakingPoint.getEndSpeed());

            } else if (point instanceof HarshSteeringPoint) {
                HarshSteeringPoint steeringPoint = (HarshSteeringPoint) point;
                bundle.putInt("type", R.id.chk_harsh_steering);
                bundle.putDouble("centripetalAcceleration", steeringPoint.getCentripetalAcceleration());
                bundle.putString("turnType", steeringPoint.getTurnType().name());
                bundle.putDouble("turnSpeed", steeringPoint.getTurnSpeed());

            } else if (point instanceof StayPoint) {
                StayPoint stayPoint = (StayPoint) point;
                bundle.putInt("type", R.id.chk_stay_point);
                bundle.putLong("startTime", stayPoint.getStartTime());
                bundle.putLong("endTime", stayPoint.getEndTime());
                bundle.putInt("duration", stayPoint.getDuration());
            }
            marker.setExtraInfo(bundle);
            markers.add(marker);
        }

        handleMarker(markers, isVisible);
    }

    /**
     * ??????marker
     *
     * @param markers
     * @param isVisible
     */
    private void handleMarker(List<Marker> markers, boolean isVisible) {
        if (null == markers || markers.isEmpty()) {
            return;
        }
        for (Marker marker : markers) {
            marker.setVisible(isVisible);
        }

        if (markers.contains(analysisMarker)) {
            mapUtil.baiduMap.hideInfoWindow();
        }

    }

    /**
     * ??????view???????????????????????????????????????
     *
     * @param mapView MapView
     */
    private void addView(MapView mapView) {
        mHistoryTrackView = new TextView(this);
        mHistoryTrackView.setTextSize(15.0f);
        mHistoryTrackView.setTextColor(Color.BLACK);
        mHistoryTrackView.setBackgroundColor(Color.parseColor("#AAA9A9A9"));
        mHistoryTrackView.setMovementMethod(ScrollingMovementMethod.getInstance());

        MapViewLayoutParams.Builder builder = new MapViewLayoutParams.Builder();
        builder.layoutMode(MapViewLayoutParams.ELayoutMode.absoluteMode);
        builder.width(mapView.getWidth());
        builder.height(200);
        builder.point(new android.graphics.Point(0, mapView.getHeight()));
        builder.align(MapViewLayoutParams.ALIGN_LEFT, MapViewLayoutParams.ALIGN_BOTTOM);
        mapUtil.baiduMap.setViewPadding(0, 0, 0, 200);
        mapView.addView(mHistoryTrackView, builder.build());
    }

    /**
     * ?????????????????????????????????
     */
    public void clearAnalysisOverlay() {
        clearOverlays(speedingMarkers);
        clearOverlays(harshAccelMarkers);
        clearOverlays(harshBreakingMarkers);
        clearOverlays(stayPointMarkers);
    }

    private void clearOverlays(List<Marker> markers) {
        if (null == markers) {
            return;
        }
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapUtil.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapUtil.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != trackAnalysisInfoLayout) {
            trackAnalysisInfoLayout = null;
        }
        if (null != trackAnalysisDialog) {
            trackAnalysisDialog.dismiss();
            trackAnalysisDialog = null;
        }
        if (null != trackPoints) {
            trackPoints.clear();
        }
        if (null != stayPoints) {
            stayPoints.clear();
        }
        clearAnalysisList();
        trackPoints = null;
        speedingPoints = null;
        harshAccelPoints = null;
        harshSteeringPoints = null;
        stayPoints = null;

        clearAnalysisOverlay();
        speedingMarkers = null;
        harshAccelMarkers = null;
        harshBreakingMarkers = null;
        stayPointMarkers = null;

        mapUtil.clear();

    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_trackquery;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mapUtil.mapView.removeView(mHistoryTrackView);
        mapUtil.baiduMap.setViewPadding(0, 0, 0, 0);
    }

    @Override
    public void onMapPoiClick(MapPoi mapPoi) {

    }
}