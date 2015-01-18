package com.everhope.xlight;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;


public class LoadActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_load);

        getSupportActionBar().hide();
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        //模拟5秒钟后进入新画面
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //删除splash view 停止loading
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                //显示actionbar
//                getSupportActionBar().show();
                handler.removeCallbacks(this);

                Intent intent = new Intent(LoadActivity.this, MainActivity.class);
                startActivity(intent);
                //
                LoadActivity.this.finish();
            }
        }, 5000);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_load, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
