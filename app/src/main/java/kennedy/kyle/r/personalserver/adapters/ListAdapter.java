package kennedy.kyle.r.personalserver.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import kennedy.kyle.r.personalserver.DriveItem;
import kennedy.kyle.r.personalserver.R;
import kennedy.kyle.r.personalserver.View.ViewHolder;

public class ListAdapter extends ArrayAdapter<DriveItem>{
    private Context mContext;
    private int mLayout;
    public ListAdapter(Context context, int resource, List<DriveItem> objects) {
        super(context, resource, objects);
        mLayout = resource;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mainViewHolder = null;
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(mLayout, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.setName((TextView) convertView.findViewById(R.id.list_item_name));
            viewHolder.getName().setText(getItem(position).getName());
            convertView.setTag(viewHolder);
        }else {
            mainViewHolder = (ViewHolder) convertView.getTag();
            mainViewHolder.getName().setText(getItem(position).getName());
        }

        return convertView;
    }

}
