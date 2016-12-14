package kennedy.kyle.r.personalserver;

import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;


public class DriveItem {
    private boolean mIsDir;
    private int mChildCount;
    private String mName;
    private int mSize;
    private DateTime mDate;

    public DriveItem(JSONObject apiItem) {
        try {
            this.mIsDir = apiItem.getBoolean("isDir");
            this.mName = apiItem.getString("name");
            this.mSize = apiItem.getInt("size");
            this.mDate = new DateTime(apiItem.getString("mtime"));
            if (apiItem.has("childCount")) {
                this.mChildCount = apiItem.getInt("childCount");
            }
        }
        catch(JSONException e) {
            Log.e("DriveItem", "JSONException: " + e.getMessage());
        }


    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public DateTime getDate() {
        return mDate;
    }

    public void setDate(DateTime date) {
        mDate = date;
    }

    public void setDate (String date){
        mDate = new DateTime(date);
    }

    public void setDir(boolean dir) {
        mIsDir = dir;
    }

    public void setChildCount(int childCount) {
        mChildCount = childCount;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isDir() {
        return mIsDir;
    }

    public int getChildCount() {
        return mChildCount;
    }

    public String getName() {
        return mName;
    }
}
