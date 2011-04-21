package andy;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class FinalCutProWrite {
	
	public final static  String START = "start";
	public final static  String END = "end";
	public final static  String DURATION = "duration";
	
	public final static  String CLIP = "clip";
	public static  final String SECTIONS = "sections";
	static public final String SECTION = "section";
	static public final String PEOPLE = "people";
	static public final String PERSON = "person";
	protected static final String PERSONTITLE = "title";
	public final static String TYPE = "type";
	
	protected static final String NARRATION = "narration";
	protected static final String NAME = "name";
	public final static String EXHIBIT = "exhibit";
	public final static  String INTERVIEW = "interview";
	private static final String LINK = "link";
	private static final String MAIN_TIT_VIDEO = "MAIN_TIT_VIDEO";
	private static final String MAIN_EXH_VIDEO = "MAIN_EXH_VIDEO";
	private static final String MAIN_INT_VIDEO = "MAIN_INT_VIDEO";
	private static final String MAIN_NAR_VIDEO = "MAIN_NAR_VIDEO";
	
	private static final String MAIN_EXH_AUDIO_A = "MAIN_EXH_AUDIO_A";
	private static final String MAIN_EXH_AUDIO_B = "MAIN_EXH_AUDIO_B";
	private static final String MAIN_INT_AUDIO_A = "MAIN_INT_AUDIO_A";
	private static final String MAIN_INT_AUDIO_B = "MAIN_INT_AUDIO_B";
	private static final String MAIN_NAR_AUDIO_A = "MAIN_NAR_AUDIO_A";
	private static final String MAIN_NAR_AUDIO_B = "MAIN_NAR_AUDIO_B";
	@SuppressWarnings("unused")
	private static final String TITLE = "title";

	static Document currentProjParsed;
	static Document finalCutProProject;
	static Element currentSection;
	static NodeList allPeople;
	static Element allPeopleToAddTitles;

	public static Long previousMarkerPosition;

	static List <File> allVidFiles;
	static List <File> unusedVidFiles;
	static Long timelineposition;
	static int clipGroupCounter;
	
	static int lastMainClipLength;

	static int exhibitLayerLength;
	static int clipcounterEXH=0;
	static int clipcounterINT=0;
	static int clipcounterNAR=0;
	
	static int PROJECTFRAMERATE;
static boolean addClipTitle;
	File vidDirectory;
	static String[] allManifestDirectories;

	public static IContainer container;
	public static IStream stream;
	public static List <Element> videofileattributes;
	static Frame frame;
	static TextArea textArea;
	
	/***
	 * START
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
timelineposition= (long) 0;
clipGroupCounter=1;
PROJECTFRAMERATE=30;//frames per second
addClipTitle=false;
//String[] allManifestDirectories= new String[];
frame=new Frame("Text Frame");
frame.setTitle("Documatic Project Compiler");
textArea=new TextArea("Welcome to Documatic",40,80);
frame.add(textArea);

frame.setLayout(new FlowLayout());
frame.setSize(600,710);
frame.setVisible(true);

frame.addWindowListener(new WindowAdapter(){
    public void windowClosing(WindowEvent e){
    System.exit(0);
    }
    });



		File BaseDirectory = new File(".");
		FilenameFilter fnf = new FilenameFilter() {
		@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.startsWith("projectmanifest");
			}
		};

		//Search for other projectmanifest files that should be merged with
	allManifestDirectories=	BaseDirectory.list(fnf);
	for(int r=0; r<allManifestDirectories.length;r++){
		print(allManifestDirectories[r]);
	}
		
	print("\n");
	//Location of our videos
		File vidDirectory = new File ("video/");
		print(""+vidDirectory.exists());
		allVidFiles = new ArrayList<File>(
				Arrays.asList(vidDirectory.listFiles()));
		unusedVidFiles=new ArrayList<File>(
				Arrays.asList(vidDirectory.listFiles()));
		// Parse the main xml manifest: currentProjParsed
		
		//get primary manifestFile
		
		File projectManifestFile = new File(allManifestDirectories[0]);// This is the primary manifest file
		
		try {
			currentProjParsed = parseXMLFILE(projectManifestFile.toURL()
					.toString());
		} catch (MalformedURLException e) {
			print("malformedURL");
		}
		/***********************************/
		
		
		

		if (allManifestDirectories.length > 0) {
			
			for(int r=0; r<allManifestDirectories.length;r++){
System.out.print("Hey");
				File importedNodeFile = new File(allManifestDirectories[r]);
				Document importedProjParsed=null;
				try {
					importedProjParsed = parseXMLFILE(importedNodeFile.toURL().toString());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				currentProjParsed.normalize();
				importedProjParsed.normalize();
				
				Node mainroot = currentProjParsed.getDocumentElement();
				Node mainIMPORTroot = importedProjParsed.getDocumentElement();
				
				print("isequal"+mainroot.isEqualNode(mainIMPORTroot) );
				if(!allManifestDirectories[r].equals("projectmanifest.xml") ){//not just our main manifestfile
					//See if there are any new sections in the extra document
					Node mainSectionParent = currentProjParsed.getElementsByTagName(SECTIONS).item(0);
					NodeList csections = currentProjParsed.getElementsByTagName("section");
					NodeList isections =importedProjParsed.getElementsByTagName("section");
					for (int i = 0; i < isections.getLength(); i++) {
						boolean amatch=false;
						for (int j = 0; j < csections.getLength(); j++) {
							if(
									((Element)isections.item(i)).getAttribute(NAME)
									.equals(
											((Element) csections.item(j)).getAttribute(NAME))){
								amatch=true;
								//append all the child clips of this section to the corresponding main section
								NodeList theclips = isections.item(i).getChildNodes();
								
								
								
								System.out.println("Total RAW nodes to import = "+theclips.getLength());
								
								// Get rid of fake clips
								for (int tj = 0; tj < theclips.getLength(); tj++) {
									if (theclips.item(tj).getNodeType() == Node.TEXT_NODE) {
										// Ignore Text Nodes posing as clips
										theclips.item(tj).getParentNode().removeChild(theclips.item(tj));
									}
								}
								System.out.println("Total nodes to import = "+theclips.getLength());

								for(int q=0;q<theclips.getLength();q++){
								Node kid = currentProjParsed.importNode(theclips.item(q),false); 
								csections.item(j).appendChild(kid); 
								System.out.println("Appending from sec "+((Element)isections.item(i)).getAttribute(NAME));

//								isections.item(i).removeChild(theclips.item(q));
								}
							}
						}
						if(amatch==false){
							System.out.println("NEW SECTION ADD "+((Element)isections.item(i)).getAttribute(NAME));
//							mainSectionParent.
							Node kid = currentProjParsed.importNode(isections.item(i),true); 
							mainSectionParent.appendChild(kid); 

						}
						
					}
					//Add people
					Node mainPeopleParent = currentProjParsed.getElementsByTagName(PEOPLE).item(0);
					NodeList cpersons = currentProjParsed.getElementsByTagName("person");
					NodeList ipersons =importedProjParsed.getElementsByTagName("person");
					for (int i = 0; i < ipersons.getLength(); i++) {
						boolean amatch=false;
						for (int j = 0; j < cpersons.getLength(); j++) {
							if(
									((Element)ipersons.item(i)).getAttribute(NAME)
									.equals(
											((Element) cpersons.item(j)).getAttribute(NAME))){
								amatch=true;
							}
						}
						if(amatch==false){
							System.out.println("NEW PERSON ADD "+((Element) ipersons.item(i)).getAttribute(NAME));
							Node kid = currentProjParsed.importNode(ipersons.item(i),true); 
							mainPeopleParent.appendChild(kid); 

						}
					}
					
					

							}

				
			

				}
					  

	
		}
		
		currentProjParsed.normalize();
		try {
			printXML(currentProjParsed);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		prepFinalCutDoc();
		
		allPeople= currentProjParsed.getElementsByTagName("person");
		for(int p=0; p<allPeople.getLength(); p++){
			print("All People: "+((Element) allPeople.item(p)).getAttribute(NAME));
		}
		
		
		//Gather Stats on all video files in video directory:
		getVideoStats();
		
		
		
		/**
		 * After the document is parsed, 
		 * 1 go to a section 
		 * 2 decide on a clip to write 
		 * 3 find that clip's corresponding video file 
		 * 4 write to finalcutproproject *
		 */

		/*
		 * 1
		 */

		// get the individual sections
		NodeList sections = currentProjParsed.getElementsByTagName("section");
		for (int i = 0; i < sections.getLength(); i++) {
			print("\n");
			print(((Element) sections.item(i)).getAttribute("name"));
			print("\n ----------------------");

			currentSection = (Element) sections.item(i);
			// Add a sequence marker for each section
			
			if(previousMarkerPosition!=timelineposition){
			addSequenceMarker(currentSection.getAttribute("name"),
					timelineposition);
			previousMarkerPosition=timelineposition;
			}
			
			// get the clips from that section
			NodeList clips = sections.item(i).getChildNodes();
			print("number of RAW clips " + clips.getLength());
			
			
			//Cleanup
			// Get rid of fake clips
			for (int j = 0; j < clips.getLength(); j++) {
				if (clips.item(j).getNodeType() == Node.TEXT_NODE) {
					// Ignore Text Nodes posing as clips
					clips.item(j).getParentNode().removeChild(clips.item(j));
				}
			}

			/*
			 * 2
			 */

			print("number of actual clips " + clips.getLength());
			// A) Find narration to introduce the section
			int narcount=0;


			for (int j = 0; j < clips.getLength(); j++) {
				Element clip = (Element) clips.item(j);

				if (clip.getAttribute(TYPE).equals(NARRATION)) {
					addClipToFinalCut(clip, addClipTitle);
					
					narcount++;
					print("num of narrations= "+narcount);

					
//					clip.getParentNode().removeChild(clip);

					// Search for exhibit video that may be related to this narration clip
					for (int e = 0; e < clips.getLength(); e++) {
						Element eClip =(Element) clips.item(e);
						if (eClip.getAttribute(TYPE).equals(EXHIBIT)&&eClip.getAttribute(LINK).equals("true")) {
							print("linked NARR AVaIL");
//							eClip.setAttribute(TYPE, "lExhibit");
							addClipToFinalCut(eClip, addClipTitle);
							eClip.setAttribute(TYPE, "USED");

						}

					}
					clip.setAttribute(TYPE, "USED");
//					clip.getAttributes().getNamedItem(TYPE).setNodeValue("USED");


				}
			}
			print("number of leftover clips after NARR " + clips.getLength());
			
			// B) Find Interviews to introduce the section
			int incount=0;

			for (int j = 0; j < clips.getLength(); j++) {
				Element clip = (Element) clips.item(j);

				if (clip.getAttribute(TYPE).equals(INTERVIEW)) {
					incount++;
					print("number of interviews= "+incount);
					
					//Add title for person
					for(int p=0; p<allPeople.getLength(); p++){

						if(clip.getAttribute(PERSON).equals(((Element) allPeople.item(p)).getAttribute(NAME))){
							//add title text
							addClipTitle=true;
							allPeople.item(p).getParentNode().removeChild(allPeople.item(p));
							}

					}

					
					addClipToFinalCut(clip, addClipTitle);
					addClipTitle=false;
//					clip.getParentNode().removeChild(clip);

					// Search for exhibit video that may be related to this Person in the clip
					for (int e = 0; e < clips.getLength(); e++) {
						Element eClip =(Element) clips.item(e);
						if (eClip.getAttribute(TYPE).equals(EXHIBIT)&&eClip.getAttribute(PERSON).equals(clip.getAttribute(PERSON))) {
							print("linked EXHIBIT AVaIL for person "+clip.getAttribute(PERSON));
//							eClip.setAttribute(TYPE, "lExhibit");
							addClipToFinalCut(eClip, addClipTitle);
							eClip.setAttribute(TYPE, "USED");
						}

					}
//					((Node) clips).removeChild(clips.item(j));
					clip.setAttribute(TYPE, "USED");

				}
			}
			print("number of leftover clips after INT " + clips.getLength());
			
			// C) Add on any exhibit video left over

			for (int j = 0; j < clips.getLength(); j++) {
				Element clip = (Element) clips.item(j);
				print(clip.getAttribute(TYPE));
				if (clip.getAttribute(TYPE).equals(EXHIBIT)) {
					
			
					addClipToFinalCut(clip, addClipTitle);
//					clip.getParentNode().removeChild(clip);
//					((Node) clips).removeChild(clip);

					
				}
			}
			print("number of leftover clips after EXH " + clips.getLength());
			
			
			
			
		}

		// After all clips have been used, check to see if there is additional
		// unused media in the folder
		for (int t = 0; t < unusedVidFiles.size(); t++) {
			print("Did not use: " + unusedVidFiles.get(t).getPath());
		}


		
		writeCurrentDocumenttoXML(currentProjParsed.getDocumentElement().getAttribute("name"));
		print("\n*************************** \nSuccessfully Created Sequence!\n\n***************************");
		
		Timer timer;
		timer= new Timer();
		timer.schedule(  new  TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toolkit toolkit = Toolkit.getDefaultToolkit();

			      toolkit.beep();
			      System.exit(0); //Stops everything
			}
			
		}
		
		, 3000);
		
	}

	
	private static void getVideoStats() {

		
		long videoStart=0;
		long videoEnd=0;
		long videoDuration=0;

		int vidFrameRate = 0;

		String vidDUR = null;
		
		videofileattributes = new ArrayList<Element>();
		// Build list of videoFIle Atrtributes Cycle through all videos to get properties of all files
		for (int p = 0; p < allVidFiles.size(); p++) {
			File currentVidFile = allVidFiles.get(p);
			String vidfilename = allVidFiles.get(p).getAbsolutePath();
			// print(vidfilename);
			
		
			// Create a Xuggler container object
			container = IContainer.make();
			// Open up the container
			if (container.open(vidfilename, IContainer.Type.READ, null) < 0) {
				// Skip it, because it probably isn't a video file, just
				// detritus
				unusedVidFiles.remove(currentVidFile);

			} else {
				// Pull out the info you want
				
				// query how many streams the call to open found
				int numStreams = container.getNumStreams();

				String vidSampleRate = null;
				String vidWidth = null;
				String vidHeight = null;
				String vidChannels = null;
				// and iterate through the streams to print their meta data
				for (int i = 0; i < numStreams; i++) {
					// Find the stream object
					stream = container.getStream(i);
					// Get the pre-configured decoder that can decode this
					// stream;
					IStreamCoder coder = stream.getStreamCoder();
					if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
						vidSampleRate = "" + coder.getSampleRate();
						vidChannels = "" + coder.getChannels();
						System.out.printf("format: %s", coder.getSampleFormat());
						
	
					} 
					else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) // The
																				// video
																				// stream
																				// duration
																				// is
																				// the
																				// only
																				// reliable
																				// method
					{

						vidFrameRate = (int) Math.round(coder.getFrameRate()
								.getDouble());
						vidDUR = stream.getDuration() == Global.NO_PTS ? "unknown"
								: "" + stream.getDuration() / vidFrameRate;
						
						vidWidth = "" + coder.getWidth();
						vidHeight = "" + coder.getHeight();
						System.out.printf("format: %s; ", coder.getPixelType());
						
						
					}
//					stream.delete();
				}
			
				videoEnd = currentVidFile.lastModified();
				videoDuration = container.getDuration() / vidFrameRate;
				videoStart = videoEnd - Long.parseLong(vidDUR);
			
				videoEnd = videoEnd * PROJECTFRAMERATE / 1000;
				videoDuration = videoDuration * PROJECTFRAMERATE / 1000;
				videoStart = videoStart * PROJECTFRAMERATE / 1000;
				
				Element vidfileInfo=currentProjParsed.createElement("vidfileInfo");
				vidfileInfo.setAttribute(NAME,vidfilename);
				vidfileInfo.setAttribute(DURATION,""+ videoDuration);
				vidfileInfo.setAttribute(START,""+ videoStart);
				vidfileInfo.setAttribute(END, ""+videoEnd);
				vidfileInfo.setAttribute("FrameRate", ""+vidFrameRate);
				vidfileInfo.setAttribute("Width", ""+vidWidth);
				vidfileInfo.setAttribute("Height", ""+vidHeight);
				vidfileInfo.setAttribute("SampleRate", ""+vidSampleRate);
				vidfileInfo.setAttribute("Channels", ""+vidChannels);



				videofileattributes.add(vidfileInfo);
				
				
				

			}
			
			
		
		}
		
	}


	private static void addSequenceMarker(String name, long position) {
			// TODO Auto-generated method stub
			
			NodeList sequences = finalCutProProject.getElementsByTagName("sequence");
			for (int i = 0; i < sequences.getLength(); i++) {
				if(((Element) sequences.item(i)).getAttribute("id").equals("main_sequence")){
					Element sequence =(Element) sequences.item(i);
					Element marker =finalCutProProject.createElement("marker");
						
						Element toad =finalCutProProject.createElement("name");
						toad.setTextContent(name);
						marker.appendChild(toad);
						
						toad =finalCutProProject.createElement("in");
							toad.setTextContent(Long.toString(position));
							marker.appendChild(toad);
							
							 toad =finalCutProProject.createElement("out");
								toad.setTextContent("-1");
								marker.appendChild(toad);
								
								toad =finalCutProProject.createElement("comment");
								toad.setTextContent("This is a comment");
								marker.appendChild(toad);
								
								sequence.appendChild(marker);
					
	i=i+10000;//only do this for one sequence probably
				}
				
			}
			
			
					
			
			
			
		}


	@SuppressWarnings("unused")
	private static void addClipToFinalCut(Element clip, boolean addTitle) {

		/**
		 * 3 find video file
		 */
		// Compare this clip to all the videos, and get an appropriate one
	
		long clipStart = Long.parseLong(clip.getAttribute("start"))
				* PROJECTFRAMERATE / 1000;

		long clipDurationFrames = Long.parseLong(clip.getAttribute("duration"))
				* PROJECTFRAMERATE / 1000; // Convert from milliseconds to
											// frames 30fps timebase

		long clipEnd = Long.parseLong(clip.getAttribute("end"))
				* PROJECTFRAMERATE / 1000;
		long videoStart=0;
		long videoEnd=0;
		long videoDuration=0;

		int vidFrameRate = 0;

		String vidDUR = null;

		/**
		 * 3 find video file
		 */
		
		//look for video files that we already got info from
		for(int vf=0;vf<videofileattributes.size();vf++){
			
			videoStart=Long.parseLong(videofileattributes.get(vf).getAttribute(START));
			videoEnd=Long.parseLong(videofileattributes.get(vf).getAttribute(END));
			videoDuration=Long.parseLong(videofileattributes.get(vf).getAttribute(DURATION));

			
			
			if (clipStart > videoStart && clipStart < videoEnd
					|| clipEnd > videoStart && clipEnd < videoEnd) {
				print("Match!" + videofileattributes.get(vf).getAttribute(NAME));

				// Clip was correctly matched to a video file
				// so remove it from the list of unused media
//				unusedVidFiles.remove(currentVidFile);

				/**
				 * Found Matching VideoFile
				 */
				File currentVideoFile = new File(videofileattributes.get(vf).getAttribute(NAME));
				// We have a clip and its associated media, now Add those clips to the finalcutpro
				makeFinalCutClip(clip, currentVideoFile,videofileattributes.get(vf), addTitle);

				container.delete();
			}
			else{
				/**
				 * No matching video file :-(
				 */
				//clip.setAttribute(TYPE, "USED_NoFILE");

				
			}
			
			
		}
		
		
		
					
		
	}




