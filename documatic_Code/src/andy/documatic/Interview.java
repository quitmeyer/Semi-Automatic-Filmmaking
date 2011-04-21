package andy.documatic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class Interview extends Activity {
	
	Animation controller;
	
	
	TextView sectionText;
	TextView recordStatus;

	List<Element> locSectionElements;
	List<String> locCurrentProjSections;
	
	List<Element> locPeopleElements;
	List<String> locCurrentPeople;
	public static String currentPersonName;
	public static String currentPersonTitle;
	Element currentClip;
	public Context context;
	


	protected static final String START = "start";
	protected static final String END = "end";
	protected static final String DURATION = "duration";

	protected static final String CLIP = "clip";
	protected static final String SECTIONS = "sections";
	protected static final String SECTION = "section";
	protected static final String PEOPLE = "people";
	protected static final String PERSON = "person";
	protected static final String PERSONTITLE = "title";

	protected static final String TYPE = "type";

	protected static final String INTERVIEW = "interview";
	protected static final String NAME = "name";

	public Context edContext;
	boolean isrecording;
	String chosenSection;
	public Spinner peoplespinner;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.interview);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		TextView mTitleLeft = (TextView) findViewById(R.id.title_left_text);
		mTitleLeft.setText(Main.CurrentProject);

		TextView mTitleRight = (TextView) findViewById(R.id.title_right_text);
		mTitleRight.setText("Interview Footage");

		
		sectionText = (TextView) findViewById(R.id.theSectionTextView);
		sectionText.setText("-Choose Section-");
		recordStatus = (TextView) findViewById(R.id.recordStatus);
		recordStatus.setText("Waiting to record...");
		edContext = this;
		context = getApplicationContext();
		
		new AnimationUtils();
		controller = AnimationUtils.loadAnimation(context, R.anim.flasher);
		controller.setRepeatCount(-1);
		controller.setRepeatMode(2);
		
		
		loadContent();
		setupButtons();
		setCurrentPerson(peoplespinner.getSelectedItem());

	}

	private void loadContent() {
		// TODO Auto-generated method stub

		
		// get sections from main document
		locSectionElements = new ArrayList<Element>();
		locCurrentProjSections = new ArrayList<String>();
		locPeopleElements = new ArrayList<Element>();
		locCurrentPeople = new ArrayList<String>();

		if (Main.currentProjParsed != null) {

			// get the sections you want
			NodeList sections = Main.currentProjParsed
					.getElementsByTagName("section");
			for (int i = 0; i < sections.getLength(); i++) {
				locSectionElements.add((Element) sections.item(i));

				locCurrentProjSections.add(locSectionElements.get(i)
						.getAttribute("name"));
			}
			//Get the people you want
			NodeList persons = Main.currentProjParsed
			.getElementsByTagName("person");
			Log.i(START, persons.getLength()+" PERSONS");
	for (int i = 0; i < persons.getLength(); i++) {
		locPeopleElements.add((Element) persons.item(i));

		locCurrentPeople.add(locPeopleElements.get(i)
				.getAttribute("name"));
	}

		}

		// Load in the current project sections
		ListView thelist = (ListView) findViewById(R.id.ExListView01);
		ArrayAdapter<String> Listadapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice,
				locCurrentProjSections);
		thelist.setAdapter(Listadapter);

		thelist.setItemsCanFocus(false);
		thelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		thelist.setHapticFeedbackEnabled(true);
		thelist.performHapticFeedback(3);
		thelist.requestFocus();

		//Load in the current people
		List<String> fillProjectSpinner = new ArrayList<String>(); 
			
		Log.i(START, locCurrentPeople +" loccurrentpoeple");
