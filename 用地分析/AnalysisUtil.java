package tools;

import java.util.List;

import model.LandAnalysisResultInfo;

/**
 *
 * @author shuiz
 * @date 2018/7/16
 *
 * 分析工具
 */

public class AnalysisUtil {
    /**
     *通过在地图上的Area大小计算百分比与实际面积
     *
     * @param landResourceInfos 分析结果的数组 成员： {@link LandAnalysisResultInfo}
     * @return 一个结果的数组 变更了参数数组中成员的属性
     */
    public static List<LandAnalysisResultInfo> getLandPercent(List<LandAnalysisResultInfo> landResourceInfos){
        /*计算实际面积 */
        double MUM=0;
        for (LandAnalysisResultInfo landResourceInfo : landResourceInfos) {
            if (landResourceInfo.getYDMJ()==null) {
                landResourceInfos.remove(landResourceInfo);
                continue;
            }
            Double visualArea=landResourceInfo.getGeometry().calculateArea2D();
            Double visualParentArea=landResourceInfo.getParentGeometry().calculateArea2D();
            Double realArea=landResourceInfo.getYDMJ()/visualParentArea*visualArea;
            landResourceInfo.setYDMJ(realArea);

//            累计求和
            MUM=MUM+landResourceInfo.getYDMJ();
        }



        /*分别计算百分比*/
        for (LandAnalysisResultInfo landResourceInfo : landResourceInfos) {
            landResourceInfo.setPercent(String.valueOf
                    (landResourceInfo.getYDMJ()/MUM*100).length()>7?
                            String.valueOf(landResourceInfo.getYDMJ()/MUM*100).substring(0,6)
                            :String.valueOf(landResourceInfo.getYDMJ()/MUM*100));
        }
        return landResourceInfos;
    }
}
