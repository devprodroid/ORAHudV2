/******************************************************************************

    Copyright (c) EUROGICIEL 2014. All rights reserved.
    This source code is subject to the terms of the ORA SDK License Agreement
    nevertheless, as an exception to this agreement, EUROGICIEL grants you the
    rights to publish, distribute, translate or reproduce it under your own
    responsibility.
    Please visit the following for more information :
    http://www.eurogiciel.fr/index.php/en/products/ora-s-sdk/license

    EUROGICIEL
    417 L'Occitane - CS 77679, 31676 LABEGE Labège Cedex  - France
    Phone: +33 (0)5 61 00 79 79     
    http://www.eurogiciel.fr

******************************************************************************/

package devprodroid.orahudserver.connection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import devprodroid.orahudserver.R;

public class BTListAdapter extends BaseAdapter {

	private Context _context;
	List<BluetoothDevice> _devList;
	
	public BTListAdapter(Context context, List<BluetoothDevice> devList) {
		this._context = context;
	    this._devList = devList;
	}
	
	
	@Override
	public int getCount() {
		return _devList.size();
	}

	@Override
	public Object getItem(int position) {
		return _devList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (_context != null)
		{
			 // inflate the layout for each item of listView
		    LayoutInflater inflater = 
		    		(LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    convertView = inflater.inflate(R.layout.devicelist, null);
		   
		    // fetch the address and the name at the corresponding position.
		    String strMac = _devList.get(position).getAddress();
		    String strName = _devList.get(position).getName();
		  
		    // get the reference of textViews
		    TextView tvMac = (TextView)convertView.findViewById(R.id.tvMAC);
		    TextView tvName = (TextView)convertView.findViewById(R.id.tvName);
		   
		    // Display the name and the MAC address
		    tvName.setText(strName);
		    tvMac.setText(strMac);

			if (strName.contains("ORA-1")){
				tvName.setTextColor(Color.GREEN);
			}
		}
	 
	    return convertView;
	}

}
