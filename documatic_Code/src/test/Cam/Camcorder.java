package test.Cam;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import andy.documatic.Main;
import andy.documatic.R;
import test.Cam.CamcorderView;

public class Camcorder extends Activity {

	     private CamcorderView camcorderView; 
	     private boolean recording = false; 
	 	private Button cRecButton;
	 	public long startTime;
	 	public long stopTime;
	 	public long duration;
	    private Chronometer crono;




	     /** Called when the activity is first created. */ 
	     @Override 
	     public void onCreate(Bundle savedInstanceState) { 
	          super.onCreate(savedInstanceState); 
	          requestWindowFeature(Window.FEATURE_NO_TITLE); 
	          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
	          setContentView(R.layout.camcorder_preview); 
	          
	          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	          camcorderView = (CamcorderView) findViewById(R.id.camcorder_preview); 
 	     
              TextView vTvw = (TextView)findViewById(R.id.EditText01);
              Date date = new Date(System.currentTimeMillis());
              java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
             
             
                 vTvw.setText(Main.CurrentProject+" - "+dateFormat.format(date)+" "+(date.getHours())+":"+(date.getMinutes()));

	          
	          
	          cRecButton = (Button) findViewById(R.id.camRec);
	          
	          
	  		cRecButton.setOnClickListener(new OnClickListener() {
	  			
	  			
	  			public void onClick(View v) {
	  				// Do things
	  				 if (recording) { 
	  					stopTime=System.currentTimeMillis();
	  	       		  	duration=stopTime-startTime;
	  	       		  	crono.stop();	
            		  	cRecButton.setText("STOPPED");

	            		  	camcorderView.stopRecording();
	            		  	
	                     finish(); 
	                 } else { 
	                     recording = true; 
	                     TextView vw = (TextView)findViewById(R.id.EditText01);
	                    startTime = System.currentTimeMillis();
	                     String projName =  Main.CurrentProject;
	                     
	                     Date date = new Date(System.currentTimeMillis());
	                     java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
	                    
	                    
		                    vw.setText(Main.CurrentProject+dateFormat.format(date)+" - "+(date.getHours())+":"+(date.getMinutes()));
File videoFile =  new File("/sdcard/Documatic/"+projName+"/video/");
	                     camcorderView.resetSurfaceFile("/sdcard/Documatic/"+projName+"/video/"+startTime+".mp4");
	             		
	                     crono = (Chronometer) findViewById(R.id.Chronometer01);

	                     crono.setBase(SystemClock.elapsedRealtime());
	                     crono.start();
	             		//camcorderView.recorder.reset();
	                     camcorderView.startRecording(); 
	            		  	cRecButton.setText("Recording!");
	            		  	
	            		  	// TODO add video file to registry xml

	                 } 
	
	  				
	  			}
	  		});
	     } 
	     
	     
	     
	     
         @Override 
         public boolean onKeyDown(int keyCode, KeyEvent event) 
         { 
             if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) 
             { 
           	  if (recording) { 
           		stopTime=System.currentTimeMillis();
       		  	duration=stopTime-startTime;
       		  	crono.stop();	
           		  
           		  camcorderView.stopRecording();
           		  	
                    finish(); 
                } else { 
                    recording = true; 
                    this.crono = (Chronometer) findViewById(R.id.Chronometer01);

                    crono.setBase(SystemClock.elapsedRealtime());
                    crono.start();
                    camcorderView.startRecording();

                } 
                 return true; 
             } 
             return super.onKeyDown(keyCode, event); 
         }	     
         
//         @Override
//     	protected void onPause(){
//        	 camcorderView.surfaceDestroyed(null);
//     		super.onPause();
//
//         }
         
         
}