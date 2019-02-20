package cn.lhlyblog.map_demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import cn.lhlyblog.map_demo.util.Constants;
import cn.lhlyblog.map_demo.util.SensorEventHelper;
import cn.lhlyblog.map_demo.util.ToastUtil;
import cn.lhlyblog.map_demo.view.PointsActivity;
import cn.lhlyblog.map_demo.view.WalkRouteActivity;

/**
 * 洛师地图
 */
public class MainActivity extends CheckPermissions
        implements LocationSource, AMapLocationListener {
    private long currentTime;
    private static final String TAG = "-----MainActivity----";
    private WifiManager mWifiManager;
    private TextView mLocationErrText;
    private Button button;
    private AutoCompleteTextView auto_edit;
    private AMap aMap;
    private SensorEventHelper sensorEventhelper;
    private Marker marker;
    private Constants constants;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private Circle ac;
    private Circle c;
    private long start = 0;
    //    private final Interpolator interpolator = new CycleInterpolator(1);
    private final Interpolator interpolator1 = new LinearInterpolator();
    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    LatLng southwestLatLng = new LatLng(34.62317, 112.597139);
    LatLng northeastLatLng = new LatLng(34.63627, 112.613232);
    LatLngBounds latLngBounds = new LatLngBounds.Builder()
            .include(southwestLatLng).include(northeastLatLng).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationErrText = findViewById(R.id.location_errInfo_text);
        mWifiManager = (WifiManager) this.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        button = findViewById(R.id.btn_list);
        auto_edit = findViewById(R.id.auto_edit);
        auto_edit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                constants.setAIDLATLNG(Constants.LOC_POINTS.get(getPosition(((TextView) view.findViewById(android.R.id.text1)).getText().toString())));
                Intent intent = new Intent(MainActivity.this, WalkRouteActivity.class);
                startActivity(intent);
            }
        });
        init();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PointsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 初始化相关信息
     */
    private void init() {
        setUpMapIfNeeded();
        //初始化传感器、注册监听
        if (sensorEventhelper == null) {
            sensorEventhelper = new SensorEventHelper(this);
            sensorEventhelper.registerSensorListener();
        }
    }

    /**
     * 初始化信息(锁定范围、注册定位监听、设置定位模式)
     */
    private void setUpMap() {
        aMap = ((SupportMapFragment) Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.map))).getMap();
