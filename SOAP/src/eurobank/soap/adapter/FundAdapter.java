package eurobank.soap.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import eurobank.soap.R;
import eurobank.soap.model.Fund;

public class FundAdapter extends ArrayAdapter<Fund> {
	
	private int resource;
	
	public FundAdapter(Context context, int resource, List<Fund> items) {
		super(context, resource, items);
		this.resource = resource;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout view;
		
		Fund fund = getItem(position);
		if(convertView == null) {
			view = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
			vi.inflate(resource, view, true);
		} else {
			view = (LinearLayout) convertView;
		}
		
		TextView quantity = (TextView) view.findViewById(R.id.quantity);
		TextView itemView = (TextView) view.findViewById(R.id.walletItem);
		
		quantity.setText(fund.getValue().toString());
		itemView.setText(fund.getFundName() + " - " + fund.getFundUnit().getName());
		
		return view;
	}

}
