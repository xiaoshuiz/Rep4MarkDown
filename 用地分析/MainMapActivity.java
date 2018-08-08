package com.example.shuiz.landofflineanalysis;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geodatabase.GeodatabaseFeature;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.query.QueryParameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import adapter.AnalysisResultListAdapter;
import application.App;
import common.Constants;
import model.DataSource;
import model.LandAnalysisResultInfo;
import model.ThemeInfo;
import tools.AnalysisUtil;
import tools.BaseMapManager;
import tools.MapViewTapTool;
import tools.MeasureManager;
import tools.OfflineDataTask;
import tools.SDCardReadUtil;
import tools.UtilsTool;

/**
 * @author shuiz
 */
public class MainMapActivity extends Activity {

    public static String TAG="mmcheck";
    /**
     * 数据源
     */
    public DataSource mDataSource;

    private BaseMapManager baseMapManager;
    private MeasureManager measureManager;
    /**
     * 离线layer的集合
     */
    private Map<String,Layer> layerMap;
    /**
     * Spinner选中图层 默认为第一个。
     */
    private Layer layer;
    /**
     * 全部Layer的List
     */
    private List<Layer> layerList;
    /**
     *绘制多边形的图层
     */
    private GraphicsLayer drawerLayer;
    /**
     * 结果高亮图层
     */
    private GraphicsLayer resultDrawerLayer;

    /**
     * 0: 未选择状态
     * 1：正在选择中
     * 2: 已有区域
     * 3：选择完成
     */
    public int selectStatus=0;
    /**
     * 是否为全选
     */
    public boolean isALL=true;
    /**
     * 分析结果
     */
    private List<LandAnalysisResultInfo> landAnalysisResultInfos;

