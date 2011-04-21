package andy.documatic;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import com.ericharlow.DragNDrop.DragListener;
import com.ericharlow.DragNDrop.DragNDropAdapter;
import com.ericharlow.DragNDrop.DragNDropListView;
import com.ericharlow.DragNDrop.DropListener;
import com.ericharlow.DragNDrop.RemoveListener;

import ch.elca.el4j.services.xmlmerge.XmlMerge;
import ch.elca.el4j.services.xmlmerge.merge.DefaultXmlMerge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {

	// TODO check for folder for each project (to build list of projects
	// Load sections smartly for all editors
	// Add Sections
	// Write all back to XML
	// Add Bluetooth Sync

	protected static final String START = "start";
	protected static final String END = "end";
	protected static final String DURATION = "duration";

	protected static final String CLIP = "clip";
	protected static final String SECTIONS = "sections";
	protected static final String SECTION = "section";
	protected static final String PEOPLE = "people";
	protected static final String PERSON = "person";
	protected static final String TYPE = "type";

	protected static final String INTERVIEW = "interview";
	protected static final String NAME = "name";
	
	
	
	public Context context;
	public CharSequence toasttext;
	public int toastduration;
	public Toast sdCardToast;
	
	//Documatic's Main entry point to your sd card
	public static File documaticManifestDirectory;
	public static File documaticVidDirectory;
	public List<String> CurrentDirectories;
	public List<String> CurrentProjectsList;


	//Manifest stuff, not really used anymore
	public FileOutputStream manifestFOS;
	public XmlSerializer manifestSerializer;
	public Document manifestParsed;
	public File documaticManifest;

	
	//Custom title
	private TextView mTitleRight;
	private TextView mTitleLeft;

	public Spinner projectspinner;
	
	
	//Here's your whole working document
	public static Document currentProjParsed;
	public static String CurrentProject;
	public static String CurrentProjectFORDIRECTORIES;


	public static List<Element> sectionElements;
	public static List<String> currentProjSections;

	//Editsections zone
	List <Element>locSectionElements;
	List <String> locCurrentProjSections;
    private ArrayList<Integer> cInt;
    private ArrayList<Integer>cExh;
    private ArrayList<Integer> cNar;
	public static Context edContext;

	ListView lv;
	
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		mTitleLeft = (TextView) findViewById(R.id.title_left_text);
		mTitleLeft.setText(R.string.app_name);

		mTitleRight = (TextView) findViewById(R.id.title_right_text);
		mTitleRight.setText(" ");
        edContext=this;
        lv=(ListView) findViewById(R.id.DragonList);
		// setup toaster
		makeToaster();

		// Run initial functions
		checkAndLoadFiles();
		loadContent();
        loadSections();

		setupButtons();

	}

	private void makeToaster() {
		// TODO Auto-generated method stub
		// Make Toaster
		context = getApplicationContext();
		toasttext = "Hello toast!";
		toastduration = Toast.LENGTH_SHORT;

		sdCardToast = Toast.makeText(context, toasttext, toastduration);
	}

	public void checkAndLoadFiles() {
		// TODO Auto-generated method stub

		// Check on availability of the storage
		chkSD();
		// Make MAIN file directories
		documaticManifestDirectory = new File("/sdcard/Documatic/");
		documaticManifestDirectory.mkdirs();
		

	
		// find Current Projects on SD card
		CurrentDirectories = new ArrayList<String>();
		CurrentProjectsList = new ArrayList<String>();

		// Note that Arrays.asList returns a horrible fixed length list, Make
		// sure to cast to ArrayList
	
			CurrentDirectories = new ArrayList<String>(
					Arrays.asList(documaticManifestDirectory.list()));
			for(int z=0;z<CurrentDirectories.size();z++){
				if(CurrentDirectories.get(z).equalsIgnoreCase("Readme.txt"))
					CurrentDirectories.remove(z);
			}
			CurrentProjectsList=CurrentDirectories;
			//Get Rid of weird underscores
			for(int z=0;z<CurrentProjectsList.size();z++){
				CurrentProjectsList.set(z, CurrentProjectsList.get(z).replace('_', ' '));
				Log.i(START, "CMON "+CurrentProjectsList.get(z));
			}
			
			
			
			if(CurrentProjectsList.size()>0)
			setCurrentProject(CurrentProjectsList.get(0));

		 
		// TODO look inside directories to check they are valid

	}

	private void setCurrentProject(Object ProjectSpinnerO) {

		// Set Current Project
		CurrentProject = (String) ProjectSpinnerO;

		mTitleRight.setText(CurrentProject);
		CurrentProjectFORDIRECTORIES= CurrentProject.replace(" ", "_");
		// Load sections from current project
		try {
			// manifestParsed =
			// parseXMLFILE(documaticManifest.toURL().toString());
			currentProjParsed = parseXMLFILE(((documaticManifestDirectory
					.toURL().toString()) + CurrentProjectFORDIRECTORIES + "/projectmanifest__"+CurrentProjectFORDIRECTORIES+".xml"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("CANNNOT PARSE XML FILE",
					"fff error occurred while creating xml file");

		}
		sectionElements = new ArrayList<Element>();
		currentProjSections = new ArrayList<String>();

		if (currentProjParsed != null) {

			// get the items you want
			NodeList sections = currentProjParsed
					.getElementsByTagName("section");
			for (int i = 0; i < sections.getLength(); i++) {
				sectionElements.add((Element) sections.item(i));

				currentProjSections.add(sectionElements.get(i).getAttribute(
						"name"));
			}

		}
		
		documaticVidDirectory = new File("/sdcard/Documatic/"+CurrentProjectFORDIRECTORIES+"/video/");
		
	}

	// Check the SD card, make sure available
	private void chkSD() {
		// TODO Auto-generated method stub
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;

		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		if (mExternalStorageAvailable) {
			toasttext = "External Storage is available";
			sdCardToast = Toast.makeText(context, toasttext,
					toastduration - 100);

//			sdCardToast.show();

		} else {
			toasttext = "External Storage is NOT available - Please Insert an SD card";
			sdCardToast = Toast.makeText(context, toasttext, toastduration);

			sdCardToast.show();

		}

		if (mExternalStorageWriteable) {
			toasttext = "External Storage is writable";
			sdCardToast = Toast.makeText(context, toasttext,
					toastduration - 100);
//			sdCardToast.show();

		} else {
			toasttext = "External Storage is NOT writable - There is something wrong with your SD card";
			sdCardToast = Toast.makeText(context, toasttext, toastduration);
			sdCardToast.show();

		}
	}


	/** Parse an XML file */
	public Document parseXMLFILE(String strURL) {
		URL url;
		URLConnection urlConn = null;

		try {
			url = new URL(strURL);
			urlConn = url.openConnection();

		} catch (IOException ioe) {
			Log.e("IIOOO Exception", "error occurred while creating xml file");

		}

		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(urlConn.getInputStream());
		} catch (IOException ioe) {

		} catch (ParserConfigurationException pce) {

		} catch (SAXException se) {

		}
		return doc;

	}

	private void loadContent() {

		// / load file

		

		List<String> fillProjectSpinner = CurrentProjectsList;
		fillProjectSpinner.add("Create New Project");

		// Load the Project Spinner
		projectspinner = (Spinner) findViewById(R.id.mProjSpinner);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, fillProjectSpinner);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		projectspinner.setAdapter(adapter);

	}

	public void setupButtons() {

		Button mbAdd;
		mbAdd = (Button) findViewById(R.id.mBadd);
		mbAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Do things
				// Go to Add footage activity
				Intent addFootintent = new Intent();
				addFootintent.setClassName(getApplicationContext(),
						"andy.documatic.FootageAnnotator");
				startActivity(addFootintent);

				///
			}
		});



		
		//Project Spinner!
		projectspinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						// your code here
						// Check New Project Choice
						String create = "Create New Project";
						if (projectspinner.getSelectedItem().equals(create)) {

							createNewProject();
//							Log.i("POOP", ""+projectspinner.getChildCount());
//							if(projectspinner.getChildCount()>1)
							projectspinner.setSelection(1);

						} else {
							setCurrentProject(projectspinner.getSelectedItem());
							loadSections();
						}

					}

					private void createNewProject() {

						// TODO Pop up a dialog to type in a new name
						final FrameLayout fl = new FrameLayout(context);

						final EditText input = new EditText(context);

						input.setGravity(Gravity.CENTER);

						fl.addView(input, new FrameLayout.LayoutParams(
								FrameLayout.LayoutParams.FILL_PARENT,
								FrameLayout.LayoutParams.WRAP_CONTENT));

						// input.setText("Preset Text");
						input.setHint("Type Name of New Project");

						AlertDialog newprojPopUp = new AlertDialog.Builder(
								Main.this).create();

						newprojPopUp.setView(fl);

						newprojPopUp.setTitle("Name your new project...");

						// Create Button
						newprojPopUp.setButton("Create",
								new DialogInterface.OnClickListener() {

									public void onClick(
											final DialogInterface dMAIN,
											int which) {

										final String newProjTitle = input.getText().toString().trim();
										final String newProjTitleFORDIRECTORIES = newProjTitle.replace(" ", "_");
										// Get this name, create a new folder,
								    	
								    	// Make MAIN file directories
										File newProjDirectory = new File("/sdcard/Documatic/"+newProjTitleFORDIRECTORIES);
										//newProjDirectory.mkdirs();
										File newProjManifest = new File(newProjDirectory,
												"projectmanifest__"+newProjTitleFORDIRECTORIES+".xml");

										// Find out if this project folder exists already
										if (!newProjDirectory.exists()) {
											newProjDirectory.mkdirs();
											try {
												newProjManifest.createNewFile();
												
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
												Log.e("cannot create file manifest", "nofile");
						                        Toast.makeText(context, "Cannot Create New File Manifest", Toast.LENGTH_LONG).show();

											}
											// we have to bind the new file with a FileOutputStream
											try {
												FileOutputStream  newprojmanifestFOS = new FileOutputStream(newProjManifest);
										    	writeNewDefaultProject(newProjTitle, newprojmanifestFOS);
										    	documaticVidDirectory =  new File("/sdcard/Documatic/"+newProjTitleFORDIRECTORIES+"/video/");

										    	documaticVidDirectory.mkdir();
												copyAssets(newProjTitleFORDIRECTORIES);

										    	
										    	

											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
												Log.e("FileNotFoundException", "can't create FileOutputStream");

											}
								    	
								    	   	//and re-run the load content function		
											checkAndLoadFiles();
											loadContent();
											loadSections();
									}
										else{ // Folder Already Exists! OVerwrite?
						                	//Project Already Exists!

						                		AlertDialog okOverwrite  = new AlertDialog.Builder(Main.this).create();
						                		okOverwrite.setTitle("Project "+newProjTitle+" Already Exists! Overwrite?");
						                		
						                		//OK OVERWRITE
						                		okOverwrite.setButton("Okay", new DialogInterface.OnClickListener(){
						           		    	 
						           		    	 
							    	                public void onClick(DialogInterface d, int which) {
							    	                	
							    	                	// Make MAIN file directories
														File newProjDirectory = new File("/sdcard/Documatic/"+newProjTitle);
														//newProjDirectory.mkdirs();

														File newProjManifest = new File(newProjDirectory,
																"projectmanifest__"+newProjTitleFORDIRECTORIES+".xml");
							    	                	
						    	                        Toast.makeText(context, "Overwritten", Toast.LENGTH_LONG).show();
						    	                        newProjDirectory.mkdirs();
														try {
															newProjManifest.createNewFile();
															documaticVidDirectory =  new File("/sdcard/Documatic/"+newProjTitle+"/video/");

															documaticVidDirectory.mkdir();
															copyAssets(newProjTitle);
														} catch (IOException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
															Log.e("cannot create file manifest", "nofile");
									                        Toast.makeText(context, "Can't Create Manifest", Toast.LENGTH_LONG).show();

														}
														// we have to bind the new file with a FileOutputStream
														try {
															FileOutputStream  newprojmanifestFOS = new FileOutputStream(newProjManifest);
													    	writeNewDefaultProject(newProjTitle, newprojmanifestFOS);

														} catch (IOException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
															Log.e("FileNotFoundException", "can't create FileOutputStream");

														}
														//and re-run the load content function		
														setCurrentProject(newProjTitle);

														checkAndLoadFiles();
														loadContent();	
														loadSections();
							    	                }
							    	                });
						                		
						                		//Cancel-RENAME
						                		okOverwrite.setButton2("Cancel", new DialogInterface.OnClickListener(){
							           		    	 
							           		    	 
							    	                public void onClick(DialogInterface d, int which) {
						    	                        Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show();

//							    	                	d.dismiss();
//							    	                	dMAIN.dismiss();

							    	                	}
							    	                });
						                		okOverwrite.show();
							    	                
						                	
											
										}

										
										
//									
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
						input.requestFocus();
						newprojPopUp
								.getWindow()
								.setSoftInputMode(
										WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

	
					}

					@Override
					public void onNothingSelected(AdapterView<?> parentView) {
						// your code here
						
						String create = "Create New Project";
						if (projectspinner.getSelectedItem().equals(create)) {

							createNewProject();

						}
						
					}

				});
// end project spinner
		
		
		// Add sections
		ImageButton bAddSections;
    	bAddSections = (ImageButton) findViewById(R.id.mAddSection);
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
				                
				                if(Main.currentProjParsed.getElementsByTagName("section").item(0)!=null){
					                Main.currentProjParsed.getElementsByTagName("sections").item(0).insertBefore(newSectionEl, Main.currentProjParsed.getElementsByTagName("sections").item(0)
					                		.getLastChild()) ;
					                }
					                else{
					                	 Main.currentProjParsed.getElementsByTagName("sections").item(0).appendChild(newSectionEl);
					                }
				                
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

	private void writeNewDefaultProject(String projName, FileOutputStream projFOS) {
		// TODO Auto-generated method stub
		// / methods learned from
		// http://www.anddev.org/write_a_simple_xml_file_in_the_sd_card_using_xmlserializer-t8350.html

		// we create a XmlSerializer in order to write xml data
		manifestSerializer = Xml.newSerializer();

		try {
			// we set the FileOutputStream as output for the serializer, usin
			// UTF-8 encoding
			manifestSerializer.setOutput(projFOS, "UTF-8");
			// Write <?xml declaration with encoding (if encoding not null) and
			// standalone flag (if standalone not null)
			manifestSerializer.startDocument(null, Boolean.valueOf(true));
			// set indentation option
//			manifestSerializer.setFeature(
//					"http://xmlpull.org/v1/doc/features.html#indent-output",
//					true);
			// start a tag called "root"
			manifestSerializer.startTag(null, "ProjectManifest");
			// i indent code just to have a view similar to xml-tree
			manifestSerializer.attribute(null, "name", projName);
			manifestSerializer.startTag(null, "sections");
			manifestSerializer.startTag(null, "section");
			// set an attribute called "attribute" with a "value" for <child2>
			manifestSerializer.attribute(null, "name", "Introduction");
		manifestSerializer.endTag(null, "section");

			manifestSerializer.startTag(null, "section");
			// set an attribute called "attribute" with a "value" for <child2>
			manifestSerializer.attribute(null, "name","Ending");
			
			manifestSerializer.endTag(null, "section");

			manifestSerializer.endTag(null, "sections");

			manifestSerializer.startTag(null, "people");
			
			manifestSerializer.endTag(null, "people");


			manifestSerializer.endTag(null, "ProjectManifest");
			manifestSerializer.endDocument();
			// write xml data into the FileOutputStream
			manifestSerializer.flush();
			// finally we close the file stream
			manifestFOS.close();

			toasttext = "New Project File Written";
			sdCardToast = Toast.makeText(context, toasttext, toastduration);

			sdCardToast.show();

		} catch (Exception e) {
			Log.e("Exception", "error occurred while creating xml file");
		}
	}
	public static void writeCurrentDocumenttoXML(){
	 	// Make MAIN file directories
		File thisProjDirectory = new File("/sdcard/Documatic/"+CurrentProjectFORDIRECTORIES);
		//newProjDirectory.mkdirs();
		File thisProjManifest = new File(thisProjDirectory,
				"projectmanifest__"+CurrentProjectFORDIRECTORIES+".xml");    	
       
		// we have to bind the new file with a FileOutputStream
		try {
			FileOutputStream  fOut = new FileOutputStream(thisProjManifest);
			 //write the content into xml file

            OutputStreamWriter osw = new OutputStreamWriter(fOut); 
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(Main.currentProjParsed);
            StreamResult result =  new StreamResult(thisProjManifest);
            
//            transformer.setOutputProperty
//            ("{http://xml.apache.org/xslt}indent-amount", "4");
//         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            
            transformer.transform(source, result);

            osw.flush();
            osw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch blo
			e.printStackTrace();
			Log.e("FileNotFoundException", "can't create FileOutputStream");

		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	

	
	private void copyAssets(String projName) {
	    AssetManager assetManager = getAssets();
        Log.i("fart", "WHAT");

	        InputStream in = null;
	        OutputStream out = null;
	        try {
	          in = assetManager.open("Generate_Project.jar");
	          out = new FileOutputStream("/sdcard/Documatic/"+projName+"/" + "Generate_Project.jar");
	          copyFile(in, out);
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
	        } catch(Exception e) {
	            Log.e("DID NOT Copied Project Generator", e.getMessage());
	        }     
	        
	        try {
		          in = assetManager.open("Readme.txt");
		          out = new FileOutputStream("/sdcard/Documatic/Readme.txt");
		          copyFile(in, out);
		          in.close();
		          in = null;
		          out.flush();
		          out.close();
		          out = null;
		        } catch(Exception e) {
		            Log.e("DID NOT Copied Readme", e.getMessage());
		        }  
	        
	    }
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	




private void loadSections() {
	// TODO Auto-generated method stub
	Log.i(START, "Load Sections!");
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
     
    lv.setAdapter(new DragNDropAdapter(this, new int[]{R.layout.dragitem}, new int[]{R.id.TextView01}, (ArrayList<String>) locCurrentProjSections,cExh, cNar,cInt));//new DragNDropAdapter(this,content)
    ListView listView = lv;
    
    if (listView instanceof DragNDropListView) {
    	((DragNDropListView) listView).setDropListener(mDropListener);
    	((DragNDropListView) listView).setRemoveListener(mRemoveListener);
    	((DragNDropListView) listView).setDragListener(mDragListener);
    	// have longclick to remove a section
    	lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
    		
    		 @Override 
             public boolean onItemLongClick(AdapterView<?> av, View v, final int 
pos, long id) { 
    				((View) v.getParent()).performHapticFeedback(1, 2);

    			 String dername= ((Element) currentProjParsed.getElementsByTagName("section").item(pos)).getAttribute(NAME);
    		      Log.i( "TAG", "onLongListItemClick id=" + id + " Name "+dername ); 
    		      AlertDialog removeSection = new AlertDialog.Builder(edContext).create();
    		  removeSection.setTitle(dername);
    		  removeSection.setMessage("Permanently remove the section, "+dername.toUpperCase()+", and all of its clips?");
    		      // Remove Button
    				removeSection.setButton("Remove",
    						new DialogInterface.OnClickListener() {

    							@Override
    							public void onClick(DialogInterface d,
    									int which) {
    								//Remove the offending section
    								currentProjParsed.getElementsByTagName("sections").item(0).removeChild(currentProjParsed.getElementsByTagName("section").item(pos));
//    								save and reload
    			    				Main.writeCurrentDocumenttoXML();
    			    				loadSections();
    								
    								
    								// d.dismiss();
    							}
    						});
    				
    		      // Cancel Button
  				removeSection.setButton2("Cancel",
  						new DialogInterface.OnClickListener() {

  							@Override
  							public void onClick(DialogInterface d,
  									int which) {

  								// d.dismiss();

  							}

  						});
  				
  				
  				removeSection.show();
  				
                    return false; 
     } 
    		
    		
		});

    }
 
	
	
}




private DropListener mDropListener = 
	new DropListener() {
    public void onDrop(int from, int to) {
    	ListAdapter adapter = lv.getAdapter();
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
//     			rootSections.replaceChild(a, b);
     			

    				Log.i("yo", "it wants to swap from="+from+" to="+to);
    				Main.writeCurrentDocumenttoXML();
    				
    			

    		}
    		
    		
    		
    		lv.invalidateViews();
    	}
    }
};

private RemoveListener mRemoveListener =
    new RemoveListener() {
    public void onRemove(int which) {
    	ListAdapter adapter = lv.getAdapter();
    	if (adapter instanceof DragNDropAdapter) {
    		((DragNDropAdapter)adapter).onRemove(which);
    		lv.invalidateViews();
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





@Override
public void onResume(){
loadSections();
	super.onResume();
	
}

}