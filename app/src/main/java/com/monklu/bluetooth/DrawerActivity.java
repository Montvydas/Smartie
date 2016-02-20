package com.monklu.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DrawerActivity extends Activity implements OnItemClickListener {

    protected DrawerLayout mDrawerLayout;            //DrawerLayout
    protected FrameLayout mFrameLayout;
    protected ListView mDrawerList;                //List View

    private ArrayAdapter<String> mAdapter;
    private String[] mDrawerItems = {"Connect", "Color Rainbow", "Guitar Colors"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_drawer);

        initialiseObjects();

    }

    private void initialiseObjects() {
        mDrawerList = (ListView) findViewById(R.id.left_drawer);            //List of activities
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);    //Layout
        mFrameLayout = (FrameLayout) findViewById(R.id.content_frame);      //Will need this one later in child activity

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDrawerItems);   //configure adapter
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        if (parent.getId() == R.id.left_drawer) {
            mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mDrawerList.setItemChecked(position, true);         //set 'checked'
            getActionBar().setTitle(mDrawerItems[position]);   //change the title to the activity name

            switch (position) {
                case 0:
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    startActivity(new Intent(this, BluetoothActivity.class));
                    //start the first activity
                    break;
                case 1:
                      mDrawerLayout.closeDrawer(Gravity.LEFT);
                    //start the second activity
                    break;
                case 2:
                    //the third activity
                    break;
                default:
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       // mDrawerLayout.openDrawer(Gravity.START);
    }
    /*
    @Override
    public void setContentView(int layoutResID) {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer, null);
        getLayoutInflater().inflate(layoutResID, mFrameLayout, true);
    }
*/
}
