package kennedy.kyle.r.personalserver.fragments;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;

import java.util.ArrayList;

import kennedy.kyle.r.personalserver.Interface.ApiCallback;
import kennedy.kyle.r.personalserver.DriveItem;
import kennedy.kyle.r.personalserver.R;
import kennedy.kyle.r.personalserver.adapters.ApiClient;
import kennedy.kyle.r.personalserver.adapters.ListAdapter;


public class ListFragmentActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ApiCallback{
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public ArrayList<DriveItem> mList = new ArrayList<>();
    private String[] mCurrentPath;
    public static final String TAG = ListFragmentActivity.class.getSimpleName();
    private ListView mListView;
    private ListAdapter mListAdapter;
    private String mBaseServerUrl = "http://192.168.0.23:3000/api/";
    private JSONArray mDataArray;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_fragment);

        mListView =(ListView) findViewById(R.id.fragmentListview);
        registerForContextMenu(mListView);
        Intent intent = getIntent();
        mCurrentPath = (String[]) intent.getCharSequenceArrayExtra("path");
        refreshListing();

        mListAdapter = new ListAdapter(this,
                R.layout.list_item,
                mList,
                mCurrentPath,
                mBaseServerUrl,
                getFragmentManager(), this);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: " + mList.get(position));
                if(mList.get(position).isDir()){
                    String[] newPath = new String[mCurrentPath.length+1];
                    for (int i = 0; i<mCurrentPath.length; i++){
                        newPath[i] = mCurrentPath[i];
                    }
                    newPath[mCurrentPath.length] = mList.get(position).getName();
                    Intent intent = new Intent(getApplicationContext(), ListFragmentActivity.class);
                    intent.putExtra("path", newPath);
                    startActivity(intent);

                    Log.i(TAG, "newScreenFragment: called");
                }else{
                    //TODO:
                    Log.i(TAG, "onItemClick: not a fucking dir asshat");
                }


            }
        });


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void refreshListing() {
        getList(serializePath());
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private String serializePath() {
        String path = mBaseServerUrl + "list/";

        for(int i = 0; i < mCurrentPath.length; i++) {
            path += mCurrentPath[i];
            path += "%2F";
        }
        return path;
    }


    private void getList(String path) {
        ApiClient newCall = new ApiClient(this, getApplicationContext());
        newCall.getList(path);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
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
    public void onSuccess(final Object result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onSuccess: called");
                ArrayList<DriveItem> apiList = (ArrayList<DriveItem>)result;
                mListAdapter.clear();
                for(DriveItem driveItem : apiList) {
                    mListAdapter.add(driveItem);
                }
            }
        });
    }

    @Override
    public void onFailure(Object result) {
        alertUserAboutError();
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }
}
