package kennedy.kyle.r.personalserver.adapters;

import android.app.DownloadManager;
import android.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import kennedy.kyle.r.personalserver.Interface.ApiCallback;
import kennedy.kyle.r.personalserver.DriveItem;
import kennedy.kyle.r.personalserver.R;
import kennedy.kyle.r.personalserver.View.ViewHolder;
import kennedy.kyle.r.personalserver.fragments.ListFragmentActivity;
import kennedy.kyle.r.personalserver.fragments.RenameDialogFragment;

public class ListAdapter extends ArrayAdapter<DriveItem> implements ApiCallback {
    private Context mContext;
    private int mLayout;
    private String[] mCurrentPath;
    private String mBaseUrl;
    private FragmentManager mFragManager;
    private String mSerializedPath;
    private int mActionType;
    private ListFragmentActivity mListFragmentActivity;
    private String mUsername;
    private String mPassword;
    private String mDomain;
    public ListAdapter(Context context, int resource,
                       List<DriveItem> objects,
                       String[] path, String baseUrl,
                       FragmentManager fragManager,
                       ListFragmentActivity listFragmentActivity,
                       String username, String password, String domain) {
        super(context, resource, objects);
        mUsername = username;
        mPassword = password;
        mDomain = domain;
        mLayout = resource;
        mContext = context;
        mCurrentPath = path;
        //TODO strip out mBaseUrl
        mBaseUrl = baseUrl;
        mFragManager = fragManager;
        mSerializedPath = "";
        for (String s : mCurrentPath) {
            mSerializedPath += s + "%2F";
        }
        mListFragmentActivity = listFragmentActivity;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        DriveItem driveItem = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(mLayout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.setName((TextView) convertView.findViewById(R.id.list_item_name));
            viewHolder.setImageView((ImageView) convertView.findViewById(R.id.folder_image));
            viewHolder.setImageButton((ImageButton) convertView.findViewById(R.id.btn));
            convertView.setTag(R.id.viewHolder, viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag(R.id.viewHolder);
        }
        viewHolder.getImageButton().setTag(R.id.viewData, driveItem);

        int icon;

        viewHolder.getName().setText(driveItem.getName());
        String fileExtension = driveItem.getExtension();
        if (getItem(position).isDir()){
            icon = R.drawable.ic_folder_black_24dp;
        } else {
            switch(fileExtension.toLowerCase()) {
                case "jpg":
                case "png":
                case "tiff":
                    icon = R.drawable.ic_person_black_24dp;
                    break;
                case "mp3":
                case "wma":
                    icon = R.drawable.ic_music_note_black_24dp;
                    break;
                case "mkv":
                case "m4v":
                case "avi":
                case "mp4":
                    icon = R.drawable.ic_videocam_black_24dp;
                    break;
                default:
                    icon = R.drawable.file_black_24dp;
            }
        }
        viewHolder.getImageView().setImageResource(icon);
        viewHolder.getImageButton().setFocusable(false);


        viewHolder.getImageButton().setOnClickListener(new View.OnClickListener() {
            public void showPopup(final View v) {
                final PopupMenu popup = new PopupMenu(getContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.item_menu, popup.getMenu());
                final ApiClient client = new ApiClient(ListAdapter.this, mContext, mUsername, mPassword, mDomain);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        DriveItem driveItem=(DriveItem)v.getTag(R.id.viewData);
                        mActionType = item.getItemId();
                        switch (mActionType){
                            case R.id.download:
                                if(driveItem.isDir()){
                                    client.zipDownload(mCurrentPath, driveItem.getName());
                                }else {
                                    client.download(mCurrentPath, driveItem.getName());
                                }
                                break;
                            case R.id.rename:
                                Log.i("rename", "clicked");
                                RenameDialogFragment rename = new RenameDialogFragment();
                                rename.setPath(mCurrentPath);
                                rename.setOldName(driveItem.getName());
                                rename.setListFragmentActivity(mListFragmentActivity, mUsername, mPassword, mDomain);
                                rename.show(mFragManager, "rename_dialog");

                                break;
                            case R.id.delete:
                                Log.i("delete", "clicked");
                                client.delete(mCurrentPath, driveItem.getName());
                                break;
//
//                            TODO: Not implemented yet.
//                            case R.id.upload:
//                                Log.i("upload", "clicked");
//                                break;
//                            case R.id.update:
//                                Log.i("update", "clicked");
//                                break;
//                            case R.id.mkdir:
//                                Log.i("New Folder", "clicked");
//                                break;
                            default:
                                Log.e("Error", String.valueOf(item.getItemId()));
                                return false;
                        }
                        return true;
                    }
                });
                popup.show();
            }
            @Override
            public void onClick(View v) {
                DriveItem driveItem = (DriveItem)v.getTag(R.id.viewData);
                Log.i("onbtn click", ""+driveItem.getName());
                showPopup(v);

            }
        });

        convertView.setTag(viewHolder);
        return convertView;
    }


    @Override
    public void onSuccess(Object result) {
        switch (mActionType) {
            case R.id.delete:
                Log.i("ListAdapter onSuccess", "Deleted item successfully!");
                mListFragmentActivity.refreshListing();
                break;
            case R.id.rename:
                Log.i("ListAdapter onSuccess", "Renamed item successfully!");
                break;
        }
    }

    @Override
    public void onFailure(Object result) {

    }
}
