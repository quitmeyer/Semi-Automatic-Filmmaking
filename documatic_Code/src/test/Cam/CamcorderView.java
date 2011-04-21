package test.Cam;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CamcorderView extends SurfaceView implements
		SurfaceHolder.Callback {

	MediaRecorder recorder;
	SurfaceHolder holder;
	String outputFile = "/sdcard/DOCdefault.mp4";

	public CamcorderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		recorder = new MediaRecorder();
		 
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setVideoSize(900, 480);
		 recorder.setVideoEncodingBitRate(7000000);
		 recorder.setAudioEncodingBitRate(50000);
			recorder.setVideoFrameRate(24);

		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		

//		 recorder.setVideoEncodingBitRate(DRAWING_CACHE_QUALITY_HIGH);
//		 recorder.setVideoEncodingBitRate(7000000);
		 
		// recorder.setMaxDuration(10000);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		recorder.setOutputFile(outputFile);
		recorder.setPreviewDisplay(holder.getSurface());
		if (recorder != null) {
			try {
				recorder.prepare();
			} catch (IllegalStateException e) {
				Log.e("IllegalStateException", e.toString());
			} catch (IOException e) {
				Log.e("IOException", e.toString());
			}
		}
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		recorder.stop();
    	recorder.release();
	}

	public void resetSurfaceFile(String filename) {
		recorder.reset();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		 
		 
		outputFile = filename;
		recorder.setOutputFile(filename);
		
		recorder.setPreviewDisplay(holder.getSurface());
		if (recorder != null) {
			try {
				recorder.prepare();
			} catch (IllegalStateException e) {
				Log.e("IllegalStateException", e.toString());
			} catch (IOException e) {
				Log.e("IOException", e.toString());
			}
		}
		
	}
	public void setOutputFile(String filename)
	{
		outputFile = filename;
		recorder.setOutputFile(filename);
	}
	
    public void startRecording()
    {
    	recorder.start();
    }
    
    public void stopRecording()
    {
    	recorder.stop();
    	recorder.release();
    }
}