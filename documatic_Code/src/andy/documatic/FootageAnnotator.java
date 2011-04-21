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
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class FootageAnnotator extends TabActivity implements OnTabChangeListener {
	
	Animation controller;
	
	
	TextView sectionText;
	TextView recordStatus;

	List<Element> locSectionElements;
	List<String> locCurrentProjSections;
	
	List<Element> locPeopleElements;
	ArrayList<String> locCurrentPeople;
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
	protected static final String NARRATION = "narration";
	public static final String EXHIBIT = "exhibit";

	protected static final String NAME = "name";
	TextView mTitleRight;
	public Context edContext;
	boolean isrecording;
	String chosenSection;
	public Spinner intPeopleSpinner;
	public Spinner exhPeopleSpinner;
	public TabHost mTabHost;
	TabWidget vTabs;
	View exPersonBox;
	View exNoLinkBox;
	View exNarrLinkBox;
	RadioButton linkNarButt;
	RadioButton linkNoButt;
	String exhibitLinker;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.footage_annotator);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

	

		
		sectionText = (TextView) findViewById(R.id.theSectionTextView);
		sectionText.setText("-Choose Section-");
		recordStatus = (TextView) findViewById(R.id.recordStatus);
		recordStatus.setText("Waiting to record...");
		recordStatus.setTextColor(getResources().getColor(R.color.dull_gray));
		
		View recBack = findViewById(R.id.mainLL);
		recBack.setBackgroundResource(R.color.MainFootageBackground);
		edContext = this;
		context = getApplicationContext();
		
		new AnimationUtils();
		controller = AnimationUtils.loadAnimation(context, R.anim.flasher);
		controller.setRepeatCount(-1);
		controller.setRepeatMode(2);
		
		setupTabs();
		loadContent();
		setupButtons();
		setCurrentPerson(intPeopleSpinner.getSelectedItem());

	}

	private void setupTabs() {
		// Initialize the tab feature
		vTabs= getTabWidget();
		mTabHost = getTabHost();
		mTabHost.setOnTabChangedListener(this);

		mTabHost.addTab(mTabHost
				.newTabSpec(INTERVIEW)
				.setIndicator("Interview",
						getResources().getDrawable(R.drawable.binterviewclear))
				.setContent(R.id.textview2));
		mTabHost.addTab(mTabHost
				.newTabSpec(EXHIBIT)
				.setIndicator("Exhibit",
						getResources().getDrawable(R.drawable.bexhibitclear))
				.setContent(R.id.textview1));
		mTabHost.addTab(mTabHost
				.newTabSpec(NARRATION)
				.setIndicator("Narration",
						getResources().getDrawable(R.drawable.bnarrationclear))
				
				.setContent(R.id.textview3));
		
		
		
		mTabHost.setCurrentTabByTag(INTERVIEW);
		
		
//		 setupTab(new TextView(this), "Tab 1");

		
}

	private void setupTab(final View view, final String tag) {
		
		    View tabview = createTabView(mTabHost.getContext(), tag);
		
		        TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(new TabHost.TabContentFactory() {
		
		        public View createTabContent(String tag) {return view;}
		
		    });
				    mTabHost.addTab(setContent);
		
		}
				 
				private static View createTabView(final Context context, final String text) {
		
		    View view = LayoutInflater.from(context).inflate(R.layout.narrtab, null);
		
//		    TextView tv = (TextView) view.findViewById(R.id.recordStatus);
//				    tv.setText(text);
		
		    return view;
				}

	
	
	private void loadContent() {
		// TODO Auto-generated method stub

		
	exPersonBox=findViewById(R.id.personBox);
		exNoLinkBox=findViewById(R.id.noBox);
		exNarrLinkBox=findViewById(R.id.narrBox);
		linkNarButt=(RadioButton) findViewById(R.id.narLink);
		linkNoButt=(RadioButton) findViewById(R.id.noLink);
		
		
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
		List<String> fillintPeopleSpinner = new ArrayList<String>(); 
			
		Log.i(START, locCurrentPeople +" loccurrentpoeple");
fillintPeopleSpinner= (List<String>) locCurrentPeople.clone();
		fillintPeopleSpinner.add("Add New Person");

		// Load the Project Spinner
		intPeopleSpinner = (Spinner) findViewById(R.id.intPeopleSpinner);
		exhPeopleSpinner=(Spinner) findViewById(R.id.exhPeopleSpinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,  fillintPeopleSpinner);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		intPeopleSpinner.setAdapter(adapter);
		
		//Exhibit Spinner
		List<String> fillexhPeopleSpinner = new ArrayList<String>(); 

		Log.i(START, locCurrentPeople +" loccurrentpoeple");
fillexhPeopleSpinner =(List<String>) locCurrentPeople.clone();
//fillexhPeopleSpinner.add(0,"NO PERSON");
fillexhPeopleSpinner.add("Add New Person");

ArrayAdapter<String> exhadapter = new ArrayAdapter<String>(this,
		android.R.layout.simple_spinner_item, fillexhPeopleSpinner);

exhadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
exhPeopleSpinner.setAdapter(exhadapter);
Log.i(START, locCurrentPeople +" loccurrentpoeple");

//TODO change color of NO PERSON and also ADD NEW PERSON
//exhPeopleSpinner.getChildAt(exhPeopleSpinner.getSelectedItemPosition()).setBackgroundResource(R.drawable.listgradient);
//Log.i(START, intPeopleSpinner.findViewById(0).toString());
		
		
	}

	public void setupButtons() {

		
		//Stop Button
		final Button exButtonNoLink;
		exButtonNoLink = (Button) findViewById(R.id.noLink);
		exButtonNoLink.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				exhPeopleSpinner.getBackground().setAlpha(100);
				
			exPersonBox.setBackgroundColor(getResources().getColor( R.color.transparent));
				exNarrLinkBox.setBackgroundColor(getResources().getColor( R.color.transparent));
				
				exNoLinkBox.setBackgroundColor(getResources().getColor( R.color.darkhighlight));
				
				linkNarButt.setChecked(false);
				linkNarButt.getBackground().setAlpha(100);
				
				//Stop recording!
				ListView l1 = (ListView) findViewById(R.id.ExListView01);
				((View) l1.getParent()).performHapticFeedback(1, 2);
				l1.setItemChecked(l1.getCheckedItemPosition () , false);
				if (currentClip != null) {

				stopCurrentClip();
				recordStatus.setText("Waiting to record...");
				recordStatus.setTextColor(getResources().getColor(R.color.dull_gray));
				View recBack = findViewById(R.id.mainLL);
				recBack.setBackgroundResource(R.color.MainFootageBackground);
				sectionText.setText("- Choose Section -");
				controller.reset();

				  controller.cancel();
				}
				
				
				
			}
			});
		
		final Button exButtonNarLink;
		exButtonNarLink = (Button) findViewById(R.id.narLink);
		exButtonNarLink.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				exhPeopleSpinner.getBackground().setAlpha(100);
				
				View peopleBox = findViewById(R.id.personBox);
				peopleBox.setBackgroundColor(getResources().getColor(R.color.transparent));
				View narrBox = findViewById(R.id.narrBox);
				narrBox.setBackgroundColor(getResources().getColor( R.color.darkhighlight));
				
				View noBox = findViewById(R.id.noBox);
				noBox.setBackgroundColor(getResources().getColor(R.color.transparent));
				
				linkNoButt.setChecked(false);
				linkNoButt.getBackground().setAlpha(100);
				
				//Stop recording!
				ListView l1 = (ListView) findViewById(R.id.ExListView01);
				((View) l1.getParent()).performHapticFeedback(1, 2);
				l1.setItemChecked(l1.getCheckedItemPosition () , false);
				if (currentClip != null) {

				stopCurrentClip();
				recordStatus.setText("Waiting to record...");
				recordStatus.setTextColor(getResources().getColor(R.color.dull_gray));
				View recBack = findViewById(R.id.mainLL);
				recBack.setBackgroundResource(R.color.MainFootageBackground);
				sectionText.setText("- Choose Section -");
				controller.reset();

				  controller.cancel();
				}
				

			}
			});
		
		
		
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
				recordStatus.setTextColor(getResources().getColor(R.color.dull_gray));
				View recBack = findViewById(R.id.mainLL);
				recBack.setBackgroundResource(R.color.MainFootageBackground);
				sectionText.setText("- Choose Section -");
				controller.reset();

				  controller.cancel();
				}

			}
		});
		
		
		//Virtual Clip Recording LISTVIEW
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
				if(mTabHost.getCurrentTabTag().equals(INTERVIEW)){
				intPeopleSpinner .startAnimation(controller);}
				if(mTabHost.getCurrentTabTag().equals(EXHIBIT)){
					exhPeopleSpinner .startAnimation(controller);}

				parent0.performHapticFeedback(1, 2);
				//Animate here!
				recordStatus.setText("Recording:");
				recordStatus.setTextColor(getResources().getColor(R.color.redd));
				View recBack = findViewById(R.id.mainLL);
				recBack.setBackgroundResource(R.color.redd);

				// Finish up with the old clip ...
				if (currentClip != null) {
					stopCurrentClip();
				
				}

				// Start recording the new clip
				currentClip = Main.currentProjParsed.createElement(CLIP);
				currentClip.setAttribute(START, "" + System.currentTimeMillis());
				currentClip.setAttribute(TYPE, mTabHost.getCurrentTabTag());
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
		intPeopleSpinner
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
						recordStatus.setTextColor(getResources().getColor(R.color.dull_gray));
						View recBack = findViewById(R.id.mainLL);
						recBack.setBackgroundResource(R.color.MainFootageBackground);
						sectionText.setText("- Choose Section -");
						controller.reset();

						  controller.cancel();
						}
						// Check New Project Choice
						String create = "Add New Person";
						if (intPeopleSpinner.getSelectedItem().equals(create)) {

							createNewPerson();
						} else {
							
							
							exhPeopleSpinner.setSelection(position);
							setCurrentPerson(intPeopleSpinner.getSelectedItem());

						}

					}

					private void createNewPerson() {

						// Pop up a dialog to type in a new name
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
						inputTitle.setHint("Title: e.g. Librarian- Georgia Tech");

						AlertDialog newprojPopUp = new AlertDialog.Builder(
								FootageAnnotator.this).create();

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
							                intPeopleSpinner.setSelection(locCurrentPeople.size()-2);
											setCurrentPerson(intPeopleSpinner.getSelectedItem());

										

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
						if (intPeopleSpinner.getSelectedItem().equals(create)) {

							createNewPerson();

						}
						
					}

				});
		//Exhibit People SPinner
		exhPeopleSpinner.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//Toggle other buttons
				exhPeopleSpinner.getBackground().setAlpha(255);
								
								exPersonBox.setBackgroundColor(getResources().getColor( R.color.darkhighlight));
									exNarrLinkBox.setBackgroundColor(getResources().getColor( R.color.transparent));
									
									exNoLinkBox.setBackgroundColor(getResources().getColor( R.color.transparent));
									
									linkNarButt.setChecked(false);
									linkNarButt.getBackground().setAlpha(100);
									linkNoButt.setChecked(false);
									linkNoButt.getBackground().setAlpha(100);
									
									//Stop recording!
									ListView l1 = (ListView) findViewById(R.id.ExListView01);
									((View) l1.getParent()).performHapticFeedback(1, 2);
									l1.setItemChecked(l1.getCheckedItemPosition () , false);
									if (currentClip != null) {

									stopCurrentClip();
									recordStatus.setText("Waiting to record...");
									recordStatus.setTextColor(getResources().getColor(R.color.dull_gray));
									View recBack = findViewById(R.id.mainLL);
									recBack.setBackgroundResource(R.color.MainFootageBackground);
									sectionText.setText("- Choose Section -");
									controller.reset();

									  controller.cancel();
									}
									
									
									
				return false;
			}
		});
					
		
		exhPeopleSpinner
		.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				
				
				
				// Check New Project Choice
				String create = "Add New Person";
				if (exhPeopleSpinner.getSelectedItem().equals(create)) {

					createNewPerson();
				} else {
					
					
					intPeopleSpinner.setSelection(position);
					setCurrentPerson(intPeopleSpinner.getSelectedItem());

				}

			}

			private void createNewPerson() {

				// Pop up a dialog to type in a new name
				final FrameLayout fl = new FrameLayout(context);

				final EditText inputName = new EditText(context);
				final EditText inputTitle = new EditText(context);
				final LinearLayout nameHolder = new LinearLayout(context);
				nameHolder.setOrientation(1);//Vertical
//				inputName.setGravity(Gravity.CENTER);
//				inputTitle.setGravity(Gravity.CENTER);
				
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
				inputTitle.setHint("Title: e.g. Librarian- Georgia Tech");

				AlertDialog newprojPopUp = new AlertDialog.Builder(
						FootageAnnotator.this).create();

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
					                intPeopleSpinner.setSelection(locCurrentPeople.size()-2);
									setCurrentPerson(intPeopleSpinner.getSelectedItem());

								

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
				if (intPeopleSpinner.getSelectedItem().equals(create)) {

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

	public void onTabChanged(String tabId) {
   //
		//Stop any clips that are recording
		ListView l1 = (ListView) findViewById(R.id.ExListView01);
		((View) l1.getParent()).performHapticFeedback(0, 2);
		l1.setItemChecked(l1.getCheckedItemPosition () , false);
		if (currentClip != null) {

			stopCurrentClip();
			recordStatus.setText("Waiting to record...");
			recordStatus.setTextColor(getResources().getColor(R.color.dull_gray));
			sectionText.setText("- Choose Section -");
			View recBack = findViewById(R.id.mainLL);
			recBack.setBackgroundResource(R.color.MainFootageBackground);
			controller.reset();

			  controller.cancel();
			}
		Log.i("Tag",mTabHost.getCurrentTabTag());

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