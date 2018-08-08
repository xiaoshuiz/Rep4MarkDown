package model;

import com.esri.core.geometry.Geometry;

/**
 *
 * @author shuiz
 * @date 2018/7/16
 *
 *  YDMC:用地名称
 *  YDMJ：用地面积
 *  geometry:框选中的图形
 *  parentGeometry：框选图形的父图形
 *  percent：百分比数值
 */

public class LandAnalysisResultInfo {
    private String YDMC;
    private Double YDMJ;
    private Geometry geometry;
    private Geometry parentGeometry;
    private String percent;

    public String getYDMC() {
        return YDMC;
    }

    public void setYDMC(String YDMC) {
        this.YDMC = YDMC;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public Double getYDMJ() {
        return YDMJ;
    }

    public void setYDMJ(Double YDMJ) {
        this.YDMJ = YDMJ;
    }

    public Geometry getParentGeometry() {
        return parentGeometry;
    }

    public void setParentGeometry(Geometry parentGeometry) {
        this.parentGeometry = parentGeometry;
    }
}
