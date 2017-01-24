package kennedy.kyle.r.personalserver.fragments;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private String mJsonString;
    private JSONObject mJson;
    private String mBaseServerUrl;
    private String mBaseServerUrlWithLogin;
    private JSONArray mDataArray;
    private GoogleApiClient mGoogleApiClient;
    private String mDomain = "";
    private String mLoginInfo = "";
    private String mUserName = "";
    private String mPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_fragment);


        mListView =(ListView) findViewById(R.id.fragmentListview);
        registerForContextMenu(mListView);
        Intent intent = getIntent();
        mCurrentPath = (String[]) intent.getCharSequenceArrayExtra("path");
        mJsonString = intent.getStringExtra("jsonString");
        try {
            Log.i(TAG, "onCreate: "+mJsonString);
            mJson = new JSONObject(mJsonString);
            mBaseServerUrl = mJson.getString("url") + "/api/";
            Log.i(TAG, "onCreate: " + mBaseServerUrl);
            String[] serverUrl = mJson.getString("url").split("");
            for (int i=9; i<serverUrl.length; i++){
                mDomain += serverUrl[i];
            }
            mUserName = mJson.getString("username");
            mPassword = mJson.getString("password");
            mLoginInfo = mUserName + ":" + mPassword;
            mBaseServerUrlWithLogin = "https://" + mLoginInfo + "@" + mDomain + "/api/";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(savedInstanceState == null || savedInstanceState.getParcelable("listState") == null){
            refreshListing();
        }

        mListAdapter = new ListAdapter(this,
                R.layout.list_item,
                mList,
                mCurrentPath,
                mBaseServerUrl,
                getFragmentManager(), this,
                mUserName, mPassword, mDomain);

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
                    intent.putExtra("jsonString", mJsonString);
                    startActivity(intent);

                    Log.i(TAG, "newScreenFragment: called");
                }else{
                    String fileExtension = mList.get(position).getExtension();
                    String uri = mBaseServerUrlWithLogin + "download/";
                    String path = "";
                    for (int i = 0; i < mCurrentPath.length; i++) {
                        path += mCurrentPath[i] + "/";
                    }
                    uri += Uri.encode(path + mList.get(position).getName());
                    Uri parsedUri = Uri.parse(uri);
                    Log.i(TAG, "onItemClick: "+ parsedUri);
                    Intent implicitIntent = new Intent(Intent.ACTION_VIEW, parsedUri);
                    String auth = mLoginInfo;
                    String authBase64 = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
                    Bundle bundle = new Bundle();
                    bundle.putString("Authorization", "Basic " + authBase64);
                    implicitIntent.putExtra(Browser.EXTRA_HEADERS, bundle);
                    Intent chooser = Intent.createChooser(implicitIntent, "Choose App to play media:");
                    chooser.putExtra(Browser.EXTRA_HEADERS, bundle);
                    switch(fileExtension.toLowerCase()) {
                            case "jpg":
                            case "png":
                            case "tiff":
                                implicitIntent.setDataAndType(parsedUri, "image/*");
                                if (implicitIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(chooser);
                                }
                                break;
                            case "mp3":
                            case "wma":
                                implicitIntent.setDataAndType(parsedUri, "audio/*");
                                if (implicitIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(chooser);
                                }else {
                                    try {
                                        Intent intent = new Intent(getApplicationContext(), VideoFragmentActivity.class);
                                        intent.putExtra("uri", uri);
                                        startActivity(intent);
                                        Log.i(TAG, "onItemClick: uri=" + uri);
                                    } catch (Exception e) {
                                        alertUserAboutError();
                                        Log.e(TAG, "onItemClick: " + e.getMessage());
                                    }
                                }
                                break;
                            case "mkv":
                            case "m4v":
                            case "avi":
                            case "mp4":
                                implicitIntent.setDataAndType(parsedUri, "video/*");
                                if (implicitIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(chooser);
                                }else {
                                    try {
                                        Intent intent = new Intent(getApplicationContext(), VideoFragmentActivity.class);
                                        intent.putExtra("uri", uri);
                                        startActivity(intent);
                                        Log.i(TAG, "onItemClick: uri=" + uri);
                                    } catch (Exception e) {
                                        alertUserAboutError();
                                        Log.e(TAG, "onItemClick: " + e.getMessage());
                                    }
                                }
                                break;
                            default:
                                Toast.makeText(ListFragmentActivity.this,
                                        mList.get(position).getName()+" is not a recognized file format. Please try downloading to local storage.",
                                        Toast.LENGTH_LONG)
                                        .show();
                        }

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
        ApiClient newCall = new ApiClient(this, getApplicationContext(), mUserName, mPassword, mDomain);
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

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", mList);
        outState.putParcelable("listState", mListView.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.getParcelableArrayList("list") != null) {
            ArrayList<DriveItem> items = savedInstanceState.getParcelableArrayList("list");
            onSuccess(items);
        }
        mListView.onRestoreInstanceState(savedInstanceState.getParcelable("listState"));
    }
}
