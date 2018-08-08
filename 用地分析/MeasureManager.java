package tools;

import android.graphics.Color;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.Symbol;
import com.example.shuiz.landofflineanalysis.MainMapActivity;


/**
 *
 * @author shuiz
 * @date 2018/7/13
 */

public class MeasureManager {

    private MainMapActivity mActivity;
    private GraphicsLayer drawLayer;
    private Symbol symbol,pointSymbol,lastSymbol;
    private Geometry.Type geometryType;
    private MultiPoint multiPoint;
    private Polygon polygon;
    private Graphic currentGraphic,pointGraphic,lastGraphic;
    private Point startPoint;
    private int currentGraphicIndex,pointGraphicIndex,lastGraphicIndex;

    public MeasureManager(MainMapActivity mActivity, GraphicsLayer drawLayer) {
        this.mActivity = mActivity;
        this.drawLayer = drawLayer;
        initData();
        initPolyGon();
    }
    public Polygon getpolygon(){
        return this.polygon;
    }

    private void initData() {
        startPoint=null;
        multiPoint = new MultiPoint();
        geometryType = Geometry.Type.POINT;
        pointSymbol = MapViewTapTool.getSquareSymbol();
        lastSymbol=MapViewTapTool.getCircleSymbol();
        geometryType= Geometry.Type.POLYGON;
        symbol =MapViewTapTool.getLabelSymbol(mActivity,geometryType,Color.RED);

        if(drawLayer!=null){
            drawLayer.removeAll();
        }

    }

    /**
     * 地图点击分发事件
     * @param x 坐标 x
     * @param y 坐标 y
     * @param mOneMapView mapview
     */
    public void onSingleTap(float x, float y, MapView mOneMapView) {
        Point currentPoint = mOneMapView.toMapPoint(x, y);


        if (geometryType != null) {
            try {
                if  (geometryType.equals(Geometry.Type.POLYGON)) {
                    polygonState(currentPoint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化面的测量
     */
    private void initPolyGon() {
        if (geometryType != Geometry.Type.POLYGON) {
            startPoint = null;
            geometryType = Geometry.Type.POLYGON;
            symbol = MapViewTapTool.getLabelSymbol(mActivity, geometryType, Color.RED);
            if (drawLayer != null) {
                drawLayer.removeAll();
            }
        }
    }


    /**
     * 绘制面
     * */
    private void polygonState(Point currentPoint)throws Exception{
        if(startPoint!=null&&!startPoint.isEmpty()){
            polygon.lineTo(currentPoint);
            currentGraphic = new Graphic(polygon, symbol);
            drawLayer.updateGraphic(currentGraphicIndex,currentGraphic);

            pointGraphic=new Graphic(multiPoint,pointSymbol);
            drawLayer.updateGraphic(pointGraphicIndex,pointGraphic);
            multiPoint.add(currentPoint);
            if(lastGraphicIndex!=-1){
                drawLayer.removeGraphic(lastGraphicIndex);
            }
            lastGraphic=new Graphic(currentPoint,lastSymbol);

            lastGraphicIndex=drawLayer.addGraphic(lastGraphic);

        }else{
            polygon=new Polygon();
            multiPoint=new MultiPoint();
            polygon.startPath(currentPoint);
            startPoint=currentPoint;
            currentGraphic=new Graphic(startPoint,symbol);
            currentGraphicIndex=drawLayer.addGraphic(currentGraphic);
            pointGraphic=new Graphic(startPoint,lastSymbol);
            pointGraphicIndex=drawLayer.addGraphic(pointGraphic);
            multiPoint.add(currentPoint);
        }
        //确定当前已经为面
        if (multiPoint.getPointCount()>2) {
            mActivity.selectStatus=2;
            mActivity.btn_select.setText("点击确定该区域");
        }
    }
}
