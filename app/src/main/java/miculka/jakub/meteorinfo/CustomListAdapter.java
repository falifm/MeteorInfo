package miculka.jakub.meteorinfo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import miculka.jakub.meteorinfo.Meteor;

/**
 * This class extends ArrayAdapter and represents one item in the listView
 */
public class CustomListAdapter extends ArrayAdapter<Meteor> {

    private final Context context;
    private final List<Meteor> values;

    public CustomListAdapter(Context context, List<Meteor> values) {
        super(context, R.layout.item_view, values);
        this.context = context;
        this.values = values;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item_view, parent, false);

        Meteor currentMeteor = this.values.get(position);

        TextView nameText = (TextView) itemView.findViewById(R.id.listItemName);
        nameText.setText(currentMeteor.getName());

        TextView yearWeightText = (TextView) itemView.findViewById(R.id.listItemYearWeight);
        yearWeightText.setText(currentMeteor.getYear() + ", " + currentMeteor.getMass() + "kg");

        return itemView;
    }
}
