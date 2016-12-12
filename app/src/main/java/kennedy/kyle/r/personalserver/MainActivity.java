package kennedy.kyle.r.personalserver;

import android.content.Context;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import kennedy.kyle.r.personalserver.adapters.ListAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private String mBaseServerUrl = "http://192.168.0.23:3000/api/";
    private ProgressBar mProgressBar;
    private String mRootList;
    private ArrayList<String> mList = new ArrayList<>();
    private ListAdapter mListAdapter;
    private ListView lv;
    private String mCurrentPosition;
    private JSONArray mDataArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv = (ListView) findViewById(R.id.listview);
        mListAdapter = new ListAdapter(MainActivity.this,
                R.layout.list_item,
                mList);

        lv.setAdapter(mListAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: " + mList.get(position));
                
                saveCurrentPosition(mList.get(position));
                try {
                    if(mDataArray.getJSONObject(position).getBoolean("isDir")){
                        openActivityForNewList();
                    }else{
                        //TODO:
                        Log.i(TAG, "onItemClick: not a fucking dir asshat");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
        getList("");





        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    private void saveCurrentPosition(String position){
        mCurrentPosition = position;
    }

    private void openActivityForNewList() {
        String formatted = mCurrentPosition+"%2F";
        Log.i(TAG, "openActivityForNewList: "+formatted);
        getList(formatted);
    }


    private void getList(String path) {
        String serverListUrl = mBaseServerUrl + "list/" + path;
        Log.i(TAG, "getList: "+serverListUrl);
        if(isNetworkAvailable()){
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(serverListUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i(TAG, "onFailure: "+ e.getMessage() + e.getStackTrace());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();

                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            mDataArray = new JSONArray(jsonData);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mListAdapter.clear();

                                    for (int i = 0; i < mDataArray.length(); i++){
                                        try {
                                            mListAdapter.add(mDataArray.getJSONObject(i).getString("name"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Log.i(TAG, "After for loop, mDataAdapter= "+ mDataArray);
                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (JSONException | IOException e) {
                        String errorMessage = Log.getStackTraceString(e);
                        Log.i(TAG, "Exception caught");
                        while(!errorMessage.isEmpty()) {
                            int end = Math.min(80, errorMessage.length());
                            Log.i(TAG, errorMessage.substring(0, end));
                            errorMessage = errorMessage.substring(end);
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.network_unavailable_message,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    private DriveItem parseJsonDetails(String jsonData) throws JSONException {
        DriveItem driveItem = new DriveItem();
        driveItem.setChildCount(getChildCount(jsonData));
        //driveItem.setName(getName(jsonData));
        //driveItem.isDir(getIsDir(jsonData));
        Log.i(TAG, "parseJsonDetails: "+ driveItem);
        return driveItem;
    }

    private int getChildCount(String jsonData) throws JSONException {
        JSONObject list = new JSONObject(jsonData);
        return list.getInt("childCount");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        Log.i(TAG, "isNetworkAvailable: " + isAvailable);
        return isAvailable;
    }

    private void toggleRefresh() {
//        if (mProgressBar.getVisibility() == View.INVISIBLE) {
//            mProgressBar.setVisibility(View.VISIBLE);
//        } else {
//            mProgressBar.setVisibility(View.INVISIBLE);
//        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }



}
