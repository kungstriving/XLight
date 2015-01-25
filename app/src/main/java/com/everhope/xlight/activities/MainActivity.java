package com.everhope.xlight.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.everhope.xlight.fragments.HomeFragment;
import com.everhope.xlight.fragments.LightFragment;
import com.everhope.xlight.R;
import com.everhope.xlight.fragments.SceneFragment;
import com.everhope.xlight.fragments.SettingsFragment;
import com.everhope.xlight.constants.Constants;

/**
 * 主页面
 * 该页面负责导航到各个子页面
 */
public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    private ProgressBar progressBar;
    private ListView leftListView;
    private DrawerLayout drawerLayout;
    private String[] leftItems;
    private int currentSelectFrag;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();

        //检查是否第一次运行
        /*
        if (isVeryFirstLoad()) {
            Log.i(TAG, "第一次启动运行");
            //是第一次运行
            //网络侦测网关地址
            setVeryFirstLoad();

        }*/

        //连接到网关
        //connectToGate();

        //getWindow().setBackgroundDrawableResource(R.drawable.splash);

        Log.i(TAG, "启动运行");
        setContentView(R.layout.activity_main);

        //动态加载splash
//        final FrameLayout mainLayout = (FrameLayout)findViewById(R.id.content_frame);
//        final ImageView imageView = new ImageView(MainActivity.this);
//        imageView.setImageResource(R.drawable.splash);
//        FrameLayout.LayoutParams layoutParams =
//                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//
//        mainLayout.addView(imageView, layoutParams);

        //显示loading

//        progressBar = (ProgressBar)findViewById(R.id.progressBar);
//        progressBar.bringToFront();
//        progressBar.setVisibility(ProgressBar.VISIBLE);

        //模拟5秒钟后进入新画面
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                //删除splash view 停止loading
////                progressBar.setVisibility(ProgressBar.INVISIBLE);
//                mainLayout.removeView(imageView);
//                //显示actionbar
//                getSupportActionBar().show();
//                handler.removeCallbacks(this);
//            }
//        }, 5000);

        //设置抽屉
        setLeftDrawer(savedInstanceState);
    }

    private void setLeftDrawer(Bundle savedInstanceState) {
        leftItems = getResources().getStringArray(R.array.left_nav_items);
        drawerLayout = (DrawerLayout)findViewById(R.id.main_drawer_layout);
        leftListView = (ListView)findViewById(R.id.main_left_drawer);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        leftListView.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, leftItems));
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
    private void selectItem(int position) {

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = null;
        // Create a new fragment and specify the planet to show based on position
        switch (position) {
            case 0:
                //打开首页
                fragment = new HomeFragment();
//                Bundle args = new Bundle();
//                args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//                fragment.setArguments(args);
                break;
            case 1:
                //打开灯
                fragment = LightFragment.newInstance("我的灯");
                Toast.makeText(MainActivity.this, "进入灯配置", Toast.LENGTH_LONG).show();
                break;
            case 2:
                //打开场景
                fragment = SceneFragment.newInstance("场景");
                Toast.makeText(MainActivity.this, "进入场景配置", Toast.LENGTH_LONG).show();
                break;
            case 3:
                //打开设置
                fragment = SettingsFragment.newInstance("设置");
                Toast.makeText(MainActivity.this, "进入设置", Toast.LENGTH_LONG).show();
                break;
            default:
                //打开首页
                fragment = new HomeFragment();
                break;
        }


        // Insert the fragment by replacing any existing fragment

        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        leftListView.setItemChecked(position, true);
        setTitle(leftItems[position]);
        this.currentSelectFrag = position;
//        invalidateOptionsMenu();        //刷新menu
        drawerLayout.closeDrawer(leftListView);
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
    protected void onResume() {
        super.onResume();

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
                    //首页
                    menu.findItem(R.id.action_frag_main_add).setVisible(true);
                    menu.findItem(R.id.action_frag_main_edit).setVisible(false);
                    break;
                case 1:
                    menu.findItem(R.id.action_frag_main_add).setVisible(false);
                    menu.findItem(R.id.action_frag_main_edit).setVisible(true);
                    break;
                default:
                    menu.findItem(R.id.action_frag_main_edit).setVisible(false);
                    menu.findItem(R.id.action_frag_main_add).setVisible(false);
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
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_frag_main_add:
                Toast.makeText (MainActivity.this, "添加场景", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
//    public static class PlanetFragment extends Fragment {
//        public static final String ARG_PLANET_NUMBER = "planet_number";
//
//        public PlanetFragment() {
//            // Empty constructor required for fragment subclasses
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
//            int i = getArguments().getInt(ARG_PLANET_NUMBER);
//            String planet = getResources().getStringArray(R.array.left_nav_items)[i];
//
//            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
//                    "drawable", getActivity().getPackageName());
//            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
//            getActivity().setTitle(planet);
//            return rootView;
//        }
//    }
}
