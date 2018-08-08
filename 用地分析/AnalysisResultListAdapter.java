package adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.shuiz.landofflineanalysis.R;

import java.util.List;

import model.LandAnalysisResultInfo;


/**
 *
 * @author shuiz
 * @date 2018/7/16
 */

public class AnalysisResultListAdapter extends BaseAdapter{
    private LayoutInflater inflater;
    List<LandAnalysisResultInfo> mylist;


    public AnalysisResultListAdapter(Context context,List<LandAnalysisResultInfo> mylist) {
        inflater = LayoutInflater.from(context);
        this.mylist=mylist;
    }

    @Override
    public int getCount() {
        return mylist.size();
    }



    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView YDMC, YDMJ, YDPercent;
        int size=mylist.size();

        convertView = inflater.inflate(R.layout.item, null);
        YDMC = (TextView) convertView.findViewById(R.id.TV_YDMC);
        YDMJ = (TextView) convertView.findViewById(R.id.TV_YDMJ);
        YDPercent = (TextView) convertView.findViewById(R.id.TV_percent);
        convertView.setTag(YDMC);

        YDMC.setText(mylist.get(position).getYDMC());
        YDMC.setTextColor(Color.GRAY);
        YDMJ.setText(String.valueOf(mylist.get(position).getYDMJ()));
        YDMJ.setTextColor(Color.GRAY);
        YDPercent.setText(mylist.get(position).getPercent() + "%");
        YDPercent.setTextColor(Color.GRAY);
        return convertView;
    }

}
