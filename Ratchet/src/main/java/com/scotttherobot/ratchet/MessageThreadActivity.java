package com.scotttherobot.ratchet;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageThreadActivity extends Activity {

    String threadId;
    String threadName;
    ArrayList<HashMap<String, String>> messageList = new ArrayList<HashMap<String, String>>();
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_thread);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        Intent thisIntent = getIntent();
        threadId = thisIntent.getStringExtra("threadid");
        threadName = thisIntent.getStringExtra("threadname");

        setTitle(threadName);

        getThreadData();
    }

    public void getThreadData() {
        messageList = new ArrayList<HashMap<String, String>>();
        ApiClient.get("threads/" + this.threadId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                //Log.d("THREAD", "Response: " + response.toString());
                try {
                    JSONArray messages = response.getJSONArray("transcript");
                    // We need to convert the JSONArray to
                    // a HashMap so we can use it to fill the ListView.
                    for (int i = 0; i < messages.length(); i++) {
                        try {
                            JSONObject message = messages.getJSONObject(i);
                            //Log.d("THREAD", "message: " + message.toString());
                            HashMap<String, String> map = new HashMap<String, String>();

                            String userid = message.get("userid").toString();

                            if (userid.compareTo(ApiClient.userId) == 0) {
                                map.put("rightimage", ((Integer)(R.drawable.chat)).toString());
                                map.put("leftimage", "0");
                            } else {
                                map.put("leftimage", ((Integer)(R.drawable.chat)).toString());
                                map.put("rightimage", "0");
                            }

                            map.put("body", message.get("body").toString());
                            map.put("userid", userid);
                            map.put("sent", message.get("sent").toString());
                            map.put("username", message.get("username").toString());
                            messageList.add(map);
                        } catch (Exception e) {
                            Log.e("THREAD", "Error parsing json", e);
                        }
                    }
                    // Assign the data to the list.
                    list = (ListView)findViewById(R.id.messageList);

                    SimpleAdapter sa = new SimpleAdapter(getApplicationContext(),
                            messageList, R.layout.messagelist_item,
                            new String[] {"leftimage", "body", "rightimage"},
                            new int[] {R.id.leftImage, R.id.messageBody, R.id.rightImage});
                    sa.setViewBinder(new MessageViewBinder());

                    ListAdapter adapter = sa;

                    list.setAdapter(adapter);
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                        }
                    });

                    list.setSelection(adapter.getCount() - 1);
                } catch (Exception e) {
                    Log.e("LIST", "Error retrieving threads from response.");
                }
            }
        });
    }

    public void sendMessage(View v) {
        EditText messageInput = (EditText)findViewById(R.id.messageInput);
        RequestParams p = new RequestParams();
        p.put("body", messageInput.getText().toString());

        ApiClient.post("threads/" + this.threadId, p, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                getThreadData();
            }
        });
        messageInput.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.message_thread, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.refreshButton:
                // Refresh the data.
                getThreadData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_message_thread, container, false);
            return rootView;
        }
    }

}
