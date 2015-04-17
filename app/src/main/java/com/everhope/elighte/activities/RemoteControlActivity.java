package com.everhope.elighte.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.everhope.elighte.R;

public class RemoteControlActivity extends ActionBarActivity {

    private short remoterID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String remoterStrID  = intent.getStringExtra("remoter_id");
        this.remoterID = Short.parseShort(remoterStrID);

        setContentView(R.layout.activity_remote_control);
        setTitle("遥控器");

        findViewById(R.id.remoter_gp1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent remoterIntent = new Intent(RemoteControlActivity.this, RemoterGroupActivity.class);
                remoterIntent.putExtra("remoter_id",remoterID);
                remoterIntent.putExtra("remoter_gp_num",1);
                startActivity(remoterIntent);
            }
        });

        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                //按压actionbar中的回退按钮
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
