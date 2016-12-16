package kennedy.kyle.r.personalserver.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import kennedy.kyle.r.personalserver.Interface.ApiCallback;
import kennedy.kyle.r.personalserver.adapters.ApiClient;

public class RenameDialogFragment extends DialogFragment implements ApiCallback{

    private String mOldName;
    private String[] mPath;

    public void setListFragmentActivity(ListFragmentActivity listFragmentActivity) {
        this.mListFragmentActivity = listFragmentActivity;
    }

    private ListFragmentActivity mListFragmentActivity;

    public void setOldName(String oldName) {
        mOldName = oldName;
    }

    public void setPath(String[] path) {
        mPath = path;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        final EditText userInput = new EditText(context);
        userInput.setText(mOldName);
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context)
                .setTitle("New Name: ")
                .setView(userInput)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userInputValue = userInput.getText().toString();
                        ApiClient client = new ApiClient(RenameDialogFragment.this, getActivity().getApplicationContext());
                        client.rename(mPath, mOldName, userInputValue);
                    }

                });

        final AlertDialog dialog = builder.create();
        userInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }

    @Override
    public void onSuccess(Object result) {
        mListFragmentActivity.refreshListing();
    }

    @Override
    public void onFailure(Object result) {

    }
}
