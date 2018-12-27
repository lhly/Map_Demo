package cn.lhlyblog.map_demo.util;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    public static final List<String> POINTS = new ArrayList<>();
    public static final List<LatLng> LOC_POINTS = new ArrayList<>();
    public LatLng MYLATLNG;

    public Constants() {
        POINTS.add("西门");
        POINTS.add("桃园宿舍");
        POINTS.add("桃园操场");

        LOC_POINTS.add(new LatLng(34.629523, 112.59804));
        LOC_POINTS.add(new LatLng(34.628357, 112.599295));
        LOC_POINTS.add(new LatLng(34.626503, 112.598512));
    }

    public void setMYLATLNG(LatLng latLng) {
        MYLATLNG = latLng;
    }

    public LatLng getMYLATLNG() {
        return MYLATLNG;
    }
}
