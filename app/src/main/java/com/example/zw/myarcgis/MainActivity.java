package com.example.zw.myarcgis;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    MapView map;
    MyTouchListener myListener = null;
    GraphicsLayer graphicsLayer;
    ArrayList<Point> points = new ArrayList<Point>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.map);
        // 获取地图和XML布局的初始程度
        //添加动态层MapView
        map.addLayer(new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
        map.setOnStatusChangedListener(new OnStatusChangedListener() {
            public void onStatusChanged(Object source, STATUS status) {
                if (source == map && status == STATUS.INITIALIZED) {

                    LocationDisplayManager ldm = map.getLocationDisplayManager();
                    ldm.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                    ldm.start();
                    //移动到当前位置
                    System.out.println("mmmmmmmmmmmmmmmmmylocation==>"+ldm.getLocation().getLatitude());
                    ShowLocation(ldm.getLocation().getLongitude(),ldm.getLocation().getLatitude());
                }
            }
        });

        graphicsLayer = new GraphicsLayer();
        map.addLayer(graphicsLayer);
        myListener = new MyTouchListener(MainActivity.this, map);
        map.setOnTouchListener(myListener);
    }

    //地图触摸事件
    class MyTouchListener extends MapOnTouchListener {
        MultiPath poly;
        String type = "POINT";
        Point startPoint =null;

        public MyTouchListener(Context context, MapView view) {
            super(context, view);
        }

        public void setType(String geometryType) {
            this.type = geometryType;
        }

        public String getType() {
            return this.type;
        }

        //获取点击位置坐标
        public boolean onSingleTap(MotionEvent e) {
            if (type.length() > 1 && type.equalsIgnoreCase("POINT")) {
                graphicsLayer.removeAll();
                Graphic graphic = new Graphic(map.toMapPoint(new Point(e.getX(), e.getY())), new SimpleMarkerSymbol(Color.BLUE, 25, SimpleMarkerSymbol.STYLE.CIRCLE));
                Point pa = map.toMapPoint(e.getX(), e.getY());
                graphicsLayer.addGraphic(graphic);
                //转化为投影坐标
                Point po=new Point(pa.getX(), pa.getY());
                points.add(po);
                drawPolyline();
                return true;
            }
            return false;
        }

    }

    //划线
    private void drawPolyline() {
        String editingmode="POLYLINE";
        if (graphicsLayer == null) {
            graphicsLayer = new GraphicsLayer();
            map.addLayer(graphicsLayer);
        }
        if (points.size() <= 1)
            return;
        Graphic graphic;
        MultiPath multipath;
        multipath = new Polyline();
        multipath.startPath(points.get(0));
        for (int i = 1; i < points.size(); i++) {
            multipath.lineTo(points.get(i));
        }
        System.out.println("aaaaaaaaaaa==>DrawPolyline: Array coutn = "+points.size());
        graphic = new Graphic(multipath, new SimpleLineSymbol(Color.RED, 4));
        graphicsLayer.addGraphic(graphic);
    }

    //将地图移动到当前位置
    public void ShowLocation(double locx,double locy) {
        PictureMarkerSymbol locationPH = new PictureMarkerSymbol(MainActivity.this.getResources().getDrawable(R.drawable.touming));
        Point wgspoint = new Point(locx, locy);
        Point mapPoint = (Point) GeometryEngine.project(wgspoint,
                SpatialReference.create(4326), map.getSpatialReference());
        graphicsLayer.removeAll();
        graphicsLayer.addGraphic(new Graphic(mapPoint, locationPH));
        map.centerAt(mapPoint, true);// 漫游到当前位置
//        }
    }


    protected void onPause() {
        super.onPause();
        map.pause();
    }

    protected void onResume() {
        super.onResume();
        map.unpause();
    }
}