fillProjectSpinner=locCurrentPeople;
		fillProjectSpinner.add("Add New Person");

		// Load the Project Spinner
		peoplespinner = (Spinner) findViewById(R.id.mProjSpinner);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, fillProjectSpinner);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		peoplespinner.setAdapter(adapter);
		
		
	}

	public void setupButtons() {

		//Stop Button
		final Button ibBackButton;
		ibBackButton = (Button) findViewById(R.id.backButton01);
		ibBackButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Do things
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
				peoplespinner .startAnimation(controller);

				parent0.performHapticFeedback(1, 2);
				//Animate here!
				recordStatus.setText("Recording:");
				// Finish up with the old clip ...
				if (currentClip != null) {
					stopCurrentClip();
				
				}

				// Start recording the new clip
				currentClip = Main.currentProjParsed.createElement(CLIP);
				currentClip.setAttribute(START, "" + System.currentTimeMillis());
				currentClip.setAttribute(TYPE, INTERVIEW);
				//Set person attribute
				currentClip.setAttribute(PERSON, currentPersonName);
				currentClip.setAttribute(PERSONTITLE, currentPersonTitle);

				chosenSection = selectedString;


			}
		});

		// Add a new Section quickkey
		ImageButton biAddSection;
		biAddSection = (ImageButton) findViewById(R.id.addSecButton);
		biAddSection.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Add New Section!
				// Add Text Alert Dialog to type in new thing

				final FrameLayout fl = new FrameLayout(getBaseContext());

				final EditText input = new EditText(getBaseContext());

				input.setGravity(Gravity.CENTER);

				fl.addView(input, new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.FILL_PARENT,
						FrameLayout.LayoutParams.WRAP_CONTENT));

				input.setHint("Type Name of New Section");

				// AlertDialog.Builder builder = new AlertDialog.Builder(null);

				AlertDialog newSection = new AlertDialog.Builder(edContext)
						.create();

				newSection.setView(fl);

				newSection
						.setTitle("Create a new section for your documentary...");

				// Create Button
				newSection.setButton("Create",
						new DialogInterface.OnClickListener() {

							public void onClick(final DialogInterface dMAIN,
									int which) {

								final String newSectionTitle = input.getText()
										.toString();
								Toast.makeText(getBaseContext(),
										newSectionTitle, Toast.LENGTH_LONG)
										.show();

								Element newSectionEl = Main.currentProjParsed
										.createElement("section");
								newSectionEl.setAttribute("name",
										newSectionTitle);
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
							public void onClick(DialogInterface d, int which) {

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


		
		//People Spinner!
		peoplespinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						
						//Stop recording!
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
						// Check New Project Choice
						String create = "Add New Person";
						if (peoplespinner.getSelectedItem().equals(create)) {

							createNewPerson();
						} else {
							
							
							
							setCurrentPerson(peoplespinner.getSelectedItem());

						}

					}

					private void createNewPerson() {

						// TODO Pop up a dialog to type in a new name
						final FrameLayout fl = new FrameLayout(context);

						final EditText inputName = new EditText(context);
						final EditText inputTitle = new EditText(context);
						final LinearLayout nameHolder = new LinearLayout(context);
						nameHolder.setOrientation(1);//Vertical
//						inputName.setGravity(Gravity.CENTER);
//						inputTitle.setGravity(Gravity.CENTER);
						
						nameHolder.addView(inputName, new FrameLayout.LayoutParams(
								FrameLayout.LayoutParams.FILL_PARENT,
								FrameLayout.LayoutParams.WRAP_CONTENT));
						nameHolder.addView(inputTitle, new FrameLayout.LayoutParams(
								FrameLayout.LayoutParams.FILL_PARENT,
								FrameLayout.LayoutParams.WRAP_CONTENT));
						fl.addView(nameHolder, new FrameLayout.LayoutParams(
								FrameLayout.LayoutParams. FILL_PARENT,
								FrameLayout.LayoutParams.FILL_PARENT));
						
						// input.setText("Preset Text");
						inputName.setHint("Name: e.g. Kitty Kelly");
						inputTitle.setHint("Title: e.g. Microbiologist-UIUC");

						AlertDialog newprojPopUp = new AlertDialog.Builder(
								Interview.this).create();

						newprojPopUp.setView(fl);

						newprojPopUp.setTitle("Please enter the interviewee's name...");

						// Create Button
						newprojPopUp.setButton("Create",
								new DialogInterface.OnClickListener() {

									public void onClick(
											final DialogInterface dMAIN,
											int which) {

										final String newPersonName = inputName.getText().toString().trim();
										final String newPersonTitle = inputTitle.getText().toString().trim();
										
										Element newPersonEl = Main.currentProjParsed.createElement("person");
							                newPersonEl.setAttribute("name", newPersonName);
							                newPersonEl.setAttribute("title", newPersonTitle);

							                Main.currentProjParsed.getElementsByTagName("people").item(0).appendChild(newPersonEl) ;
							                
							                
							                Main.writeCurrentDocumenttoXML();
							                loadContent();
							                peoplespinner.setSelection(locCurrentPeople.size()-2);
											setCurrentPerson(peoplespinner.getSelectedItem());

										

									}

								});
						// Cancel Button
						newprojPopUp.setButton2("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface d,
											int which) {

										// d.dismiss();

									}

								});

						newprojPopUp.show();
						inputName.requestFocus();
						newprojPopUp
								.getWindow()
								.setSoftInputMode(
										WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

	
					}

					@Override
					public void onNothingSelected(AdapterView<?> parentView) {
						// your code here
						
						String create = "Add New Person";
						if (peoplespinner.getSelectedItem().equals(create)) {

							createNewPerson();

						}
						
					}

				});
		
		

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

	protected void setCurrentPerson(Object selectedItem) {
		// TODO Auto-generated method stub
		currentPersonName = (String) selectedItem;

		//Get the people you want
		NodeList persons = Main.currentProjParsed
		.getElementsByTagName("person");
for (int i = 0; i < persons.getLength(); i++) {
	if(((Element) persons.item(i)).getAttribute("name").equalsIgnoreCase(currentPersonName)){
		currentPersonTitle = ((Element) persons.item(i)).getAttribute("title");
Log.i(START, currentPersonTitle);
	}

	
}
		
	}

	@Override
	protected void onPause() {
		// Finish up with the old clip ...
		if (currentClip != null) {
			currentClip.setAttribute(END, "" + System.currentTimeMillis());
			long duration = -Long.parseLong(currentClip.getAttribute("start"))
					+ Long.parseLong(currentClip.getAttribute(END));
			currentClip.setAttribute(DURATION, "" + duration);

			// Write it into the proper spot in the manifest
			for (int i = 0; i < Main.currentProjParsed.getElementsByTagName(
					"section").getLength(); i++) {
				Element aSection = (Element) Main.currentProjParsed
						.getElementsByTagName("section").item(i);
				if (aSection.getAttribute(NAME) == chosenSection) {
					Main.currentProjParsed.getElementsByTagName("section")
							.item(i).appendChild(currentClip);
					Main.writeCurrentDocumenttoXML();

					Toast.makeText(edContext, "WROTE", 0);

				}

			}

		}
		super.onPause();

	}

}