package kennedy.kyle.r.personalserver.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class DownloadDialogFragment extends DialogFragment{
    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    private String mFileName;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Download Finished!")
                .setMessage("Do you want to open "+mFileName+" now?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                        context.startActivity(intent);
                    }
                });
        return builder.create();
    }
}
