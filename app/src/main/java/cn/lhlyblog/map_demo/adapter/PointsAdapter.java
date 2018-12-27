package cn.lhlyblog.map_demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.lhlyblog.map_demo.R;

public class PointsAdapter extends BaseAdapter {

    private Context context;
    private List<String> mData;
    private LayoutInflater layoutInflater;

    public PointsAdapter(List<String> data, Context context) {
        this.context = context;
        mData = data;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = layoutInflater.inflate(R.layout.points_item, null);
            viewHolder.img = (ImageView) view.findViewById(R.id.point_image);
            viewHolder.title = (TextView) view.findViewById(R.id.point_textView);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.img.setBackgroundResource(R.mipmap.ic_search);
        viewHolder.title.setText(mData.get(i));
        return view;
    }

    public class ViewHolder {
        public ImageView img;
        public TextView title;
    }
}
