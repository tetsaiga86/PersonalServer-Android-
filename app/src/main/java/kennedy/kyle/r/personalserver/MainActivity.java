package kennedy.kyle.r.personalserver;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

import kennedy.kyle.r.personalserver.fragments.ListFragmentActivity;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private String[] mCurrentPosition = {};
    private Button mQrBtn;
    private Button mLoginBtn;
    private String mJsonString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mQrBtn = (Button) findViewById(R.id.qr_sign_in_button);
        mLoginBtn = (Button) findViewById(R.id.login_button);
        mQrBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                scanQR(v);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                SharedPreferences login = MainActivity.this.getSharedPreferences("Login", 0);
                if (login.contains("JsonString")) {
                    mJsonString = login.getString("JsonString", null);
                    newScreenFragment();
                }else{
                    Toast.makeText(getApplicationContext(), "Scan QR code to setup client", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createSave(String jsonString) {
        SharedPreferences login = getSharedPreferences("Login", 0);
        SharedPreferences.Editor editor = login.edit();
        editor.putString("JsonString", jsonString);
        editor.commit();
    }

    private void newScreenFragment(){
        Intent intent = new Intent(getApplicationContext(), ListFragmentActivity.class);
        intent.putExtra("path", mCurrentPosition);
        intent.putExtra("jsonString", mJsonString);
        startActivity(intent);
        finish();

        Log.i(TAG, "newScreenFragment: called");
    }

    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MainActivity.this, "No Scanner Found", "Download a QR scanner?", "Yes", "No").show();
        }

    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                mJsonString = intent.getStringExtra("SCAN_RESULT");
                createSave(mJsonString);
                newScreenFragment();
            }
        }
    }

}
