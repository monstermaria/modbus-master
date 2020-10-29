package se.monstermaria.modbusmaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import se.monstermaria.modbusmaster.database.Profile;

public class ProfileAdapter extends BaseAdapter {
    List<Profile> profiles;

    public ProfileAdapter(List<Profile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView text = convertView.findViewById(android.R.id.text1);
        String name = profiles.get(position).toString();
        text.setText(name != null ? name : parent.getContext().getString(R.string.new_profile));
        return convertView;
    }

    @Override
    public Profile getItem(int position) {
        return profiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return profiles.size();
    }
}
