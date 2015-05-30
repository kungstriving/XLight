package com.everhope.elighte.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.adapters.LeftMenuAdapter;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.fragments.AddSceneFragment;
import com.everhope.elighte.fragments.FragmentTabListener;
import com.everhope.elighte.fragments.HomeFragment;
import com.everhope.elighte.fragments.LightFragment;
import com.everhope.elighte.R;
import com.everhope.elighte.fragments.RemoterFragment;
import com.everhope.elighte.fragments.SettingsFragment;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.fragments.SwitchFragment;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.GetAllStationsMsgResponse;
import com.everhope.elighte.models.LightRemoter;
import com.everhope.elighte.models.LightScene;
import com.everhope.elighte.models.Remoter;
import com.everhope.elighte.models.SubGroup;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.StationObject;
import com.everhope.elighte.receivers.SyncStationAlarmReceiver;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 主页面
 * 该页面负责导航到各个子页面
 */
public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity@Light";

    private static final int MSG_ADD_NEWGROUP = 0;
    private static final int MSG_ADD_SCENE = 1;

    private ProgressBar progressBar;
    private ListView leftListView;
    private DrawerLayout drawerLayout;
    private String[] leftItems;
    private int currentSelectFrag;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment currentFragment;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isVeryFirstLoad()) {
            //如果是初次运行，需要加入默认数据库表内容
            AppUtils.initDB();
            setVeryFirstLoad();
        }
        Log.i(TAG, "启动运行");
        setContentView(R.layout.activity_main);

        //设置抽屉
        setLeftDrawer(savedInstanceState);
        //与网关同步数据
        syncDataWithGate();
        //设置handler
        setMainHandler();
    }

    private void setMainHandler() {
        this.mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_ADD_NEWGROUP:
                        //
                        if (currentFragment != null) {
                            LightFragment lightFragment = (LightFragment)currentFragment;
                            String newGroupName = (String)msg.obj;
                            lightFragment.addNewGroup(newGroupName);
                        }
                        break;
                    case MSG_ADD_SCENE:
                        if (currentFragment != null) {
                            HomeFragment homeFragment = (HomeFragment)currentFragment;
                            String newSceneName = (String)msg.obj;
                            homeFragment.addNewScene(newSceneName);
                            selectItem(2);
                            selectItem(0);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 与网关同步数据
     */
    private void syncDataWithGate() {

        //同步一次 站点信息
        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        dataAgent.getAllLights(MainActivity.this, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                    //读到了回应消息
                    byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                    short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                    //解析回应消息
                    GetAllStationsMsgResponse getAllStationsMsgResponse = null;
                    try {
                        getAllStationsMsgResponse = MessageUtils.decomposeGetAllStationsMsgResponse(msgBytes, msgBytes.length, idShould);
                    } catch (Exception e) {
                        Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                        return;
                    }
                    Log.i(TAG, "获取所有站点列表回应消息 " + getAllStationsMsgResponse.toString());
                    //取出当前数据库表中的所有站点进行比对
                    //新增 或 删除
                    List<Light> myLights = Light.getAll();
                    List<Remoter> myRemoters = Remoter.getAll();

                    StationObject[] gateAllStations = getAllStationsMsgResponse.getStationObjects();

                    if (gateAllStations == null) {
                        //网关内容为空 则全部清除
                        gateAllStations = new StationObject[0];
//                        return;
                    }
                    //判断新增或修改

                    for (StationObject stationObject : gateAllStations) {
                        //遍历当前站点列表中是否包含有该站点
                        switch (stationObject.getStationTypes()) {
                            case LIGHT:
                                syncLight(myLights, stationObject);
                                break;
                            case REMOTER:
                                syncRemoter(myRemoters, stationObject);
                                break;
                            case SWITCH:
                                break;
                            default:
                                break;
                        }

                    }

                    //判断是否删除
                    checkDeleteLight(myLights, gateAllStations);
                    checkDeleteRemoter(myRemoters, gateAllStations);

                    //启动周期同步
                    startAlarmSync();
                } else {
                    //出错
                    Log.e(TAG, String.format("获取所有站点列表出错 Code=[%s]", resultCode + ""));
                }
            }
        });
    }

    private void startAlarmSync() {
        //启动周期性同步数据
        Intent intent = new Intent(this, SyncStationAlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, SyncStationAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
        int intervalMillis = Constants.SYSTEM_SETTINGS.SYNC_INTERVAL;
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+100, intervalMillis, pIntent);

        Log.i(TAG, "启动周期同步服务");
    }
    private void checkDeleteLight(List<Light> myLights, StationObject[] allStations) {
        for(Light light : myLights) {
            boolean delete = true;
            for(StationObject stationObject : allStations) {
                if (light.lightMac.equals(stationObject.getMac())) {
                    delete = false;
                    break;
                }
            }

            if (delete) {
                //如果需要删除该灯
                List<LightScene> lightScenes = light.lightScenes();
                for(LightScene lightScene : lightScenes) {
                    lightScene.delete();
                }
                List<LightGroup> lightGroups = light.lightGroups();
                for(LightGroup lightGroup : lightGroups) {
                    lightGroup.delete();
                }
                List<LightRemoter> lightRemoters = light.lightRemoters();
                for(LightRemoter lightRemoter : lightRemoters) {
                    lightRemoter.delete();
                }

                light.delete();
            }
        }
    }

    private void checkDeleteRemoter(List<Remoter> myRemoters, StationObject[] allStations) {
        for(Remoter remoter : myRemoters) {
            boolean delete = true;
            for(StationObject stationObject : allStations) {
                if (remoter.remoterMac.equals(stationObject.getMac())) {
                    delete = false;
                    break;
                }
            }

            if (delete) {
                //如果需要删除该灯
                for (int i  = 1 ; i <= 4; i++) {
                    List<LightRemoter> lightRemoters = remoter.groupLightRemoters(i + "");
                    for(LightRemoter tmp : lightRemoters) {
                        tmp.delete();
                    }
                }

                remoter.delete();
            }
        }
    }

    private void syncLight(List<Light> allLights, StationObject light) {
        boolean add = true;
        for (Light dbLight : allLights) {

            if (dbLight.lightMac.equals(light.getMac())) {
                //已存在 不用新增 但ID可能发生变化，需要进行更新
                if (!dbLight.lightID.equals(light.getId() + "") ) {
                    dbLight.lightID = light.getId() + "";
                    dbLight.save();
                }
                add = false;
                break;
            }
        }

        if (add) {
            //新加灯
            Light newLight = new Light();
            newLight.lightID = light.getId() + "";
            newLight.name = "[" + light.getMac() + "]";
            newLight.lightMac = light.getMac();
            //新增的灯加入到未分组 组中
            SubGroup ungroup = SubGroup.load(SubGroup.class, 1);
            LightGroup lightGroup = new LightGroup();
            lightGroup.light = newLight;
            lightGroup.subgroup = ungroup;
            newLight.save();
            lightGroup.save();
        }
    }

    private void syncRemoter(List<Remoter> allRemoters, StationObject remoter) {
        boolean add = true;
        for (Remoter dbRemoter : allRemoters) {

            if (dbRemoter.remoterMac.equals(remoter.getMac())) {
                //已存在 不用新增 但ID可能发生变化，需要进行更新
                if (!dbRemoter.remoterID.equals(remoter.getId() + "") ) {
                    dbRemoter.remoterID = remoter.getId() + "";
                    dbRemoter.save();
                }
                add = false;
                break;
            }
        }

        if (add) {
            //新加遥控器
            Remoter newRemoter = new Remoter();
            newRemoter.remoterID = remoter.getId() + "";
            newRemoter.name = "[" + remoter.getMac() + "]";
            newRemoter.remoterMac = remoter.getMac();
            newRemoter.save();
        }
    }

    private void setLeftDrawer(Bundle savedInstanceState) {
        leftItems = getResources().getStringArray(R.array.left_nav_items);
        drawerLayout = (DrawerLayout)findViewById(R.id.main_drawer_layout);
        leftListView = (ListView)findViewById(R.id.main_left_drawer);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ArrayList<String> arrayList = new ArrayList<>();
        for(int i = 0; i < leftItems.length; i++) {
            arrayList.add(leftItems[i]);
        }
        LeftMenuAdapter leftMenuAdapter = new LeftMenuAdapter(MainActivity.this, arrayList);
//        leftListView.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, leftItems));
        leftListView.setAdapter(leftMenuAdapter);
        leftListView.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mTitle = mDrawerTitle = getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(mDrawerToggle);

        if(savedInstanceState == null) {
            selectItem(0);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    public void selectItem(int position) {

        FragmentManager fragmentManager = getFragmentManager();
        // Create a new fragment and specify the planet to show based on position
        switch (position) {
            case 0:
                //打开首页
//                setupHomeTabs();
                this.currentFragment = new HomeFragment();
//                Bundle args = new Bundle();
//                args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//                fragment.setArguments(args);
                break;
            case 1:
                //打开灯
                this.currentFragment = LightFragment.newInstance("");
                break;
            case 2:
                //开关配置
                this.currentFragment = SwitchFragment.newInstance("开关","test");
                break;
            case 3:
                //遥控配置
                this.currentFragment = RemoterFragment.newInstance();
                break;
            case 4:
                //打开设置
                this.currentFragment = SettingsFragment.newInstance("设置");
                break;
            default:
                //打开首页
                this.currentFragment = new HomeFragment();
                break;
        }


        // Insert the fragment by replacing any existing fragment

        if (this.currentFragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, this.currentFragment)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer
            leftListView.setItemChecked(position, true);
            setTitle(leftItems[position]);
            this.currentSelectFrag = position;
        }

//        invalidateOptionsMenu();        //刷新menu
        drawerLayout.closeDrawer(leftListView);
    }

    private void setupHomeTabs() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.setDisplayShowTitleEnabled(true);

        ActionBar.Tab sceneTab = actionBar.newTab()
                .setText("场景")
                .setTabListener(new FragmentTabListener<HomeFragment>(
                        R.id.content_frame,
                        this,
                        "scene_tab",
                        HomeFragment.class));
        actionBar.addTab(sceneTab);
        actionBar.selectTab(sceneTab);

        ActionBar.Tab commonGroupsTab = actionBar.newTab()
                .setText("常用组")
                .setTabListener(new FragmentTabListener<LightFragment>(
                        R.id.content_frame,
                        this,
                        "groups_tab",
                        LightFragment.class));
        actionBar.addTab(commonGroupsTab);

        ActionBar.Tab commonLightsTab = actionBar.newTab()
                .setText("常用灯")
                .setTabListener(new FragmentTabListener<AddSceneFragment>(
                        R.id.content_frame,
                        this,
                        "lights_tab",
                        AddSceneFragment.class));
        actionBar.addTab(commonLightsTab);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }


    /**
     * 设置启动过
     */
    private void setVeryFirstLoad() {
        Log.i(TAG, "第一次启动，设置标志位");

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.SYSTEM_SETTINGS.IS_LOADED_YET, true);
        editor.commit();
    }
    /**
     * 检查是否第一次运行。
     * 即在本机上没有启动过该APP
     * @return true 已经启动过；false 还没有启动过
     */
    private boolean isVeryFirstLoad() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return !pref.getBoolean(Constants.SYSTEM_SETTINGS.IS_LOADED_YET, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止服务
        Log.i(TAG, "停止周期同步服务");

        Intent intent = new Intent(this, SyncStationAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, SyncStationAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(leftListView);
        int menuLength = menu.size();
        if (drawerOpen) {
            for (int i = 0; i < menuLength; i++) {
                menu.getItem(i).setVisible(false);
            }
        } else {
            //关闭的时候
            switch (this.currentSelectFrag) {
                case 0:
                    //场景
                    menu.findItem(R.id.action_frag_main_add).setVisible(true);
                    menu.findItem(R.id.action_frag_main_edit).setVisible(false);
                    menu.findItem(R.id.action_frag_main_rmv).setVisible(true);
                    break;
                case 1:
                    //灯列表
                    menu.findItem(R.id.action_frag_main_add).setVisible(true);
                    menu.findItem(R.id.action_frag_main_edit).setVisible(false);
                    menu.findItem(R.id.action_frag_main_rmv).setVisible(true);
                    break;
                default:
                    menu.findItem(R.id.action_frag_main_add).setVisible(false);
                    menu.findItem(R.id.action_frag_main_edit).setVisible(false);
                    menu.findItem(R.id.action_frag_main_rmv).setVisible(false);
                    break;
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = null;

        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_frag_main_add:
                switch (this.currentSelectFrag) {
                    case 0:
                        //添加场景
                        addScene();
                        break;
                    case 1:
                        //灯列表
//                        Toast.makeText(MainActivity.this, "添加分组",Toast.LENGTH_LONG).show();
                        addLightGroup();
                        break;
                    default:
                        break;
                }

                break;
            case R.id.action_frag_main_rmv:
                switch (this.currentSelectFrag) {
                    case 0:
                        //删除场景
                        ((HomeFragment)this.currentFragment).deleteScene();
//                        selectItem(2);
//                        selectItem(0);
                        break;
                    case 1:
                        //删除分组
                        ((LightFragment)this.currentFragment).deleteGroup();
                        break;
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }

        return true;
    }

    private void addScene() {
        //弹出对话框进行输入
        EditText editText = new EditText(MainActivity.this);

        InputSceneNameListener inputGroupNameListener = new InputSceneNameListener(editText);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("创建场景")
                .setIcon(android.R.drawable.ic_menu_edit)
                .setView(editText)
                .setPositiveButton("确定", inputGroupNameListener)
                .setNegativeButton("取消",null);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        editText.setFocusable(true);
        editText.requestFocus();
    }

    private void addLightGroup() {
        //弹出对话框进行输入
        EditText editText = new EditText(MainActivity.this);

        InputGroupNameListener inputGroupNameListener = new InputGroupNameListener(editText);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("创建组")
                .setIcon(android.R.drawable.ic_menu_edit)
                .setView(editText)
                .setPositiveButton("确定", inputGroupNameListener)
                .setNegativeButton("取消",null);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        editText.setFocusable(true);
        editText.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        selectItem(this.currentSelectFrag);
    }

    class InputSceneNameListener implements DialogInterface.OnClickListener {
        private EditText sceneNameET;
        public InputSceneNameListener(EditText editText) {
            this.sceneNameET = editText;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String newSceneName = this.sceneNameET.getText().toString();

            if (StringUtils.isEmpty(newSceneName)) {
                return;
            }
            Message message = new Message();
            message.what = MSG_ADD_SCENE;
            message.obj = newSceneName;
            mainHandler.sendMessage(message);
        }
    }

    class InputGroupNameListener implements DialogInterface.OnClickListener {

        private EditText groupNameET;
        public InputGroupNameListener(EditText editText) {
            this.groupNameET = editText;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String newGroupName = this.groupNameET.getText().toString();

            Message message = new Message();
            message.what = MSG_ADD_NEWGROUP;
            message.obj = newGroupName;
            mainHandler.sendMessage(message);
        }
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
