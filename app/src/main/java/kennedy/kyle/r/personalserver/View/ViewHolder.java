package kennedy.kyle.r.personalserver.View;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
    TextView mName;

    public ImageButton getImageButton() {
        return mImageButton;
    }

    public void setImageButton(ImageButton imageButton) {
        mImageButton = imageButton;
    }

    ImageButton mImageButton;
    ImageView mImageView;

    public ImageView getImageView() {
        return mImageView;
    }

    public void setImageView(ImageView imageView) {
        mImageView = imageView;
    }



    public TextView getName() {
        return mName;
    }

    public void setName(TextView name) {
        mName = name;
    }


}