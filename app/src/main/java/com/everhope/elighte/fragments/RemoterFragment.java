package com.everhope.elighte.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.everhope.elighte.R;
import com.everhope.elighte.activities.RemoteControlActivity;
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.Remoter;
import com.everhope.elighte.models.SubGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RemoterFragment extends Fragment {
    private static final String TAG = "RemoterFragment@Light";

    private ListView listView;
    private RemoterListAdapter remoterListAdapter;

    // TODO: Rename and change types and number of parameters
    public static RemoterFragment newInstance() {
        RemoterFragment fragment = new RemoterFragment();
        return fragment;
    }

    public RemoterFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_remoter, container, false);

        listView = (ListView)rootView.findViewById(R.id.allremoters_lv);

        List<Remoter> remoters = Remoter.getAll();
        remoterListAdapter = new RemoterListAdapter(getActivity(), remoters);
        remoterListAdapter.setNotifyOnChange(true);
        listView.setAdapter(remoterListAdapter);

        getActivity().setTitle("遥控器");

        return rootView;
    }

    class RemoterListAdapter extends ArrayAdapter<Remoter> {
        public RemoterListAdapter(Context context, List<Remoter> remoters) {
            super(context, 0, remoters);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.remoter_item, parent, false);
            }

            final View remoterView = convertView;
            final Remoter remoter = getItem(position);
            String remoterName = remoter.name;
            TextView textView = (TextView)convertView.findViewById(R.id.remoter_name);
            textView.setText(remoterName);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), RemoteControlActivity.class);
                    intent.putExtra("remoter_id",remoter.remoterID);
                    startActivity(intent);

                }
            });

            ImageView editNameView = (ImageView)convertView.findViewById(R.id.remoter_rename);
            editNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //弹出对话框进行输入
                    EditText editText = new EditText(getActivity());

                    RenameRemoterListener renameLightListener = new RenameRemoterListener(remoter,remoterView,editText);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("重命名").setIcon(android.R.drawable.ic_menu_edit)
                            .setView(editText)
                            .setPositiveButton("确定", renameLightListener)
                            .setNegativeButton("取消", null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    alertDialog.show();

                    editText.setFocusable(true);
                    editText.requestFocus();
                }
            });
            return convertView;
        }
    }

    class RenameRemoterListener implements DialogInterface.OnClickListener {

        private Remoter remoter;
        private View view;
        private EditText editText;

        public RenameRemoterListener(Remoter remoter, View remoterView, EditText newNameET) {
            this.remoter = remoter;
            this.view = remoterView;
            this.editText = newNameET;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            //修改view的名称
            String newName = this.editText.getText().toString();

            TextView textView = (TextView)view.findViewById(R.id.remoter_name);
            textView.setText(newName);
            //存储Light
            this.remoter.name = newName;
            this.remoter.save();
        }
    }
}
