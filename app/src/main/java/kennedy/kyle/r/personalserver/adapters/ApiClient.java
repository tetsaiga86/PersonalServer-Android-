package kennedy.kyle.r.personalserver.adapters;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Authenticator;
import java.util.ArrayList;

import kennedy.kyle.r.personalserver.DriveItem;
import kennedy.kyle.r.personalserver.Interface.ApiCallback;
import kennedy.kyle.r.personalserver.R;
import kennedy.kyle.r.personalserver.fragments.DownloadDialogFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class ApiClient{
    public ArrayList<DriveItem> mList = new ArrayList<>();
    public static final String TAG = ApiClient.class.getSimpleName();
    public JSONArray mDataArray;
    private Context mContext;
    private ApiCallback mCaller;
    public static final String PROTOCOL = "https://";
    public String mUsername;
    public String mPassword;
    public String mLoginInfo;
    public String mDomain;
    public String mBaseUrl;
    private String mEncodedCredentials;

    public ApiClient(ApiCallback caller, Context context, String username, String password, String domain) {
        mCaller = caller;
        mContext = context;
        mUsername = username;
        mPassword = password;
        mLoginInfo = username+":"+password;
        mDomain = domain;
        mBaseUrl = PROTOCOL + domain + "/api/";
        mEncodedCredentials = "Basic " + Base64.encodeToString(mLoginInfo.getBytes(), Base64.NO_WRAP);

    }

    private String getUrlPathFragment(String[] folders) {
        String url = "";
        for(String s : folders) {
            url += s + "%2F";
        }
        return url;
    }

    private String getPathFragment(String[] folders) {
        String path = "";
        for(String s : folders) {
            path += s + "/";
        }
        return path;
    }

    public void getList(String path) {
        if (!ensureNetworkAvailable()) {
            return;
        }

        okhttp3.Authenticator authenticator = new okhttp3.Authenticator() {

            @Override
            public Request authenticate(Route route, Response response) throws IOException {

                String credential = Credentials.basic(mUsername, mPassword);
                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            }
        };
        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator(authenticator)
                .build();
        Request request = new Request.Builder()
                .url(path)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mCaller.onFailure(call);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    try {

                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {

                            mDataArray = new JSONArray(jsonData);
                            try {
                                for(int i = 0; i < mDataArray.length(); i++) {
                                    JSONObject jsonItem = mDataArray.getJSONObject(i);
                                    mList.add(new DriveItem(jsonItem));
                                }
                                mCaller.onSuccess(mList);
                            } catch (JSONException e) {

                            }
                        } else {
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
    }

    public void rename(String[] folderNames, final String oldFileName, final String newFileName) {
        if (!ensureNetworkAvailable()) {
            return;
        }
        okhttp3.Authenticator authenticator = new okhttp3.Authenticator() {

            @Override
            public Request authenticate(Route route, Response response) throws IOException {

                String credential = Credentials.basic(mUsername, mPassword);
                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            }
        };
        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator(authenticator)
                .build();
        JSONObject postBody = new JSONObject();
        try {
            postBody.put("newPath", getPathFragment(folderNames) + newFileName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), postBody.toString());

        Request request = new Request.Builder()
                .url(mBaseUrl+"rename/"+getUrlPathFragment(folderNames)+oldFileName)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        Log.i(TAG, "call: "+ call.toString());
        Log.i(TAG, "request: "+request.toString());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mCaller.onFailure(call);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    mCaller.onSuccess(true);
                } else {
                    mCaller.onFailure(true);
                }
            }
        });

    }

    public void download(String[] folderNames, final String name) {
        if (!ensureNetworkAvailable()) {
            return;
        }
        final DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mBaseUrl+"download/"+Uri.encode(getPathFragment(folderNames)+name)));
        Log.i(TAG, "download request: " + mBaseUrl+"download/"+Uri.encode(getPathFragment(folderNames)+name));
        request.addRequestHeader("Authorization", mEncodedCredentials);
        request.setTitle(name)
                .setDescription(getPathFragment(folderNames))
                .setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, name)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setVisibleInDownloadsUi(true)
                .allowScanningByMediaScanner();

        downloadManager.enqueue(request);
        Toast.makeText(mContext, name+" has begun downloading!", Toast.LENGTH_LONG).show();
    }

    public void zipDownload(final String[] folderNames, final String name) {
        if (!ensureNetworkAvailable()) {
            return;
        }

        final DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mBaseUrl+"zip/"+Uri.encode(getPathFragment(folderNames)+name)));
        request.addRequestHeader("Authorization", mEncodedCredentials);
        request.setTitle(name)
                .setDescription(getPathFragment(folderNames))
                .setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, name)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        downloadManager.enqueue(request);

        Toast.makeText(mContext, name+" has begun zipping and will begin downloading shortly!", Toast.LENGTH_LONG).show();
    }

    public void delete(String[] folderNames, String name){
        if (!ensureNetworkAvailable()) {
            return;
        }

        Log.i(TAG, "delete, url sent: "+mBaseUrl+"remove/"+getUrlPathFragment(folderNames)+name);
        okhttp3.Authenticator authenticator = new okhttp3.Authenticator() {

            @Override
            public Request authenticate(Route route, Response response) throws IOException {

                String credential = Credentials.basic(mUsername, mPassword);
                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            }
        };
        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator(authenticator)
                .build();
        Request request = new Request.Builder()
                .url(mBaseUrl+"remove/"+getUrlPathFragment(folderNames)+name)
                .delete()
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mCaller.onFailure(call);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    mCaller.onSuccess(true);
                } else {
                    mCaller.onFailure(true);
                }
            }
        });
    }
    private boolean ensureNetworkAvailable() {
        if (!isNetworkAvailable(mContext)) {
            Toast.makeText(mContext, R.string.network_unavailable_message,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        Log.i(TAG, "isNetworkAvailable: " + isAvailable);
        return isAvailable;
    }
}