//        aMap.setMapStatusLimits(latLngBounds);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        //初始化autoCompleteText
        auto_edit.setAdapter(getInfo());     // 绑定adapter
    }

    /**
     * 初始一次aMap
     */
    private void setUpMapIfNeeded() {
        if (aMap == null) {
            setUpMap();
        }
    }

    /**
     * 检查wifi是否启动
     * 没启动则提示用户启动
     */
    private void checkWifiSetting() {
        if (mWifiManager.isWifiEnabled()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("开启WIFI模块会提升定位准确性"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton("去开启", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
                Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent); // 打开系统设置界面
            }
        });
        builder.setNegativeButton("不了", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    /**
     * 初始化aMap
     * 方向传感器注册
     */
    @Override
    protected void onResume() {
        super.onResume();
        init();
        activate(mListener);
    }

    /**
     * 销毁方向传感服务
     * 销毁定位服务
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (sensorEventhelper != null) {
            sensorEventhelper.unRegisterSensorListener();
            sensorEventhelper.setCurrentMarker(null);
            sensorEventhelper = null;
        }
        deactivate();
    }

    /**
     * 销毁定位点
     * 销毁定位服务
     * 销毁经度圈定时器
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (marker != null) {
            marker.destroy();
        }
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        try {
            mTimer.cancel();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        Log.d(TAG, "activate: ");
        checkWifiSetting();
        mListener = listener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2000);
            mLocationOption.setSensorEnable(true);
//            设置为单次定位
//            mLocationOption.setOnceLocation(true);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
        } else {
            mLocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     * 销毁定位服务
     */
    @Override
    public void deactivate() {
        Log.d(TAG, "deactivate: ");
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        Log.d(TAG, "onLocationChanged: ");
        if (mListener != null && amapLocation != null) {
            if (mTimerTask != null) {
                mTimerTask.cancel();
                mTimerTask = null;
            }
            if (amapLocation.getErrorCode() == 0) {
                LatLng location = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                mLocationErrText.setVisibility(View.GONE);
//                Constants constants = new Constants();
                constants.setMYLATLNG(location);
                // Log.e("showMyLoc", "onLocationChanged: " + constants.getMYLATLNG());;
                /*mListener.onLocationChanged(amapLocation);// 显示系统小蓝点*/
                addMarker(location, 50);
                sensorEventhelper.setCurrentMarker(marker);
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
                mLocationErrText.setVisibility(View.VISIBLE);
                mLocationErrText.setText(errText);
            }
        }
    }

    /**
     * 添加定位标记点(包括更新)
     */
    private void addMarker(LatLng latLng, float accuracy) {
        Log.d(TAG, "addMarker: 精度值:" + accuracy);
        if (marker != null) {
            marker.setPosition(latLng);
            ac.setCenter(latLng);
            ac.setRadius(accuracy);
            c.setCenter(latLng);
            c.setRadius(accuracy);
        } else {
            MarkerOptions options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                    R.mipmap.locked)));
            options.anchor(0.5f, 0.5f);
            options.position(latLng);
            marker = aMap.addMarker(options);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
            //设置精度圆信息
            ac = aMap.addCircle(new CircleOptions().center(latLng)
                    .fillColor(Color.argb(100, 255, 218, 185)).radius(accuracy)
                    .strokeColor(Color.argb(255, 255, 228, 185)).strokeWidth(5));
            c = aMap.addCircle(new CircleOptions().center(latLng)
                    .fillColor(Color.argb(70, 255, 218, 185)).radius(accuracy)
                    .strokeColor(Color.argb(255, 255, 228, 185)).strokeWidth(0));

        }
        Scalecircle(c);
    }

    /**
     * 缩放定位圈(更新)
     */
    public void Scalecircle(final Circle circle) {
        start = SystemClock.uptimeMillis();
        if (mTimerTask == null) {
            mTimerTask = new CircleTask(circle, 1000);
        }
        mTimer.schedule(mTimerTask, 0, 30);
    }

    /**
     * 获取全部常量点位
     *
     * @return autoComplete适配器
     */
    private ArrayAdapter<String> getInfo() {
        if (constants == null) {
            constants = new Constants();
        }
        return new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Constants.POINTS);
    }

    /**
     * 根据点位名称，获取下标
     *
     * @param name 名称
     * @return 下标
     */
    private int getPosition(String name) {
        for (int i = 0; i < Constants.POINTS.size(); i++) {
            if (name.equals(Constants.POINTS.get(i))) {
                return i;
            }
        }
        return Constants.POINTS.size();
    }

    /**
     * 两秒内多次点击back退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - currentTime > 2000) {
                currentTime = System.currentTimeMillis();
                ToastUtil.show(this, "再按一次后退键退出程序");
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class CircleTask extends TimerTask {
        private double r;
        private Circle circle;
        private long duration = 1000;

        CircleTask(Circle circle, long rate) {
            this.circle = circle;
            this.r = circle.getRadius();
            if (rate > 0) {
                this.duration = rate;
            }
        }

        @Override
        public void run() {
            try {
                long elapsed = SystemClock.uptimeMillis() - start;
                float input = (float) elapsed / duration;
//                外圈循环缩放
//                float t = interpolator.getInterpolation((float)(input-0.25));//return (float)(Math.sin(2 * mCycles * Math.PI * input))
//                double r1 = (t + 2) * r;
//                外圈放大后消失
                float t = interpolator1.getInterpolation(input);
                double r1 = (t + 1) * r;
                circle.setRadius(r1);
                if (input > 2)
                    start = SystemClock.uptimeMillis();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}