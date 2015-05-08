package com.example.android.networkconnect;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.common.logger.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class DisplayGamesActivity extends Activity {
    private ArrayList<Game> gamesList;
    private CustomListAdapter adapter;
    private Button yes;
    private Button no;

    private Calendar calendar = Calendar.getInstance();
    private int getMonth = calendar.get(Calendar.MONTH) + 1;
    private int getYear = calendar.get(Calendar.YEAR);
    private static final String TAG_MY_APP = "MyApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_games);

        //getting json data from login activity to pass into api calendar
        String playerId;
        String teamId;
        Bundle bundle = getIntent().getExtras();
        playerId = bundle.getString("playerid");
        teamId = bundle.getString("teamid");
        Log.d(TAG_MY_APP, " Bundle in display games activity " + bundle);
        Log.d(TAG_MY_APP, " DisplayActivity " + playerId + " " + teamId);

        //new JSONAsyncTask().execute("https://teamlockerroom.com/api/calendar/408330/17786870/4/2015");
        new JSONAsyncTask().execute("https://teamlockerroom.com/api/calendar/" + teamId + "/" + playerId + "/" + getMonth + "/" + getYear);
        //new JSONAsyncTask().execute("https://teamlockerroom.com/api/calendar/408330/17786870/" + getMonth + "/" + getYear);
        //new JSONAsyncTask().execute("https://teamlockerroom.com/api/calendar/408330/17786870");
        Log.d(TAG_MY_APP, "MONTH " + getMonth + " YEAR " + getYear);

        gamesList = new ArrayList<Game>();
        ListView listView = (ListView)findViewById(R.id.listView);
        adapter = new CustomListAdapter(getApplicationContext(), R.layout.custom_list_adapter, gamesList);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), gamesList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_games, menu);
        return true;
    }

    private class JSONAsyncTask extends AsyncTask<String, Void, JSONArray> {
        private ProgressDialog dialog;
        private static final String TAG_TITLE = "title";
        private static final String TAG_ARENA_NAME = "arenaname";
        private static final String TAG_RINK_NAME = "rinkname";
        private static final String TAG_EVENT_DATE = "eventdate";
        private static final String TAG_ATTENDANCE_STATUS = "attstatus";
        private JSONObject object;
        private JSONArray jsonArray;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(DisplayGamesActivity.this);
            dialog.setMessage("Loading, please wait");
            dialog.setTitle("Connecting to server");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected JSONArray doInBackground(String... urls) {
            try {
                HttpGet httpGet = new HttpGet(urls[0]);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpGet);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if(status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);

                    jsonArray = new JSONArray(data);
                    Log.d(TAG_MY_APP + " JSON Array in DisplayGamesActivity", jsonArray.toString());
                    Log.d(TAG_MY_APP, "hello");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        object = jsonArray.getJSONObject(i);

                        Log.d(TAG_MY_APP + " JSON object in DisplayGamesActivity", object.toString());

                        Game game = new Game();

                        //if user hasn't set attendance yet
                        if(object.getString(TAG_ATTENDANCE_STATUS).equalsIgnoreCase("null")) {
                            game.setTitle(object.getString(TAG_TITLE));
                            game.setArenaName(object.getString(TAG_ARENA_NAME));
                            game.setRinkName(object.getString(TAG_RINK_NAME));
                            game.setEventDate(object.getString(TAG_EVENT_DATE));
                            game.setAttendance("Bro, you haven't set your attendance to this event yet");
                            //if user has set attendance to no
                            if(object.getString(TAG_ATTENDANCE_STATUS).equalsIgnoreCase("0")) {
                                game.setTitle(object.getString(TAG_TITLE));
                                game.setArenaName(object.getString(TAG_ARENA_NAME));
                                game.setRinkName(object.getString(TAG_RINK_NAME));
                                game.setEventDate(object.getString(TAG_EVENT_DATE));
                                game.setAttendance("Not attending this event bro");
                            }
                        } else {
                            game.setTitle(object.getString(TAG_TITLE));
                            game.setArenaName(object.getString(TAG_ARENA_NAME));
                            game.setRinkName(object.getString(TAG_RINK_NAME));
                            game.setEventDate(object.getString(TAG_EVENT_DATE));
                            game.setAttendance("I am attending this event bro");
                        }
                        gamesList.add(game);
                    }
                    return jsonArray;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            dialog.cancel();
            adapter.notifyDataSetChanged();
            super.onPostExecute(jsonArray);
            final Message message = new Message();
            message.obj = jsonArray;
            Log.d(TAG_MY_APP, message.toString());
            if (jsonArray == null) {
                Toast.makeText(getApplicationContext(), "Unable to fetch data from server", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG_MY_APP + " onPostExecute DisplayGamesActivity", jsonArray.toString());
        }
    }
}