/**
 * This takes in a clip and a video file (which were previously determined to be linked) and makes the appropriate nodes
 * @param clip 
 * @param currentVidFile  The video file
 * @param videoFileelement 
 * @param addTitle 
 * @param container - The video file (as a xuggle container object)
 */
	@SuppressWarnings("unused")
	private static void makeFinalCutClip(Element clip, File currentVidFile, Element videoFileelement, boolean addTitle ) {
		

		
		/**
		 * Collect all the stats about the matching video file
		 */
String type = clip.getAttribute(TYPE);
String videoType = MAIN_NAR_VIDEO;
String audioTypeA = MAIN_NAR_AUDIO_A;
String audioTypeB = MAIN_NAR_AUDIO_B;
Boolean linkedExhibit=false;
int vTrackindex=1;
int aaTrackindex=1;
int abTrackindex=2;
int clipindexcounter=0;




if(type.equals(EXHIBIT)){
videoType= MAIN_EXH_VIDEO;
audioTypeA=MAIN_EXH_AUDIO_A;
audioTypeB=MAIN_EXH_AUDIO_B;
 vTrackindex=3;
aaTrackindex=5;
abTrackindex=6;
clipcounterEXH++;
clipindexcounter=clipcounterEXH;

}

if(type.equals("linkedExhibit")){
	videoType= MAIN_EXH_VIDEO;
	audioTypeA=MAIN_EXH_AUDIO_A;
	audioTypeB=MAIN_EXH_AUDIO_B;
linkedExhibit=true;
vTrackindex=3;
aaTrackindex=5;
abTrackindex=6;
clipcounterEXH++;
clipindexcounter=clipcounterEXH;

	}

if(type.equals(INTERVIEW)){
	videoType= MAIN_INT_VIDEO;
	audioTypeA=MAIN_INT_AUDIO_A;
	audioTypeB=MAIN_INT_AUDIO_B;
	vTrackindex=2;
	aaTrackindex=3;
	abTrackindex=4;
	clipcounterINT++;
	clipindexcounter=clipcounterINT;

	}
if(type.equals(NARRATION)){
	videoType= MAIN_NAR_VIDEO;
	audioTypeA=MAIN_NAR_AUDIO_A;
	audioTypeB=MAIN_NAR_AUDIO_B;
	vTrackindex=1;
	aaTrackindex=1;
	abTrackindex=2;
	clipcounterNAR++;
	clipindexcounter=clipcounterNAR;

	}

		long clipStart = Long.parseLong(clip.getAttribute("start"))
				* PROJECTFRAMERATE / 1000;

		long clipDurationFrames = Long.parseLong(clip.getAttribute("duration"))
				* PROJECTFRAMERATE / 1000; // Convert from milliseconds to
											// frames 30fps timebase

		long clipEnd = Long.parseLong(clip.getAttribute("end"))
				* PROJECTFRAMERATE / 1000;

		long videoStart;
		long videoEnd;
		long videoDuration;

		videoStart=Long.parseLong(videoFileelement.getAttribute(START));
		videoEnd=Long.parseLong(videoFileelement.getAttribute(END));
		videoDuration=Long.parseLong(videoFileelement.getAttribute(DURATION));
		
		String vidChannels = videoFileelement.getAttribute("Channels");
		String vidWidth = videoFileelement.getAttribute("Width");
		String vidHeight = videoFileelement.getAttribute("Height");
		int vidFrameRate = 		Integer.parseInt(videoFileelement.getAttribute("FrameRate"));

		String vidSampleRate = videoFileelement.getAttribute("SampleRate");

		String vidfilename = currentVidFile.getAbsolutePath();


			/**
			 * Gathered all the stats about the matching video file
			 */
	
	
	
		
		//Write the clip using the gathered data
		Element clipitem = finalCutProProject.createElement("clipitem");
		clipitem.setAttribute("id", "vid-"+clipGroupCounter); //TODO make this proper
			
			//Stupid elements i don't need to mess with
			Element toad =finalCutProProject.createElement("enabled");
			toad.setTextContent(currentSection.getAttribute("TRUE"));
			clipitem.appendChild(toad);

			//Items to change
			Element clipname =finalCutProProject.createElement("name");
			clipname.setTextContent(currentSection.getAttribute("name")+" "+clip.getAttribute("type")+" "+clip.getAttribute("person"));
			clipitem.appendChild(clipname);
			
			toad =finalCutProProject.createElement("duration");
			toad.setTextContent(Long.toString(clipDurationFrames));
			clipitem.appendChild(toad);
			
			toad =finalCutProProject.createElement("start");
			toad.setTextContent(Long.toString(timelineposition));
			clipitem.appendChild(toad);
			
			toad =finalCutProProject.createElement("end");
			toad.setTextContent(Long.toString(timelineposition+clipDurationFrames));
			clipitem.appendChild(toad);
			
			toad =finalCutProProject.createElement("in");
			toad.setTextContent(Long.toString(clipStart -videoStart));
			clipitem.appendChild(toad);
			toad =finalCutProProject.createElement("out");
			toad.setTextContent(Long.toString((clipStart - videoStart)+clipDurationFrames));
			clipitem.appendChild(toad);
			
			//File of the clip
			
			Element file = finalCutProProject.createElement("file");
			file.setAttribute("id", "filevid-"+clipGroupCounter);//TODO make this proper
				toad =finalCutProProject.createElement("name");
				toad.setTextContent(currentSection.getAttribute("name")+" "+clip.getAttribute("type")+" "+clip.getAttribute("person"));
				file.appendChild(toad);
			
				toad =finalCutProProject.createElement("pathurl");
				toad.setTextContent(vidfilename);
				file.appendChild(toad);
				
				toad =finalCutProProject.createElement("rate");
					Element timebase =finalCutProProject.createElement("timebase");
					timebase.setTextContent(Integer.toString(vidFrameRate));
					toad.appendChild(timebase);
					Element ntsc =finalCutProProject.createElement("ntsc");
					ntsc.setTextContent("FALSE");
					toad.appendChild(ntsc);
				file.appendChild(toad);
				toad =finalCutProProject.createElement("duration");
				toad.setTextContent(Long.toString(videoDuration));
				file.appendChild(toad);
				
				Element media =finalCutProProject.createElement("media");
					Element video = finalCutProProject.createElement("video");
						Element duration =finalCutProProject.createElement("duration");
						duration.setTextContent(Long.toString(videoDuration));
						video.appendChild(duration);
						Element samplechar =finalCutProProject.createElement("samplecharacteristics");
						toad =finalCutProProject.createElement("rate");
							timebase =finalCutProProject.createElement("timebase");
							timebase.setTextContent(Integer.toString(vidFrameRate));
						toad.appendChild(timebase);
						ntsc =finalCutProProject.createElement("ntsc");
						ntsc.setTextContent("FALSE");
						toad.appendChild(ntsc);
						samplechar.appendChild(toad);
						
						toad =finalCutProProject.createElement("width");
						toad.setTextContent(vidWidth);
						samplechar.appendChild(toad);
						
						toad =finalCutProProject.createElement("height");
						toad.setTextContent(vidHeight);
						samplechar.appendChild(toad);
						
					video.appendChild(samplechar);
					media.appendChild(video);
					Element audio = finalCutProProject.createElement("audio");
						samplechar =finalCutProProject.createElement("samplecharacteristics");
						toad =finalCutProProject.createElement("depth");
						toad.setTextContent("16");
						samplechar.appendChild(toad);
						
						toad =finalCutProProject.createElement("samplerate");
						toad.setTextContent(vidSampleRate);
						samplechar.appendChild(toad);
						
						audio.appendChild(samplechar);
						
						toad =finalCutProProject.createElement("channelcount");
						toad.setTextContent(vidChannels);
						audio.appendChild(toad);
						
					media.appendChild(audio);
					
				file.appendChild(media);
				
			clipitem.appendChild(file);
			//add links
			Element link = finalCutProProject.createElement("link");
				toad =finalCutProProject.createElement("linkclipref");
				toad.setTextContent("vid-"+clipGroupCounter);
				link.appendChild(toad);
				
				toad =finalCutProProject.createElement("mediatype");
				toad.setTextContent("video");
				link.appendChild(toad);
				toad =finalCutProProject.createElement("trackindex");
				toad.setTextContent(""+vTrackindex);
				link.appendChild(toad);
				toad =finalCutProProject.createElement("clipindex");
				toad.setTextContent(""+clipindexcounter);
				link.appendChild(toad);
				toad =finalCutProProject.createElement("groupindex");
				toad.setTextContent("1");
				link.appendChild(toad);
			clipitem.appendChild(link);
			link = finalCutProProject.createElement("link");
				toad =finalCutProProject.createElement("mediatype");
				toad.setTextContent("audio");
				link.appendChild(toad);
				toad =finalCutProProject.createElement("trackindex");
				toad.setTextContent(""+aaTrackindex);
				link.appendChild(toad);
				toad =finalCutProProject.createElement("clipindex");
				toad.setTextContent(""+clipindexcounter);
				link.appendChild(toad);
				toad =finalCutProProject.createElement("groupindex");
				toad.setTextContent("1");
				link.appendChild(toad);
			clipitem.appendChild(link);
			link = finalCutProProject.createElement("link");
				
				toad =finalCutProProject.createElement("mediatype");
				toad.setTextContent("audio");
				link.appendChild(toad);
				toad =finalCutProProject.createElement("trackindex");
				toad.setTextContent(""+abTrackindex);
				link.appendChild(toad);
				toad =finalCutProProject.createElement("clipindex");
				toad.setTextContent(""+clipindexcounter);
				link.appendChild(toad);
				toad =finalCutProProject.createElement("groupindex");
				toad.setTextContent("1");
				link.appendChild(toad);
			clipitem.appendChild(link);
			
			
			
			print("fart");
			//Toss that video clip onto the track
			NodeList tracks = finalCutProProject.getElementsByTagName("track");
			for (int i = 0; i < tracks.getLength(); i++) {
				if(((Element) tracks.item(i)).getAttribute("id").equals(videoType)){
					tracks.item(i).appendChild(clipitem);
					print("once");
					

				}
				
			}
			
			//Add the two (or one) audio tracks

			//Track A
			clipitem = finalCutProProject.createElement("clipitem");
			clipitem.setAttribute("id", "vid-"+clipGroupCounter); //TODO make this proper
			//Stupid elements i don't need to mess with
			toad =finalCutProProject.createElement("enabled");
			toad.setTextContent(currentSection.getAttribute("TRUE"));
			clipitem.appendChild(toad);

			//Items to change
			clipname =finalCutProProject.createElement("name");
			clipname.setTextContent(currentSection.getAttribute("name")); // Make this proper
			clipitem.appendChild(clipname);
			
			toad =finalCutProProject.createElement("duration");
			toad.setTextContent(Long.toString(clipDurationFrames));
			clipitem.appendChild(toad);
			
			toad =finalCutProProject.createElement("start");
			toad.setTextContent(Long.toString(timelineposition));
			clipitem.appendChild(toad);
			
			toad =finalCutProProject.createElement("end");
			toad.setTextContent(Long.toString(timelineposition+clipDurationFrames));
			clipitem.appendChild(toad);
			
			toad =finalCutProProject.createElement("in");
			toad.setTextContent(Long.toString(clipStart -videoStart));
			clipitem.appendChild(toad);
			toad =finalCutProProject.createElement("out");
			toad.setTextContent(Long.toString((clipStart - videoStart)+clipDurationFrames));
			clipitem.appendChild(toad);
			
			file = finalCutProProject.createElement("file");
			file.setAttribute("id", "filevid-"+clipGroupCounter);//TODO make this proper
			clipitem.appendChild(file);
			Element sourcetrack = finalCutProProject.createElement("sourcetrack");
				toad =finalCutProProject.createElement("mediatype");
				toad.setTextContent("audio");
				sourcetrack.appendChild(toad);
				toad =finalCutProProject.createElement("trackindex");
				toad.setTextContent("1");
				sourcetrack.appendChild(toad);
			clipitem.appendChild(sourcetrack);
			
		
		//Toss that Audio clip A onto the track
		tracks = finalCutProProject.getElementsByTagName("track");
		for (int i = 0; i < tracks.getLength(); i++) {
			if(((Element) tracks.item(i)).getAttribute("id").equals(audioTypeA)){
				tracks.item(i).appendChild(clipitem);
				print("trackA");

			}
			
		}
		//Track B
		if(Integer.parseInt(vidChannels)==2){
		clipitem = finalCutProProject.createElement("clipitem");
		clipitem.setAttribute("id", "vid-"+clipGroupCounter); //TODO make this proper
		//Stupid elements i don't need to mess with
		toad =finalCutProProject.createElement("enabled");
		toad.setTextContent(currentSection.getAttribute("TRUE"));
		clipitem.appendChild(toad);

		//Items to change
		clipname =finalCutProProject.createElement("name");
		clipname.setTextContent(currentSection.getAttribute("name")); // Make this proper
		clipitem.appendChild(clipname);
		
		toad =finalCutProProject.createElement("duration");
		toad.setTextContent(Long.toString(clipDurationFrames));
		clipitem.appendChild(toad);
		
		toad =finalCutProProject.createElement("start");
		toad.setTextContent(Long.toString(timelineposition));
		clipitem.appendChild(toad);
		
		toad =finalCutProProject.createElement("end");
		toad.setTextContent(Long.toString(timelineposition+clipDurationFrames));
		clipitem.appendChild(toad);
		
		toad =finalCutProProject.createElement("in");
		toad.setTextContent(Long.toString(clipStart -videoStart));
		clipitem.appendChild(toad);
		toad =finalCutProProject.createElement("out");
		toad.setTextContent(Long.toString((clipStart - videoStart)+clipDurationFrames));
		clipitem.appendChild(toad);
		
		file = finalCutProProject.createElement("file");
		file.setAttribute("id", "filevid-"+clipGroupCounter);//TODO make this proper
		clipitem.appendChild(file);
		sourcetrack = finalCutProProject.createElement("sourcetrack");
			toad =finalCutProProject.createElement("mediatype");
			toad.setTextContent("audio");
			sourcetrack.appendChild(toad);
			toad =finalCutProProject.createElement("trackindex");
			toad.setTextContent("2");
			sourcetrack.appendChild(toad);
		clipitem.appendChild(sourcetrack);
		//Links
		
	
				// Toss that audio clip onto the track
				tracks = finalCutProProject.getElementsByTagName("track");
				for (int i = 0; i < tracks.getLength(); i++) {
					if (((Element) tracks.item(i)).getAttribute("id").equals(
							audioTypeB)) {
						tracks.item(i).appendChild(clipitem);
						print("trackB");

					}

				}
			}
		
		if(addTitle){
		// Toss that Title onto the track (if need be)
		tracks = finalCutProProject.getElementsByTagName("track");
		for (int i = 0; i < tracks.getLength(); i++) {
			if (((Element) tracks.item(i)).getAttribute("id").equals(
					MAIN_TIT_VIDEO)) {
				tracks.item(i).appendChild(makeVideoText(clip.getAttribute(PERSON),clip.getAttribute(PERSONTITLE) , timelineposition, 90 ));

				print("Added Title");

			}

		}
		
		
		}
		

			timelineposition = timelineposition + clipDurationFrames;
			clipGroupCounter = clipGroupCounter + 1;

		}
	




	
	
	
	private static void prepFinalCutDoc() {
		// TODO Auto-generated method stub
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DOMImplementation impl = builder.getDOMImplementation();
		finalCutProProject= builder.newDocument();
		finalCutProProject = impl.createDocument(null, "xmeml", null);
		
		Element root = finalCutProProject.getDocumentElement();
		root.setAttribute("version", "4");
		Element project = null;
		project= finalCutProProject.createElement("project");
		Element name = null;
		name=finalCutProProject.createElement("name");
		name.setTextContent(currentProjParsed.getDocumentElement().getAttribute("name"));
		project.appendChild(name);
		
		Element children = null;
		children= finalCutProProject.createElement("children");
		Element sequence = null;
		sequence= finalCutProProject.createElement("sequence");
		sequence.setAttribute("id", "main_sequence");
			Element toad =finalCutProProject.createElement("duration");
			toad.setTextContent("150");
			sequence.appendChild(toad);
			
			toad =finalCutProProject.createElement("rate");
			Element timebase =finalCutProProject.createElement("timebase");
			timebase.setTextContent("30");
			toad.appendChild(timebase);
			sequence.appendChild(toad);
			
			toad =finalCutProProject.createElement("name");
			toad.setTextContent(currentProjParsed.getDocumentElement().getAttribute("name"));
			sequence.appendChild(toad);
			
			Element media =finalCutProProject.createElement("media");
				Element video =finalCutProProject.createElement("video");
					Element format =finalCutProProject.createElement("format");
						Element samplechar =finalCutProProject.createElement("samplecharacteristics");
							toad =finalCutProProject.createElement("rate");
								timebase =finalCutProProject.createElement("timebase");
								timebase.setTextContent("30");
							toad.appendChild(timebase);
							samplechar.appendChild(toad);
							
							toad =finalCutProProject.createElement("width");
							toad.setTextContent("1920");
							samplechar.appendChild(toad);
							
							toad =finalCutProProject.createElement("height");
							toad.setTextContent("1080");
							samplechar.appendChild(toad);
							
							toad =finalCutProProject.createElement("anamorphic");
							toad.setTextContent("FALSE");
							samplechar.appendChild(toad);
							
							toad =finalCutProProject.createElement("pixelaspectratio");
							toad.setTextContent("square");
							samplechar.appendChild(toad);
							
							toad =finalCutProProject.createElement("fielddominance");
							toad.setTextContent("none");
							samplechar.appendChild(toad);
							
							toad =finalCutProProject.createElement("colordepth");
							toad.setTextContent("24");
							samplechar.appendChild(toad);
						
						format.appendChild(samplechar);
	
					video.appendChild(format);
					
					// set up 3 video editing tracks
					
					//Narration Track (lowest)
					Element track =finalCutProProject.createElement("track");
					track.setAttribute("id", MAIN_NAR_VIDEO);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					video.appendChild(track);
					
					//Interview Track (in middle)
					 track =finalCutProProject.createElement("track");
						track.setAttribute("id", MAIN_INT_VIDEO);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					track.setAttribute("id", MAIN_INT_VIDEO);
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					video.appendChild(track);
					
					//Exhibit Video Track
					 track =finalCutProProject.createElement("track");
						track.setAttribute("id", MAIN_EXH_VIDEO);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					video.appendChild(track);
					
					//Title Track
					 track =finalCutProProject.createElement("track");
						track.setAttribute("id", MAIN_TIT_VIDEO);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					track.setAttribute("id", MAIN_TIT_VIDEO);
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					
					//add title text
					track.appendChild(makeVideoText(currentProjParsed.getDocumentElement().getAttribute("name"),"" , 0, 90 ));
								
					video.appendChild(track);
					
					
				media.appendChild(video);
				
				// Audio Setup
				Element audio =finalCutProProject.createElement("audio");
					format =finalCutProProject.createElement("format");
					samplechar =finalCutProProject.createElement("samplecharacteristics");
				
						toad =finalCutProProject.createElement("depth");
						toad.setTextContent("16");
						samplechar.appendChild(toad);
						
						toad =finalCutProProject.createElement("samplerate");
						toad.setTextContent("48000");
						samplechar.appendChild(toad);
														
					format.appendChild(samplechar);
	
					audio.appendChild(format);
					
					Element outputs =finalCutProProject.createElement("outputs");
						Element group =finalCutProProject.createElement("group");
						toad =finalCutProProject.createElement("index");
						toad.setTextContent("1");
						group.appendChild(toad);
						toad =finalCutProProject.createElement("numchannels");
						toad.setTextContent("1");
						group.appendChild(toad);
						toad =finalCutProProject.createElement("downmix");
						toad.setTextContent("0");
						group.appendChild(toad);
						
						toad =finalCutProProject.createElement("channel");
						Element index =finalCutProProject.createElement("index");
						index.setTextContent("2");
						toad.appendChild(index);
						group.appendChild(toad);
					outputs.appendChild(group);
					audio.appendChild(outputs);
					
					//Setup 6 audio tracks
					
					//Narration
					track =finalCutProProject.createElement("track");
					track.setAttribute("id", MAIN_NAR_AUDIO_A);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					
					toad =finalCutProProject.createElement("outputchannelindex");
					toad.setTextContent("1");
					track.appendChild(toad);
					
					audio.appendChild(track);
					
					track =finalCutProProject.createElement("track");
					track.setAttribute("id", MAIN_NAR_AUDIO_B);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					
					toad =finalCutProProject.createElement("outputchannelindex");
					toad.setTextContent("2");
					track.appendChild(toad);
					
					audio.appendChild(track);
					
					//Interview
					track =finalCutProProject.createElement("track");
					track.setAttribute("id", MAIN_INT_AUDIO_A);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					
					toad =finalCutProProject.createElement("outputchannelindex");
					toad.setTextContent("1");
					track.appendChild(toad);
					
					audio.appendChild(track);
					
					track =finalCutProProject.createElement("track");
					track.setAttribute("id", MAIN_INT_AUDIO_B);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					
					toad =finalCutProProject.createElement("outputchannelindex");
					toad.setTextContent("2");
					track.appendChild(toad);
					
					audio.appendChild(track);
					
					//Exhibit
					track =finalCutProProject.createElement("track");
					track.setAttribute("id", MAIN_EXH_AUDIO_A);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					
					toad =finalCutProProject.createElement("outputchannelindex");
					toad.setTextContent("1");
					track.appendChild(toad);
					
					audio.appendChild(track);
					
					track =finalCutProProject.createElement("track");
					track.setAttribute("id", MAIN_EXH_AUDIO_B);
					toad =finalCutProProject.createElement("enabled");
					toad.setTextContent("TRUE");
					track.appendChild(toad);
					toad =finalCutProProject.createElement("locked");
					toad.setTextContent("FALSE");
					track.appendChild(toad);
					
					toad =finalCutProProject.createElement("outputchannelindex");
					toad.setTextContent("2");
					track.appendChild(toad);
					
					audio.appendChild(track);
					
					
	
				
				media.appendChild(audio);
			sequence.appendChild(media);
		
		children.appendChild(sequence);
		project.appendChild(children);
		root.appendChild(project);
		
		
	}


	private static Element makeVideoText(String theMaintext, String theSubtext, long start,  int duration) {
		// TODO Auto-generated method stub
		print("Title for: "+theMaintext+" at "+start);
		Element generatoritem = finalCutProProject.createElement("generatoritem");
		generatoritem.setAttribute("id", theMaintext);
		Element toad =finalCutProProject.createElement("name");
		
		toad.setTextContent(theMaintext);
		generatoritem.appendChild(toad);
		
		
		toad =finalCutProProject.createElement("duration");
		toad.setTextContent(Integer.toString(duration));
		generatoritem.appendChild(toad);
		
		toad = finalCutProProject.createElement("rate");
		Element timebase = finalCutProProject.createElement("timebase");
		timebase.setTextContent("30");
	toad.appendChild(timebase);
	generatoritem.appendChild(toad);
	
	toad =finalCutProProject.createElement("in");
	toad.setTextContent(""+start);
	generatoritem.appendChild(toad);
	
	toad =finalCutProProject.createElement("out");
	toad.setTextContent(""+(start+duration));
	generatoritem.appendChild(toad);
	
	toad =finalCutProProject.createElement("start");
	toad.setTextContent(""+start);
	generatoritem.appendChild(toad);
	
	toad =finalCutProProject.createElement("end");
	toad.setTextContent(""+(start+duration));
	generatoritem.appendChild(toad);
	toad =finalCutProProject.createElement("anamorphic");
	toad.setTextContent("FALSE");
	generatoritem.appendChild(toad);
	
	toad =finalCutProProject.createElement("alphatype");
	toad.setTextContent("black");
	generatoritem.appendChild(toad);
	
Element effect = finalCutProProject.createElement("effect");
effect.setAttribute("id", theMaintext);
			toad =finalCutProProject.createElement("name");
			toad.setTextContent(theMaintext);
			effect.appendChild(toad);
			toad =finalCutProProject.createElement("effectid");
			toad.setTextContent("Text");
			effect.appendChild(toad);
			
			toad =finalCutProProject.createElement("effectcategory");
			toad.setTextContent("Text");
			effect.appendChild(toad);
			toad =finalCutProProject.createElement("effecttype");
			toad.setTextContent("generator");
			effect.appendChild(toad);
			
			toad =finalCutProProject.createElement("mediatype");
			toad.setTextContent("video");
			effect.appendChild(toad);
			
			Element param = finalCutProProject.createElement("parameter");
				toad =finalCutProProject.createElement("parameterid");
				toad.setTextContent("str");
				param.appendChild(toad);
				toad =finalCutProProject.createElement("name");
				toad.setTextContent("string");
				param.appendChild(toad);
				
				toad =finalCutProProject.createElement("value");
				toad.setTextContent(theMaintext);
				param.appendChild(toad);
			
			effect.appendChild(param);
			//Accidently doubled for a subtitle!!
			param = finalCutProProject.createElement("parameter");
				toad =finalCutProProject.createElement("parameterid");
				toad.setTextContent("str");
				param.appendChild(toad);
				toad =finalCutProProject.createElement("name");
				toad.setTextContent("strindg");
				param.appendChild(toad);
				
				toad =finalCutProProject.createElement("value");
				toad.setTextContent(theSubtext);
				param.appendChild(toad);
		
			effect.appendChild(param);
			
			
			param = finalCutProProject.createElement("parameter");
				toad =finalCutProProject.createElement("parameterid");
				toad.setTextContent("fontname");
				param.appendChild(toad);
				toad =finalCutProProject.createElement("name");
				toad.setTextContent("strding");
				param.appendChild(toad);
				
				toad =finalCutProProject.createElement("value");
				toad.setTextContent("Gill Sans");
				param.appendChild(toad);
		
			effect.appendChild(param);
		
			// font size
			param = finalCutProProject.createElement("parameter");
				toad =finalCutProProject.createElement("parameterid");
				toad.setTextContent("fontsize");
				param.appendChild(toad);
				toad =finalCutProProject.createElement("name");
				toad.setTextContent("sdtring");
				param.appendChild(toad);
				toad =finalCutProProject.createElement("valuemin");
				toad.setTextContent("0");
				param.appendChild(toad);toad =finalCutProProject.createElement("valuemax");
				toad.setTextContent("1000");
				param.appendChild(toad);
				toad =finalCutProProject.createElement("value");
				toad.setTextContent("24");
				param.appendChild(toad);
		
			effect.appendChild(param);
			//Font style
			param = finalCutProProject.createElement("parameter");
				toad =finalCutProProject.createElement("parameterid");
				toad.setTextContent("fontstyle");
				param.appendChild(toad);
				toad =finalCutProProject.createElement("name");
				toad.setTextContent("stringdd");
				param.appendChild(toad);
				toad =finalCutProProject.createElement("valuemin");
				toad.setTextContent("0");
				param.appendChild(toad);toad =finalCutProProject.createElement("valuemax");
				toad.setTextContent("4");
				param.appendChild(toad);
					Element valuelist = finalCutProProject.createElement("valuelist");
						Element valueentry = finalCutProProject.createElement("valueentry");
	
							toad =finalCutProProject.createElement("name");
							toad.setTextContent("Normal");
							valueentry.appendChild(toad);
							toad =finalCutProProject.createElement("value");
							toad.setTextContent("1");
							valueentry.appendChild(toad);
						valuelist.appendChild(valueentry);
						
						valueentry = finalCutProProject.createElement("valueentry");
							
							toad =finalCutProProject.createElement("name");
							toad.setTextContent("Bold");
							valueentry.appendChild(toad);
							toad =finalCutProProject.createElement("value");
							toad.setTextContent("2");
							valueentry.appendChild(toad);
						valuelist.appendChild(valueentry);
						
						valueentry = finalCutProProject.createElement("valueentry");
							
							toad =finalCutProProject.createElement("name");
							toad.setTextContent("Italic");
							valueentry.appendChild(toad);
							toad =finalCutProProject.createElement("value");
							toad.setTextContent("3");
							valueentry.appendChild(toad);
						valuelist.appendChild(valueentry);
						
						
						valueentry = finalCutProProject.createElement("valueentry");
							
							toad =finalCutProProject.createElement("name");
							toad.setTextContent("Bold/Italic");
							valueentry.appendChild(toad);
							toad =finalCutProProject.createElement("value");
							toad.setTextContent("4");
							valueentry.appendChild(toad);
						valuelist.appendChild(valueentry);
						
					param.appendChild(valuelist);
				toad =finalCutProProject.createElement("value");
				toad.setTextContent("1");
				param.appendChild(toad);
		
			effect.appendChild(param);
		//Font alignment
			param = finalCutProProject.createElement("parameter");
			toad =finalCutProProject.createElement("parameterid");
			toad.setTextContent("fontalign");
			param.appendChild(toad);
			toad =finalCutProProject.createElement("name");
			toad.setTextContent("strindddg");
			param.appendChild(toad);
			toad =finalCutProProject.createElement("valuemin");
			toad.setTextContent("1");
			param.appendChild(toad);toad =finalCutProProject.createElement("valuemax");
			toad.setTextContent("3");
			param.appendChild(toad);
				valuelist = finalCutProProject.createElement("valuelist");
					valueentry = finalCutProProject.createElement("valueentry");

						toad =finalCutProProject.createElement("name");
						toad.setTextContent("Left");
						valueentry.appendChild(toad);
						toad =finalCutProProject.createElement("value");
						toad.setTextContent("1");
						valueentry.appendChild(toad);
					valuelist.appendChild(valueentry);
					
					valueentry = finalCutProProject.createElement("valueentry");
						
						toad =finalCutProProject.createElement("name");
						toad.setTextContent("Center");
						valueentry.appendChild(toad);
						toad =finalCutProProject.createElement("value");
						toad.setTextContent("2");
						valueentry.appendChild(toad);
					valuelist.appendChild(valueentry);
					
					valueentry = finalCutProProject.createElement("valueentry");
						
						toad =finalCutProProject.createElement("name");
						toad.setTextContent("Right");
						valueentry.appendChild(toad);
						toad =finalCutProProject.createElement("value");
						toad.setTextContent("3");
						valueentry.appendChild(toad);
					valuelist.appendChild(valueentry);
						
				param.appendChild(valuelist);
			toad =finalCutProProject.createElement("value");
			toad.setTextContent("1");
			param.appendChild(toad);
	
		effect.appendChild(param);
			//fontcolor
		param = finalCutProProject.createElement("parameter");
		toad =finalCutProProject.createElement("parameterid");
		toad.setTextContent("fontcolor");
		param.appendChild(toad);
		toad =finalCutProProject.createElement("name");
		toad.setTextContent("stringdddd");
		param.appendChild(toad);
		Element value =finalCutProProject.createElement("value");
			toad =finalCutProProject.createElement("alpha");
			toad.setTextContent("255");
			value.appendChild(toad);
			toad =finalCutProProject.createElement("red");
			toad.setTextContent("255");
			value.appendChild(toad);
			toad =finalCutProProject.createElement("green");
			toad.setTextContent("255");
			value.appendChild(toad);
			toad =finalCutProProject.createElement("blue");
			toad.setTextContent("255");
			value.appendChild(toad);
		param.appendChild(value);

	effect.appendChild(param);
			
	// origin
	param = finalCutProProject.createElement("parameter");
	toad =finalCutProProject.createElement("parameterid");
	toad.setTextContent("origin");
	param.appendChild(toad);
	toad =finalCutProProject.createElement("name");
	toad.setTextContent("stringddddd");
	param.appendChild(toad);
	value =finalCutProProject.createElement("value");
		toad =finalCutProProject.createElement("horiz");
		toad.setTextContent("255");
		value.appendChild(toad);
		toad =finalCutProProject.createElement("vert");
		toad.setTextContent("255");
		value.appendChild(toad);
	param.appendChild(value);

effect.appendChild(param);
// font track
param = finalCutProProject.createElement("parameter");
toad =finalCutProProject.createElement("parameterid");
toad.setTextContent("fonttrack");
param.appendChild(toad);
toad =finalCutProProject.createElement("name");
toad.setTextContent("stringdddddd");
param.appendChild(toad);
toad =finalCutProProject.createElement("valuemin");
toad.setTextContent("-200");
param.appendChild(toad);toad =finalCutProProject.createElement("valuemax");
toad.setTextContent("200");
param.appendChild(toad);
toad =finalCutProProject.createElement("value");
toad.setTextContent("1");
param.appendChild(toad);

effect.appendChild(param);

//leading
param = finalCutProProject.createElement("parameter");
toad =finalCutProProject.createElement("parameterid");
toad.setTextContent("leading");
param.appendChild(toad);
toad =finalCutProProject.createElement("name");
toad.setTextContent("stridddddddng");
param.appendChild(toad);
toad =finalCutProProject.createElement("valuemin");
toad.setTextContent("-100");
param.appendChild(toad);toad =finalCutProProject.createElement("valuemax");
toad.setTextContent("100");
param.appendChild(toad);
toad =finalCutProProject.createElement("value");
toad.setTextContent("1");
param.appendChild(toad);

effect.appendChild(param);
//aspect
param = finalCutProProject.createElement("parameter");
toad =finalCutProProject.createElement("parameterid");
toad.setTextContent("aspect");
param.appendChild(toad);
toad =finalCutProProject.createElement("name");
toad.setTextContent("stringdddddddd");
param.appendChild(toad);
toad =finalCutProProject.createElement("valuemin");
toad.setTextContent(".1");
param.appendChild(toad);toad =finalCutProProject.createElement("valuemax");
toad.setTextContent("5");
param.appendChild(toad);
toad =finalCutProProject.createElement("value");
toad.setTextContent("1");
param.appendChild(toad);

effect.appendChild(param);

//autokern
param = finalCutProProject.createElement("parameter");
toad =finalCutProProject.createElement("parameterid");
toad.setTextContent("autokern");
param.appendChild(toad);
toad =finalCutProProject.createElement("name");
toad.setTextContent("strdfdfdfing");
param.appendChild(toad);
toad =finalCutProProject.createElement("value");
toad.setTextContent("TRUE");
param.appendChild(toad);

effect.appendChild(param);
//subpixel
param = finalCutProProject.createElement("parameter");
toad =finalCutProProject.createElement("parameterid");
toad.setTextContent("subpixel");
param.appendChild(toad);
toad =finalCutProProject.createElement("name");
toad.setTextContent("strindfdfdfdg");
param.appendChild(toad);
toad =finalCutProProject.createElement("value");
toad.setTextContent("TRUE");
param.appendChild(toad);

effect.appendChild(param);


	generatoritem.appendChild(effect);
	return generatoritem;
	}


	public static void writeCurrentDocumenttoXML(String projectName){
		 	// Make MAIN file directories
			File finalCutProjDirectory = new File("video/");
			//newProjDirectory.mkdirs();
			File finalCutProjManifestTEMP = new File(finalCutProjDirectory,
			"temp");
			File finalCutProjManifest = new File(
					projectName+"__PreEdit.xml");
	    	try {
				finalCutProjManifest.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	       
			// we have to bind the new file with a FileOutputStream
			try {
				FileOutputStream  fOut = new FileOutputStream(finalCutProjManifestTEMP);
				 //write the content into xml file
	
	            OutputStreamWriter osw = new OutputStreamWriter(fOut); 
	            
	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            DOMSource source = new DOMSource( finalCutProProject);
	            StreamResult result =  new StreamResult(finalCutProjManifestTEMP);
	            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"REMOVE");
	           //Set up indents
	            transformer.setOutputProperty
	            ("{http://xml.apache.org/xslt}indent-amount", "4");
	         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            
	            transformer.transform(source, result);
	
	            osw.flush();
	            osw.close();
	            fOut.flush();
	            fOut.close();
	
			} catch (IOException e) {
				// TODO Auto-generated catch blo
				e.printStackTrace();
				print("FileNotFoundException can't create FileOutputStream");
	
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//get rid of that damn thing after !DOCTYPE
			String thefile=null;
		      try {
				 thefile =readFile(finalCutProjManifestTEMP.toString());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			print(thefile=thefile.replaceFirst(" SYSTEM \"REMOVE\"", ""));

			thefile=thefile.replaceFirst(" SYSTEM \"REMOVE\"", "");
			
			
			
			//Write teh string to a file
			FileWriter outFile = null;
			try {
	//			outFile = new FileWriter(finalCutProjManifestTEMP.toString());
				outFile = new FileWriter(finalCutProjManifest.toString());
	
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			 PrintWriter out = new PrintWriter(outFile);
		
		out.write(thefile);
		  out.close();
	
		}


	/** read the XML file to get rid of pesky string**/
		private static String readFile(String path) throws IOException {
			  FileInputStream stream = new FileInputStream(new File(path));
			    FileChannel fc = stream.getChannel();
	String thebigstring=null;
			  try {
			    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			    /* Instead of using default, pass in a decoder. */
		
			   thebigstring=Charset.defaultCharset().decode(bb).toString();
			  }
			  finally {
				  fc.close();
	
				  stream.close();
	
			  }
	return thebigstring;
			}


	/*
	 * Tiny Functions
	 */
	/** Print **/
	private static void print(String string) {
		
		
	   
	    textArea.append("\n"+string);

		
		System.out.println(string);
	}
	
	private static void printXML(Document doc) throws Exception {
	    TransformerFactory transformerFactory = TransformerFactory
	        .newInstance();
	    Transformer transformer = transformerFactory
	        .newTransformer();
	    DOMSource source = new DOMSource(doc);
	    Result result = new StreamResult(System.out);
	    transformer.transform(source, result);
	  }


	/** Parse an XML file */
	public static Document parseXMLFILE(String strURL) {
		URL url;
		URLConnection urlConn = null;

		try {
			url = new URL(strURL);
			urlConn = url.openConnection();

		} catch (IOException ioe) {
			System.out.println("error occurred while creating xml file");

		}

		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(urlConn.getInputStream());
		} catch (IOException ioe) {
print("IO");
		} catch (ParserConfigurationException pce) {
			print("PARSER");

		} catch (SAXException se) {
			print("SAX");

		}
		return doc;

	}	

}
	

