package com.DeviceTest;

import java.util.ArrayList;
import java.util.regex.Pattern;






import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.DeviceTest.ViewFileActivity.ViewFileEntry;




public class ViewFileAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<ViewFileEntry> myList=new ArrayList<ViewFileEntry>();
    public ViewFileAdapter(Context context,ArrayList<ViewFileEntry> mList) 
    {
	    this.mContext = context;
	    this.myList=mList;
    }

	@Override
	public int getCount() {
		return myList.size();
	}

	@Override
	public Object getItem(int position) {
		return myList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) { //by zxx
		 ViewHolder  myViews;
		if(convertView == null)
		{
			myViews=new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_file_item_body, parent, false);
			myViews.name = (TextView) convertView.findViewById(R.id.text_name);
			convertView.setTag(myViews);
		}else{
			
			myViews = (ViewHolder ) convertView.getTag();
		}
		myViews.name.setText(myList.get(position).fileName);
	
		return convertView;
	}
	
	
	static class ViewHolder {
	      private TextView name;
	      
	}
  
}
