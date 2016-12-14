package kennedy.kyle.r.personalserver.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import kennedy.kyle.r.personalserver.DriveItem;
import kennedy.kyle.r.personalserver.MainActivity;
import kennedy.kyle.r.personalserver.R;
import kennedy.kyle.r.personalserver.adapters.ListAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class FragmentActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final String TAG = FragmentActivity.class.getSimpleName();
    private ListView mListView;
    private ListAdapter mListAdapter;
    public ArrayList<DriveItem> mList = new ArrayList<>();
    private String[] mCurrentPath;
    private String mBaseServerUrl = "http://192.168.0.23:3000/api/";
    private JSONArray mDataArray;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_fragment);

        mListView =(ListView) findViewById(R.id.fragmentListview);
        mListAdapter = new ListAdapter(this,
                R.layout.list_item,
                mList);
        mListView.setAdapter(mListAdapter);

        Intent intent = getIntent();
        mCurrentPath = (String[]) intent.getCharSequenceArrayExtra("path");

//        if (savedInstanceState == null){
//            Log.i("savedInstanceState ", "is null: ");
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .add(R.id.fragmentLayout, new PlaceholderFragment(),"list")
//                    .commit();
//        }else{
//            Log.i("savedInstanceState ", "is not null: ");
//        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: " + mList.get(position));

                try {
                    if(mDataArray.getJSONObject(position).getBoolean("isDir")){
                        String[] newPath = new String[mCurrentPath.length+1];
                        for (int i = 0; i<mCurrentPath.length; i++){
                            newPath[i] = mCurrentPath[i];
                        }
                        newPath[mCurrentPath.length] = mDataArray.getJSONObject(position).getString("name");
                        Intent intent = new Intent(getApplicationContext(), FragmentActivity.class);
                            intent.putExtra("path", newPath);
                            startActivity(intent);

                            Log.i(TAG, "newScreenFragment: called");
                    }else{
                        //TODO:
                        Log.i(TAG, "onItemClick: not a fucking dir asshat");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        String path = "";

        for(int i = 0; i < mCurrentPath.length; i++) {
            path += mCurrentPath[i];
            path += "%2F";
        }
        getList(path);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void getList(String path) {
        String serverListUrl = mBaseServerUrl + "list/" + path;
        Log.i(TAG, "getList: "+serverListUrl);
        if(isNetworkAvailable()){
            //TODO:
            //toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(serverListUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Log.i(TAG, "onFailure: "+ e.getMessage() + sw.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //toggleRefresh();

                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {

                            mDataArray = new JSONArray(jsonData);





                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for(int i = 0; i < mDataArray.length(); i++) {
                                            JSONObject jsonItem = mDataArray.getJSONObject(i);
                                            mList.add(new DriveItem(jsonItem));
                                        }
                                    } catch (JSONException e) {

                                    }

//                                    mListAdapter.notifyDataSetChanged();
//                                    //mListAdapter.clear();
//
//                                    for (int i = 0; i < mList.size(); i++){
//                                        mListAdapter.add(mList.get(i));
//                                    }
                                    Log.i(TAG, "After for loop, mDataAdapter.size= "+ mListAdapter.getCount());
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

//    public static class PlaceholderFragment extends android.support.v4.app.Fragment {
//        public PlaceholderFragment(){
//
//        }
//
//        @Nullable
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//            View view = inflater.inflate(R.layout.list_fragment, container, false);
//
//            return view;
//        }
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
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
}
