package kennedy.kyle.r.personalserver;

public class DriveItem {
    private boolean mIsDir;
    private int mChildCount;
    private String mName;
    private int mSize;
    private String mBirthTime;

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public String getBirthTime() {
        return mBirthTime;
    }

    public void setBirthTime(String birthTime) {
        mBirthTime = birthTime;
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
