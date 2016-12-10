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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private String mBaseServerUrl = "http://10.0.2.2:3000/api/";
    //"http://192.168.0.23:3000/api/";
    private ProgressBar mProgressBar;
    private String mRootList;
    private ArrayList<String> mList = new ArrayList<>();
    private MyListAdapter mListAdapter;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv = (ListView) findViewById(R.id.listview);
        mListAdapter = new MyListAdapter(MainActivity.this,
                R.layout.list_item,
                mList);

        lv.setAdapter(mListAdapter);
        createFileList();





        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    private void createFileList() {
        getServerRootList();
    }

    private void getServerRootList() {
        String serverListUrl = mBaseServerUrl + "list";

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
                            JSONArray dataArray = new JSONArray(jsonData);
                            for (int i = 0; i < dataArray.length(); i++){
                                mListAdapter.add(dataArray.getJSONObject(i).getString("name"));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });
                            Log.i(TAG, "mlist = "+mList);


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

    private Dir parseJsonDetails(String jsonData) throws JSONException {
        Dir dir = new Dir();
        dir.setChildCount(getChildCount(jsonData));
        //dir.setName(getName(jsonData));
        //dir.isDir(getIsDir(jsonData));
        Log.i(TAG, "parseJsonDetails: "+dir);
        return dir;
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


    private class MyListAdapter extends ArrayAdapter<String> {
        private int mLayout;
        public MyListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            mLayout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewHolder = null;
            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(mLayout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.mName = (TextView) convertView.findViewById(R.id.list_item_name);
                viewHolder.mName.setText(getItem(position));
                convertView.setTag(viewHolder);
            }else {
                mainViewHolder = (ViewHolder) convertView.getTag();
                mainViewHolder.mName.setText(getItem(position));
            }
            return convertView;
        }
    }

    public class ViewHolder {
        TextView mName;
    }
}
