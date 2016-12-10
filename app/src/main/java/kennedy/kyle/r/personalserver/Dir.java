package kennedy.kyle.r.personalserver;

public class Dir {
    private boolean mIsDir;
    private int mChildCount;
    private String mName;

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
