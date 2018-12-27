package cn.lhlyblog.map_demo.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.lhlyblog.map_demo.R;
import cn.lhlyblog.map_demo.adapter.PointsAdapter;
import cn.lhlyblog.map_demo.util.Constants;

public class PointsActivity extends AppCompatActivity {

    private Constants constants;
    private ListView listView;
    private List<String> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);
        PointsAdapter adapter = new PointsAdapter(mList, this);
        listView = (ListView) findViewById(R.id.points_list);
        constants = new Constants();

        initList();
        listView.setAdapter(adapter);
    }

    private void initList() {
        for (int i = 0; i < constants.POINTS.size(); i++) {
            mList.add(constants.POINTS.get(i));
        }
    }
}
