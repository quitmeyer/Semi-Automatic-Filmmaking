package andy.documatic;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class Narration extends Activity {

	Animation controller;

	TextView sectionText;
	TextView recordStatus;

	List <Element>locSectionElements;
	List <String> locCurrentProjSections;

	Element currentClip;
	
	public final String START = "start";
	public final String END = "end";
	public final String DURATION = "duration";
	
	public final String CLIP = "clip";
	public final String SECTIONS = "sections";
	public final String SECTION = "section";
	public final String PEOPLE = "people";
	public final String PERSON = "person";
	protected static final String PERSONTITLE = "title";

	public final String TYPE = "type";
	
	protected static final String NARRATION = "narration";
	protected static final String NAME = "name";



	public Context edContext;
	boolean isrecording;
	String chosenSection;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.narration);

        
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        TextView mTitleLeft = (TextView) findViewById(R.id.title_left_text);
		mTitleLeft.setText(Main.CurrentProject);

        TextView mTitleRight = (TextView) findViewById(R.id.title_right_text);
        mTitleRight.setText("Record Narration");


		sectionText = (TextView) findViewById(R.id.theSectionTextView);
		sectionText.setText("- Choose Section -");
		recordStatus = (TextView) findViewById(R.id.recordStatus);
		recordStatus.setText("Waiting to record...");
		
        edContext=this;
isrecording = false;
		
		new AnimationUtils();
		controller = AnimationUtils.loadAnimation(edContext, R.anim.flasher);
		controller.setRepeatCount(-1);
		controller.setRepeatMode(2);
        
     loadContent();
		setupButtons();

    	
    }
    
    
    
    private void loadContent() {
		// TODO Auto-generated method stub
		
         
    	//get sections from main document
		locSectionElements = new ArrayList<Element>();
		locCurrentProjSections = new ArrayList<String>();

		if (Main.currentProjParsed != null) {

			// get the items you want
			NodeList sections = Main.currentProjParsed
					.getElementsByTagName("section");
			for (int i = 0; i < sections.getLength(); i++) {
				locSectionElements.add((Element) sections.item(i));

				locCurrentProjSections.add(locSectionElements.get(i).getAttribute(
						"name"));
			}

		}
       	
       //  Load in the current project sections
      ListView thelist = (ListView) findViewById(R.id.ExListView01);
      ArrayAdapter<String> Listadapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, locCurrentProjSections);
      thelist.setAdapter(Listadapter);
        
    	thelist.setItemsCanFocus(false);
    	thelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    	thelist.setHapticFeedbackEnabled(true);
    	thelist.performHapticFeedback(0, 2);
  
        
    	
	}



	public void setupButtons() {

		
ListView l1 = (ListView) findViewById(R.id.ExListView01);
		
l1.setOnItemClickListener(new OnItemClickListener() {

	@Override
	public void onItemClick(AdapterView<?> parent0, View clickedView,
			int position, long id) {

		final String selectedString = (String) ((TextView) clickedView)
				.getText();

		sectionText.setText((CharSequence) selectedString);
		controller.reset();
		sectionText.startAnimation(controller);
		parent0.performHapticFeedback(1, 2);

		//Animate here!
		recordStatus.setText("Recording:");
		// Finish up with the old clip ...
		if (currentClip != null) {
			stopCurrentClip();
		
		}
		
		
		// Start recording the new clip
		currentClip = Main.currentProjParsed
				.createElement(CLIP);
		currentClip.setAttribute(START, ""+System.currentTimeMillis());
		currentClip.setAttribute(TYPE, NARRATION);
		currentClip.setAttribute(PERSON, "NO PERSON");
		currentClip.setAttribute(PERSONTITLE, "NO PERSON");
		chosenSection=selectedString;


	}
});
		
		
	// Add a new Section quickkey
    	ImageButton biAddSection;
		biAddSection = (ImageButton) findViewById(R.id.addSecButton);
		biAddSection.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Add New Section!
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

								final String newSectionTitle = input.getText().toString();
				                Toast.makeText(getBaseContext(), newSectionTitle, Toast.LENGTH_LONG).show();
				                
				                Element newSectionEl = Main.currentProjParsed.createElement("section");
				                newSectionEl.setAttribute("name", newSectionTitle);
				                
				                if(Main.currentProjParsed.getElementsByTagName("section").item(0)!=null){
				                Main.currentProjParsed.getElementsByTagName("sections").item(0).insertBefore(newSectionEl, Main.currentProjParsed.getElementsByTagName("sections").item(0)
				                		.getLastChild()) ;
				                }
				                else{
				                	 Main.currentProjParsed.getElementsByTagName("sections").item(0).appendChild(newSectionEl);
				                }
				                Main.writeCurrentDocumenttoXML();
				                loadContent();
				             

				                
				                
				                
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
		
		
		Button ibBackButton;
		ibBackButton = (Button) findViewById(R.id.backButton01);
		ibBackButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ListView l1 = (ListView) findViewById(R.id.ExListView01);
				((View) l1.getParent()).performHapticFeedback(1, 2);
				l1.setItemChecked(l1.getCheckedItemPosition () , false);
				if (currentClip != null) {

				stopCurrentClip();
				recordStatus.setText("Waiting to record...");
				sectionText.setText("- Choose Section -");
				controller.reset();

				  controller.cancel();
				}

			}
		});
		
		
	}
	@Override
	protected void onPause(){
		//Finish up with the old clip ...
		if(currentClip!=null){
		currentClip.setAttribute(END, ""+System.currentTimeMillis());
		long duration= -Long.parseLong(currentClip.getAttribute("start"))+ Long.parseLong(currentClip.getAttribute(END));
			currentClip.setAttribute(DURATION, ""+duration);
			
			//Write it into the proper spot in the manifest
			for(int i = 0; i<Main.currentProjParsed.getElementsByTagName("section").getLength();i++){
				Element aSection=(Element) Main.currentProjParsed.getElementsByTagName("section").item(i);
			if (aSection.getAttribute(NAME)==chosenSection){
				Main.currentProjParsed.getElementsByTagName("section").item(i).appendChild(currentClip);
                Main.writeCurrentDocumenttoXML();

				Toast.makeText(edContext, "WROTE", 0);

			}

			}
			
		}
		super.onPause();
	
		
		
	}

	protected void stopCurrentClip() {
		
		currentClip.setAttribute(END,
				"" + System.currentTimeMillis());
		long duration = -Long.parseLong(currentClip
				.getAttribute("start"))
				+ Long.parseLong(currentClip.getAttribute(END));
		currentClip.setAttribute(DURATION, "" + duration);

		// Write it into the proper spot in the manifest
		for (int i = 0; i < Main.currentProjParsed
				.getElementsByTagName("section").getLength(); i++) {
			Element aSection = (Element) Main.currentProjParsed
					.getElementsByTagName("section").item(i);
			if (aSection.getAttribute(NAME) == chosenSection) {
				Main.currentProjParsed
						.getElementsByTagName("section").item(i)
						.appendChild(currentClip);
				Main.writeCurrentDocumenttoXML();
				currentClip=null;

			}

		}
		
	}
   
}