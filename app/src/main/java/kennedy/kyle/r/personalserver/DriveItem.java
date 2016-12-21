package kennedy.kyle.r.personalserver;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;


public class DriveItem implements Parcelable {
    private boolean mIsDir;
    private int mChildCount;
    private String mName;
    private int mSize;
    private DateTime mDate;
    private JSONObject source;

    public DriveItem(JSONObject apiItem) {
        source = apiItem;
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

    protected DriveItem(Parcel in) {
        mIsDir = in.readByte() != 0;
        mChildCount = in.readInt();
        mName = in.readString();
        mSize = in.readInt();
    }

    public static final Creator<DriveItem> CREATOR = new Creator<DriveItem>() {
        @Override
        public DriveItem createFromParcel(Parcel in) {
            String jsonString = in.readString();
            try {
                return new DriveItem(new JSONObject(jsonString));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public DriveItem[] newArray(int size) {
            return new DriveItem[size];
        }
    };

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

    public String getExtension() {
        if (mName != null) {
            String[] split = mName.split("\\.");
            return split[split.length-1];
        }
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(source.toString());
    }
}
