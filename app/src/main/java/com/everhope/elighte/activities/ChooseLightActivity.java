package com.everhope.elighte.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.SubGroup;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 选择灯活动
 */
public class ChooseLightActivity extends ActionBarActivity {

    private static final String TAG = "ChooseLightsActivity@Light";

    private ListView lightListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_light);

        //获取所传入的分组
        Intent intent = getIntent();
        // 1是默认是所有灯分组
//        int groupID = intent.getIntExtra("subgroup_id", 1);
        long groupID = intent.getLongExtra("subgroup_id", 1L);
        long [] filterIDs = intent.getLongArrayExtra("filter_rm_ids");
        SubGroup subGroup = SubGroup.load(SubGroup.class, groupID);
        List<Light> lights = new ArrayList<>();
        List<LightGroup> lightGroupList = subGroup.lightGroups();
        for(LightGroup lightGroup : lightGroupList) {
            long lightID = Long.parseLong(lightGroup.light.lightID);
            if (!ArrayUtils.contains(filterIDs, lightID)) {
                lights.add(lightGroup.light);
            }
        }


        lightListView = (ListView)findViewById(R.id.choose_lights_lv);
        lightListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        final ChooseLightsListViewAdapter chooseLightsListViewAdapter = new ChooseLightsListViewAdapter(ChooseLightActivity.this, lights);
        lightListView.setAdapter(chooseLightsListViewAdapter);

//        lightListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                boolean selected = lightListView.isItemChecked(position);
//                lightListView.setItemChecked(position, selected);
//                if (selected) {
//                    view.setBackgroundColor(getResources().getColor(R.color.goldenrod));
//                } else {
//                    view.setBackgroundColor(getResources().getColor(R.color.whitesmoke));
//                }
//            }
//        });

        setTitle("选择灯");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    class ChooseLightsListViewAdapter extends ArrayAdapter<Light> {
        public ChooseLightsListViewAdapter(Context context, List<Light> lights) {
            super(context, 0, lights);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.choose_light_item, parent, false);
                Light light = getItem(position);
                TextView textView = (TextView)convertView.findViewById(R.id.choose_light_name_tv);
                textView.setText(light.name);
                if (light.lostConnection) {
                    ImageView imageView = (ImageView)convertView.findViewById(R.id.choose_light_icon);
                    imageView.setImageResource(R.drawable.offline);
                }

                CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.select_light_cb);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        lightListView.setItemChecked(position,isChecked);
                    }
                });
            }
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_light, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                Intent data = new Intent();
                //获取当前选中的灯ID
                ListView listView = (ListView)findViewById(R.id.choose_lights_lv);
                ChooseLightsListViewAdapter lightAdapter = (ChooseLightsListViewAdapter)listView.getAdapter();
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                long[] selectedIDs = new long[checked.size()];
                int arrCount = 0;
                int length = listView.getCount();
                for(int i = 0;i< length; i++) {
                    if (checked.get(i)) {
                        //该项被选中
                        Light light = lightAdapter.getItem(i);
                        selectedIDs[arrCount] = light.getId();
                        arrCount++;
                    }
                }
                data.putExtra("lights_selected_ids", selectedIDs);
                setResult(Constants.COMMON.RESULT_CODE_OK, data);
                finish();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
