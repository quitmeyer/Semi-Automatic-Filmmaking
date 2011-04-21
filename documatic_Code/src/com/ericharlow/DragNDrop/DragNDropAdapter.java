package com.ericharlow.DragNDrop;

import java.util.ArrayList;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import andy.documatic.R;

public final class DragNDropAdapter extends BaseAdapter implements RemoveListener, DropListener{

	private int[] mIds;
    private int[] mLayouts;
    public ArrayList<Integer> cInt;
    public ArrayList<Integer>cExh;
    public ArrayList<Integer> cNar;



    private LayoutInflater mInflater;
    private ArrayList<String> mContent;

    public DragNDropAdapter(Context context, ArrayList<String> content, ArrayList<Integer> Exh,ArrayList<Integer> Nar, ArrayList<Integer>Int) {
        init(context,new int[]{android.R.layout.simple_list_item_1},new int[]{android.R.id.text1}, content, Int, Exh, Nar );
    }
    
    public DragNDropAdapter(Context context, int[] itemLayouts, int[] itemIDs, ArrayList<String> content, ArrayList<Integer> Exh,ArrayList<Integer> Nar, ArrayList<Integer>Int) {
    	init(context,itemLayouts,itemIDs, content, Exh, Nar, Int);
    }

    private void init(Context context, int[] layouts, int[] ids, ArrayList<String> content,ArrayList<Integer> Exh,ArrayList<Integer> Nar, ArrayList<Integer>Int) {
    	// Cache the LayoutInflate to avoid asking for a new one each time.
    	mInflater = LayoutInflater.from(context);
    	mIds = ids;
    	mLayouts = layouts;
    	mContent = content;
    	cInt= Int;
    	cExh=Exh;
    	cNar=Nar;
    	
    }
    
    /**
     * The number of items in the list
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
        return mContent.size();
    }

    /**
     * Since the data comes from an array, just returning the index is
     * sufficient to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
     *
     * @see android.widget.ListAdapter#getItem(int)
     */
    public String getItem(int position) {
        return mContent.get(position);
    }

    /**
     * Use the array index as a unique id.
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Make a view to hold each row.
     *
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(mLayouts[0], null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(mIds[0]);
            convertView.setTag(holder);
            holder.intHold = (TextView) convertView.findViewById(R.id.INTNUM);
            holder.exhHold = (TextView) convertView.findViewById(R.id.EXHNUM);
            holder.narHold = (TextView) convertView.findViewById(R.id.NARNUM);


        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        holder.text.setText(mContent.get(position));
       
        Log.i("cint",""+cInt.get(position));
        Integer IntAmount=cInt.get(position);
        holder.intHold.setText(""+IntAmount);
        
        Integer EXHAmount=cExh.get(position);
        holder.exhHold.setText(""+EXHAmount);
        
        Integer NARAmount=cNar.get(position);
        holder.narHold.setText(""+NARAmount);
        
        return convertView;
    }

    static class ViewHolder {
        TextView text;
        TextView intHold;
        TextView exhHold;
        TextView narHold;
    }

	public void onRemove(int which) {
		if (which < 0 || which > mContent.size()) return;		
		mContent.remove(which);
	}

	public void onDrop(int from, int to) {
		String temp = mContent.get(from);
		mContent.remove(from);
		mContent.add(to,temp);
	}
}