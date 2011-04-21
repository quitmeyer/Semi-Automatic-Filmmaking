package andy.documatic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ericharlow.DragNDrop.DragListener;
import com.ericharlow.DragNDrop.DragNDropAdapter;
import com.ericharlow.DragNDrop.DragNDropListView;
import com.ericharlow.DragNDrop.DropListener;
import com.ericharlow.DragNDrop.RemoveListener;


public class EditSections extends ListActivity {
	public static Context edContext;
	
	List <Element>locSectionElements;
	List <String> locCurrentProjSections;
    private ArrayList<Integer> cInt;
    private ArrayList<Integer>cExh;
    private ArrayList<Integer> cNar;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.editsections);
        

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        TextView mTitleLeft = (TextView) findViewById(R.id.title_left_text);
        mTitleLeft.setText(Main.CurrentProject);

        TextView mTitleRight = (TextView) findViewById(R.id.title_right_text);
        mTitleRight.setText("Arrange/Add Sections");
    	
        edContext=this;

       
        loadSections();
           
    	Button bAddSections;
    	bAddSections = (Button) findViewById(R.id.mAddSection);
    	bAddSections.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {

                //Add Text Alert Dialog to type in new thing
           
				final FrameLayout fl = new FrameLayout(getBaseContext());

				final EditText input = new EditText(getBaseContext());

				input.setGravity(Gravity.CENTER);

				fl.addView(input, new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.FILL_PARENT,
						FrameLayout.LayoutParams.WRAP_CONTENT));

				// input.setText("Preset Text");
				input.setHint("Type Name of New Section");

//				AlertDialog.Builder builder = new AlertDialog.Builder(null);

				AlertDialog newSection = new AlertDialog.Builder(edContext).create();

				newSection.setView(fl);

				newSection.setTitle("Create a new section for your documentary...");

				// Create Button
				newSection.setButton("Create",
						new DialogInterface.OnClickListener() {

							public void onClick(
									final DialogInterface dMAIN,
									int which) {

								String newSectionTitle = input.getText().toString().trim();
								newSectionTitle.trim();
				                Toast.makeText(getBaseContext(), newSectionTitle, Toast.LENGTH_LONG).show();
				                
				                Element newSectionEl = Main.currentProjParsed.createElement("section");
				                newSectionEl.setAttribute("name", newSectionTitle);
				                Main.currentProjParsed.getElementsByTagName("sections").item(0).insertBefore(newSectionEl, Main.currentProjParsed.getElementsByTagName("sections").item(0).getLastChild()) ;
				                
				                
				                Main.writeCurrentDocumenttoXML();
				                loadSections();
				             
				                
				                
				                
							}
								
						});
				// Cancel Button
				newSection.setButton2("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface d,
									int which) {

								// d.dismiss();

							}

						});

				newSection.show();
				input.requestFocus();
				newSection
						.getWindow()
						.setSoftInputMode(
								WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    		}
    	});
    	
    	
        
    }

	private void loadSections() {
		// TODO Auto-generated method stub
		
		//get sections from main document
		locSectionElements = new ArrayList<Element>();
		locCurrentProjSections = new ArrayList<String>();
		cInt= new ArrayList<Integer>();
	    cExh= new ArrayList<Integer>();
	    cNar= new ArrayList<Integer>();
		if (Main.currentProjParsed != null) {

			// get the sections you want
			NodeList sections = Main.currentProjParsed
					.getElementsByTagName("section");
			for (int i = 0; i < sections.getLength(); i++) {
				locSectionElements.add((Element) sections.item(i));

				locCurrentProjSections.add(locSectionElements.get(i).getAttribute(
						"name"));
				
				//Find how many clips are part of each section
				Element currentSection = (Element) sections.item(i);
				// get the clips from that section
				NodeList clips = sections.item(i).getChildNodes();
			
				// A) run through all clips and tally up how many of each
				int narCount=0;
				int exhCount=0;
				int intCount=0;

				for (int j = 0; j < clips.getLength(); j++) {
					Element clip = (Element) clips.item(j);

					if (clip.getAttribute("type").equals("narration")) {
						narCount++;
					}
					if (clip.getAttribute("type").equals("exhibit")) {
						exhCount++;
					}
					if (clip.getAttribute("type").equals("interview")) {
						intCount++;
					}
					
					
					
				}
				cNar.add(narCount);
				cExh.add(exhCount);
				cInt.add(intCount);

				
				
				
				
				
			}

		}
		
		
		//Load in sections of current project
	     
        setListAdapter(new DragNDropAdapter(this, new int[]{R.layout.dragitem}, new int[]{R.id.TextView01}, (ArrayList<String>) locCurrentProjSections,cExh, cNar,cInt));//new DragNDropAdapter(this,content)
        ListView listView = getListView();
        
        if (listView instanceof DragNDropListView) {
        	((DragNDropListView) listView).setDropListener(mDropListener);
        	((DragNDropListView) listView).setRemoveListener(mRemoveListener);
        	((DragNDropListView) listView).setDragListener(mDragListener);
        }
     
		
		
	}

	private DropListener mDropListener = 
		new DropListener() {
        public void onDrop(int from, int to) {
        	ListAdapter adapter = getListAdapter();
        	if (adapter instanceof DragNDropAdapter) {
        		((DragNDropAdapter)adapter).onDrop(from, to);
        		//Swap Sections
        		if (Main.currentProjParsed != null) {
    				Log.i("yo", "it wants to swap from="+from+" to="+to);

        			
        			//rootSection
        			Node rootSections = Main.currentProjParsed
					.getElementsByTagName("sections").item(0);

        			// get the sections you want
         			NodeList swapNodeList = rootSections.getChildNodes();
    				Log.i("yo", "totalSections="+swapNodeList.getLength());

         			Element a = (Element) swapNodeList.item(from);
         			Element b = (Element) swapNodeList.item(to);
         			Element after_b = (Element) b.getNextSibling();

         			a=(Element) rootSections.insertBefore(a, b);
//         			rootSections.replaceChild(a, b);
         			

        				Log.i("yo", "it wants to swap from="+from+" to="+to);
        				Main.writeCurrentDocumenttoXML();
        				
        			

        		}
        		
        		
        		
        		getListView().invalidateViews();
        	}
        }
    };
    
    private RemoveListener mRemoveListener =
        new RemoveListener() {
        public void onRemove(int which) {
        	ListAdapter adapter = getListAdapter();
        	if (adapter instanceof DragNDropAdapter) {
        		((DragNDropAdapter)adapter).onRemove(which);
        		getListView().invalidateViews();
        	}
        }
    };
    
    private DragListener mDragListener =
    	new DragListener() {

    	int backgroundColor = 0xe0103010;
    	int defaultBackgroundColor;
    	
			public void onDrag(int x, int y, ListView listView) {
				// TODO Auto-generated method stub
			}

			public void onStartDrag(View itemView) {
				itemView.setVisibility(View.INVISIBLE);
				defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
				itemView.setBackgroundColor(backgroundColor);
				ImageView iv = (ImageView)itemView.findViewById(R.id.ImageView01);
				if (iv != null) iv.setVisibility(View.INVISIBLE);
			}

			public void onStopDrag(View itemView) {
				itemView.setVisibility(View.VISIBLE);
				itemView.setBackgroundColor(defaultBackgroundColor);
				ImageView iv = (ImageView)itemView.findViewById(R.id.ImageView01);
				if (iv != null) iv.setVisibility(View.VISIBLE);
			}
    	
    };
    

    
    
}