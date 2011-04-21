package andy.documatic;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.*;


public class AddFootage extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.addfootage);
        
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        TextView mTitleLeft = (TextView) findViewById(R.id.title_left_text);
		mTitleLeft.setText(Main.CurrentProject);

        TextView mTitleRight = (TextView) findViewById(R.id.title_right_text);
        mTitleRight.setText("Choose Footage Type");
        
        
        
     loadContent();
		setupButtons();

    	
    }
    
    
    
    private void loadContent() {

       	
    	
	}



	public void setupButtons() {

    	ImageButton mbINT;
		mbINT = (ImageButton) findViewById(R.id.addBinterview);
		mbINT.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Do things
				//Go to Interview activity
				Intent intIntent = new Intent();
				intIntent.setClassName(getApplicationContext(), "andy.documatic.Interview");
				startActivity(intIntent);
		
			}
		});
		
	  	ImageButton mbNARR;
		mbNARR = (ImageButton) findViewById(R.id.addBnarr);
		mbNARR.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Do things
				//Go to Interview activity
				Intent intent = new Intent();
				intent.setClassName(getApplicationContext(), "andy.documatic.Narration");
				startActivity(intent);
		
			}
		});
		
		ImageButton mbEX;
		mbEX = (ImageButton) findViewById(R.id.addBexhibit);
		mbEX.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Do things
				//Go to Interview activity
				Intent intent = new Intent();
				intent.setClassName(getApplicationContext(), "andy.documatic.Exhibit");
				startActivity(intent);
		
			}
		});
		
		Button becomeRecorderButton;
		becomeRecorderButton = (Button) findViewById(R.id.bBecomeRecorder);
		becomeRecorderButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//Launch Camera Activity
				Intent intent = new Intent();
				intent.setClassName(getApplicationContext(), "test.Cam.Camcorder");
				startActivity(intent);
		
			}
		});
	}
    
    
}