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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import kennedy.kyle.r.personalserver.DriveItem;
import kennedy.kyle.r.personalserver.Interface.ApiCallback;
import kennedy.kyle.r.personalserver.R;
import kennedy.kyle.r.personalserver.fragments.DownloadDialogFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient{
    public ArrayList<DriveItem> mList = new ArrayList<>();
    public static final String TAG = ApiClient.class.getSimpleName();
    public JSONArray mDataArray;
    private Context mContext;
    private ApiCallback mCaller;
    public static final String BASE_URL = "http://192.168.0.23:3000/api/";

    public ApiClient(ApiCallback caller, Context context) {
        mCaller = caller;
        mContext = context;
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
        OkHttpClient client = new OkHttpClient();
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
        OkHttpClient client = new OkHttpClient();
        JSONObject postBody = new JSONObject();
        try {
            postBody.put("newPath", getPathFragment(folderNames) + newFileName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), postBody.toString());

        Request request = new Request.Builder()
                .url(BASE_URL+"rename/"+getUrlPathFragment(folderNames)+oldFileName)
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
        Log.i(TAG, "download, url sent: "+BASE_URL+"download/"+Uri.encode(getPathFragment(folderNames)+name));
        final DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(BASE_URL+"download/"+Uri.encode(getPathFragment(folderNames)+name)));
        request.setTitle(name)
                .setDescription(getPathFragment(folderNames))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        final long enqueuedDownloadId = downloadManager.enqueue(request);

//        BroadcastReceiver receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
//                    long downloadId = intent.getLongExtra(
//                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
//                    DownloadManager.Query query = new DownloadManager.Query();
//                    query.setFilterById(enqueuedDownloadId);
//                    Cursor c = downloadManager.query(query);
//                    if (c.moveToFirst()) {
//                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
//                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
//                            mContext.getApplicationContext();
//                            DownloadDialogFragment newFrag = new DownloadDialogFragment();
//                            newFrag.setFileName(name);
//                            newFrag.show(((Activity)context).getFragmentManager(),"download_tag");
//                        }
//                    }
//                }
//            }
//        };

//        mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        Toast.makeText(mContext, name+" has begun downloading!", Toast.LENGTH_LONG).show();
    }

    public void zipDownload(final String[] folderNames, final String name) {
        if (!ensureNetworkAvailable()) {
            return;
        }

        final DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(BASE_URL+"zip/"+Uri.encode(getPathFragment(folderNames)+name)));
        request.setTitle(name)
                .setDescription(getPathFragment(folderNames))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        final long enqueuedDownloadId = downloadManager.enqueue(request);

//        BroadcastReceiver receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
//                    long downloadId = intent.getLongExtra(
//                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
//                    DownloadManager.Query query = new DownloadManager.Query();
//                    query.setFilterById(enqueuedDownloadId);
//                    Cursor c = downloadManager.query(query);
//                    if (c.moveToFirst()) {
//                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
//                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
//                            DownloadDialogFragment newFrag = new DownloadDialogFragment();
//                            newFrag.setFileName(name);
//                            newFrag.show(((Activity)context).getFragmentManager(),"download_tag");
//                        }
//                    }
//                }
//            }
//        };
//
//        mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        Toast.makeText(mContext, name+" has begun zipping and will begin downloading shortly!", Toast.LENGTH_LONG).show();
    }

    public void delete(String[] folderNames, String name){
        if (!ensureNetworkAvailable()) {
            return;
        }

        Log.i(TAG, "delete, url sent: "+BASE_URL+"remove/"+getUrlPathFragment(folderNames)+name);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL+"remove/"+getUrlPathFragment(folderNames)+name)
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