    /**
     * View控件
     */
    public MapView mapView_main;
    private FloatingActionButton analysisBtn;
    private View analysisLayout;
    public Button btn_all,btn_select,btn_analysis,btn_close;
    private Spinner sp_theme;
    private ListView LV_result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();

    }


    private void initView() {
        setContentView(R.layout.activity_main_map);
        mapView_main =findViewById(R.id.map);
        readConfigFile();

    }
    private void loadData() {

        initMapView();
        loadOfflineLayer();
        loadMapLayer();
        initClickView();
        initGraphic();
        initMapViewListener();

    }
    private void loadOfflineLayer() {
        String filePath= SDCardReadUtil.ReadZTFile(Constants.GEOFILENAME);
        layerMap = UtilsTool.getGeoDataBaseFeatureLayer(filePath);
        layerList=new ArrayList<>();
        if (layerMap != null) {
            Layer layer = null;
            Iterator iteratorMap = layerMap.entrySet().iterator();
            while (iteratorMap.hasNext()) {
                Map.Entry entry = (Map.Entry) iteratorMap.next();
                layer = (Layer) entry.getValue();
                if (layer != null) {
                    layerList.add(layer);
                }
            }
        }
    }

    private void initMapViewListener() {
        mapView_main.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float x, float y) {
                if (!mapView_main.isLoaded()) {
                    return;
                }
                if(selectStatus==1||selectStatus==2){
                    if(measureManager !=null){
                        measureManager.onSingleTap(x,y, mapView_main);
                    }
                }
            }
        });
    }

    private void initGraphic() {
        drawerLayer=new GraphicsLayer();
        drawerLayer.setOpacity(0.5f);
        resultDrawerLayer=new GraphicsLayer();
        resultDrawerLayer.setOpacity(0.75f);
        mapView_main.addLayer(drawerLayer);
        mapView_main.addLayer(resultDrawerLayer);
    }

    private void initClickView() {

        analysisBtn=findViewById(R.id.analysis);
        analysisLayout=findViewById(R.id.layout_analysis);
        analysisBtn.setOnClickListener(mainClickListener);
        btn_all=analysisLayout.findViewById(R.id.button_all);
        btn_select=analysisLayout.findViewById(R.id.button_select);
        btn_analysis=analysisLayout.findViewById(R.id.button_analysis);
        btn_close=analysisLayout.findViewById(R.id.button_close);
        btn_all.setOnClickListener(mainClickListener);
        btn_select.setOnClickListener(mainClickListener);
        btn_analysis.setOnClickListener(mainClickListener);
        btn_close.setOnClickListener(mainClickListener);
        sp_theme=analysisLayout.findViewById(R.id.Spinner_layer_select);
        LV_result=analysisLayout.findViewById(R.id.LV_result);


    }

    private void initMapView() {
        baseMapManager =new BaseMapManager(mDataSource,this);
    }

    private void loadMapLayer() {
        List<Layer> layers=layerList;
        for (Layer layer : layers) {
            mapView_main.addLayer(layer);
        }
    }


    private void readConfigFile() {
        String data=null;
        try {
            data = SDCardReadUtil.ReadPrivilegeFile(Constants.OFFLINE_PRIVILEGE);
            if (data != null) {
                OfflineDataTask offlineDataTask = new OfflineDataTask(this);
                offlineDataTask.execute(data);
                offlineDataTask.setOnDataSourceListener(new OfflineDataTask.OnDataSourceListener() {
                    @Override
                    public void OnDataSourceResult(DataSource dataSource, boolean isSuccess) {
                        if(isSuccess){
                            if(isSuccess){
                                if(dataSource!=null){
                                    ((App) MainMapActivity.this.getApplicationContext()).setDataSource(dataSource);
                                    mDataSource=dataSource;
                                    List<ThemeInfo> mThemeInfo =dataSource.getTheme();
                                    if(mThemeInfo !=null&& mThemeInfo.size()>0){
                                        ((App) MainMapActivity.this.getApplicationContext()).getThemeInstance().addAll(mThemeInfo);
                                    }
                                    if(dataSource!=null){
                                        MainMapActivity.this.loadData();
                                    }
                                }
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取不为空的layer名字数组
     *
     * @return String[]  不为空的layer的name数组
     */
    private String[] getLayerList(){

        String[] stringResult=new String[layerList.size()];
        for (int i = 0; i < layerList.size(); i++) {
            stringResult[i]=layerList.get(i).getName();
        }
        return stringResult;
    }

    /**
     * 页面按钮点击
     */
    private View.OnClickListener mainClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                /*
                 * 悬浮唤出按钮
                 */
                case R.id.analysis:
                    if (analysisLayout.getVisibility()== View.VISIBLE) {
                        drawerLayer.removeAll();
                        resultDrawerLayer.removeAll();
                        btn_all.setBackground(ContextCompat.getDrawable(MainMapActivity.this, R.mipmap.select));
                        btn_select.setBackground(ContextCompat.getDrawable(MainMapActivity.this, R.mipmap.noselect));
                        analysisLayout.setVisibility(View.GONE);
                    }else {
                        analysisLayout.setVisibility(View.VISIBLE);
                        String[] mItems = getLayerList();
                        if (layerList!=null&&!layerList.isEmpty()) {
                            layer=layerList.get(0);
                        }
                        ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(MainMapActivity.this,android.R.layout.simple_spinner_item, mItems);

                        sp_theme.setAdapter(spinnerAdapter);
                        sp_theme.setOnItemSelectedListener(SPOnItemSelectListener);
                        LV_result.setAdapter(null);
                        btn_select.setText("选择");
                        selectStatus=0;
                    }
                    break;
                /*
                 * 全选按钮
                 */
                case R.id.button_all:
                    drawerLayer.removeAll();
                    LV_result.setAdapter(null);
                    resultDrawerLayer.removeAll();
                    btn_all.setBackground(ContextCompat.getDrawable(MainMapActivity.this, R.mipmap.select));
                    btn_select.setBackground(ContextCompat.getDrawable(MainMapActivity.this, R.mipmap.noselect));
                    btn_select.setText("选择");
                    selectStatus=0;
                    isALL=true;
                    break;
                /*
                 * 选择按钮
                 */
                case R.id.button_select:
                    LV_result.setAdapter(null);

                    if (selectStatus==0) {
                        btn_select.setBackground(ContextCompat.getDrawable(MainMapActivity.this, R.mipmap.select));
                        btn_all.setBackground(ContextCompat.getDrawable(MainMapActivity.this, R.mipmap.noselect));
                        btn_select.setText("请选择区域");
                        selectStatus = 1;
                        isALL=false;
                        measureManager =new MeasureManager(MainMapActivity.this,drawerLayer);
                    }else if(selectStatus==2){
                        btn_select.setText("区域已选择完毕");
                        selectStatus=3;
                    }
                    break;
                /*
                 * 分析按钮
                 */
                case R.id.button_analysis:
                    if (selectStatus==0||selectStatus==1) {
                        break;
                    }

                    QueryParameters queryParams=new QueryParameters();
                    if (isALL) {
                        queryParams.setGeometry(mapView_main.getExtent());
                    }else {
                        btn_select.setText("区域已选择完毕");
                        selectStatus=3;
                        Polygon queryPolygon=measureManager.getpolygon();
                        queryParams.setGeometry(queryPolygon);
                    }
                    landAnalysisResultInfos=new ArrayList<>();
                    queryParams.setOutFields(new String[]{"*"});
                    List<LandAnalysisResultInfo> analysisResultInfos=new ArrayList<>();
                    if(layer!=null) {
                        Future<FeatureResult> resultFuture=null;
                        FeatureResult featureResult=null;
                        FeatureLayer featureLayer=(FeatureLayer)layer;
                        resultFuture=featureLayer.getFeatureTable().queryFeatures(queryParams, new CallbackListener<FeatureResult>() {
                            @Override
                            public void onCallback(FeatureResult objects) {
                            }

                            @Override
                            public void onError(Throwable throwable) {
                            }
                        });
                        try {
                            featureResult=resultFuture.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        if (featureResult!=null&&featureResult.featureCount()>0) {
                            Iterator it = featureResult.iterator();

                            while (it.hasNext()){
                                GeodatabaseFeature geodatabaseFeature= (GeodatabaseFeature) it.next();
                                Geometry geometry=geodatabaseFeature.getGeometry();
                                Map<String, Object> featureMap=geodatabaseFeature.getAttributes();

                                if(geometry!=null){
                                    if (featureMap!=null&&featureMap.size()>0) {

                                        LandAnalysisResultInfo landAnalysisResultInfo=new LandAnalysisResultInfo();
                                        landAnalysisResultInfo.setGeometry(GeometryEngine.intersect(geometry,measureManager.getpolygon(),mapView_main.getSpatialReference()));

                                        landAnalysisResultInfo.setParentGeometry(geometry);
                                        if (featureMap.containsKey("YDMC")) {
                                            landAnalysisResultInfo.setYDMC(featureMap.get("YDMC").toString());
                                        }
                                        if (featureMap.containsKey("用地名称")) {
                                            landAnalysisResultInfo.setYDMC(featureMap.get("用地名称").toString());
                                        }
                                        if (featureMap.containsKey("YDMJ")) {
                                            if (featureMap.get("YDMJ")!=null) {
                                                landAnalysisResultInfo.setYDMJ(Double.valueOf(featureMap.get("YDMJ").toString()));
                                            }

                                        }
                                        if (featureMap.containsKey("用地面积")) {
                                            if (featureMap.get("用地面积")!=null) {
                                                landAnalysisResultInfo.setYDMJ(Double.valueOf(featureMap.get("用地面积").toString()));
                                            }
                                        }
                                        if (landAnalysisResultInfo.getYDMC()!=null&&landAnalysisResultInfo.getYDMJ()!=null) {
                                            landAnalysisResultInfos.add(landAnalysisResultInfo);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (landAnalysisResultInfos!=null&&!landAnalysisResultInfos.isEmpty()) {
                        analysisResultInfos= AnalysisUtil.getLandPercent(landAnalysisResultInfos);
                    }
                    if (!analysisResultInfos.isEmpty()) {
                        LV_result.setAdapter(new AnalysisResultListAdapter(MainMapActivity.this,analysisResultInfos));
                        LV_result.setOnItemClickListener(resultListOnItemClickListener);

                    }else {
                        LV_result.setAdapter(null);
                        Toast.makeText(MainMapActivity.this,getString(R.string.noresult), Toast.LENGTH_LONG).show();

                    }
                    break;
                /*
                 * 关闭按钮
                 */
                case R.id.button_close:
                    drawerLayer.removeAll();
                    resultDrawerLayer.removeAll();
                    btn_all.setBackground(ContextCompat.getDrawable(MainMapActivity.this, R.mipmap.select));
                    btn_select.setBackground(ContextCompat.getDrawable(MainMapActivity.this, R.mipmap.noselect));
                    analysisLayout.setVisibility(View.GONE);
                default:
                    break;
            }
        }
    };
    /**
     * 结果列表点击效果
     */
    private AdapterView.OnItemClickListener resultListOnItemClickListener =new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            resultDrawerLayer.removeAll();
            resultDrawerLayer.addGraphic(new Graphic(landAnalysisResultInfos.get(position).getGeometry(), MapViewTapTool.getLabelSymbol(MainMapActivity.this,Geometry.Type.POLYGON, Color.BLUE)));
            resultDrawerLayer.addGraphic(new Graphic(landAnalysisResultInfos.get(position).getParentGeometry(),MapViewTapTool.getLabelSymbol(MainMapActivity.this,Geometry.Type.POLYLINE, Color.BLACK)));
            Envelope tEnvelope = new Envelope();
            landAnalysisResultInfos.get(position).getParentGeometry().queryEnvelope(tEnvelope);
            Point tPoint = tEnvelope.getCenter();
            mapView_main.centerAt(tPoint,true);
        }
    };
    /**
     * Spinner适配器
     */
    private AdapterView.OnItemSelectedListener SPOnItemSelectListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //获取点击的layer并准备作为查询对象
            if (layerList!=null&&layerList.size()>position) {
                layer=layerList.get(position);
                for (Layer layer1 : layerList) {
                    layer1.setVisible(false);
                }
                layerList.get(position).setVisible(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (layerList!=null&&!layerList.isEmpty()) {
                layer=layerList.get(0);
                layerList.get(0).setVisible(true);
            }

        }
    };
}
