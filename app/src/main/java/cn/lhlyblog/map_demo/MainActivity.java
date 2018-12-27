package cn.lhlyblog.map_demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import cn.lhlyblog.map_demo.util.Constants;
import cn.lhlyblog.map_demo.util.SensorEventHelper;
import cn.lhlyblog.map_demo.view.PointsActivity;

public class MainActivity extends CheckPermissions
        implements LocationSource, AMapLocationListener {

    private WifiManager mWifiManager;
    private TextView mLocationErrText;
    private Button button;

    private AMap aMap;
    private SensorEventHelper sensorEventhelper;
    private Marker marker;

    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;

    LatLng southwestLatLng = new LatLng(34.62317, 112.597139);
    LatLng northeastLatLng = new LatLng(34.63627, 112.613232);
    LatLngBounds latLngBounds = new LatLngBounds.Builder()
            .include(southwestLatLng).include(northeastLatLng).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationErrText = (TextView) findViewById(R.id.location_errInfo_text);
        mWifiManager = (WifiManager) this.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        button = (Button) findViewById(R.id.btn_list);

        init();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PointsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init() {
        setUpMapIfNeeded();
        sensorEventhelper = new SensorEventHelper(this);
        if (sensorEventhelper != null) {
            sensorEventhelper.registerSensorListener();
        }
    }

    private void setUpMap() {
        aMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        aMap.setMapStatusLimits(latLngBounds);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    private void setUpMapIfNeeded() {
        if (aMap == null) {
            setUpMap();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (marker != null) {
            marker.destroy();
        }
        if (mlocationClient != null) {
            mlocationClient.onDestroy();
        }
    }


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

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (sensorEventhelper != null) {
            sensorEventhelper.registerSensorListener();
        }
    }


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
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                LatLng location = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                mLocationErrText.setVisibility(View.GONE);
                Constants constants = new Constants();
                constants.setMYLATLNG(location);
                // Log.e("showMyLoc", "onLocationChanged: " + constants.getMYLATLNG());;
                /*mListener.onLocationChanged(amapLocation);// 显示系统小蓝点*/
                addMarker(location);
                sensorEventhelper.setCurrentMarker(marker);
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
                mLocationErrText.setVisibility(View.VISIBLE);
                mLocationErrText.setText(errText);
            }
        }
    }

    private void addMarker(LatLng latLng) {
        if (marker != null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.locked)));
        options.anchor(0.5f, 0.5f);
        options.position(latLng);
        marker = aMap.addMarker(options);
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        checkWifiSetting();
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2000);
            mLocationOption.setSensorEnable(true);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();

        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
}