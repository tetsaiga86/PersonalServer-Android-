package kennedy.kyle.r.personalserver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import kennedy.kyle.r.personalserver.fragments.FragmentActivity;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private String[] mCurrentPosition = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.list_fragment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        newScreenFragment();
    }

    private void newScreenFragment(){
        Intent intent = new Intent(getApplicationContext(), FragmentActivity.class);
        intent.putExtra("path", mCurrentPosition);
        startActivity(intent);

        Log.i(TAG, "newScreenFragment: called");
    }
}
