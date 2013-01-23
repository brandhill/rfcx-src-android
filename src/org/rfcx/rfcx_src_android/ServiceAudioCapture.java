package org.rfcx.rfcx_src_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import org.rfcx.src_audio.*;

public class ServiceAudioCapture extends Service {

	private static final String TAG = ServiceAudioCapture.class.getSimpleName();

	AudioState audioState = new AudioState();

	private boolean runFlag = false;
	private AudioCapture audioCapture;
	
	private int audioCaptureSampleRate = audioState.audioCaptureSampleRate;
	private int audioCaptureFrameSize = audioState.fftBlockSize;
	private int audioCaptureChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioCaptureEncoding = AudioFormat.ENCODING_PCM_16BIT;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.audioCapture = new AudioCapture();
		Log.d(TAG, "onCreated()");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		this.runFlag = true;
		this.audioCapture.start();
		Log.d(TAG, "onStarted()");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		this.audioCapture.interrupt();
		this.audioCapture = null;
		Log.d(TAG, "onDestroyed()");
	}

	private class AudioCapture extends Thread {

		public AudioCapture() {
			super("AudioCaptureService-AudioCapture");
		}

		@Override
		public void run() {
			ServiceAudioCapture audioCaptureService = ServiceAudioCapture.this;

			try {
				int bufferSize = 4 * AudioRecord.getMinBufferSize(
						audioCaptureSampleRate, audioCaptureChannelConfig,
						audioCaptureEncoding);
				AudioRecord audioRecord = new AudioRecord(
						MediaRecorder.AudioSource.MIC, audioCaptureSampleRate,
						audioCaptureChannelConfig, audioCaptureEncoding,
						bufferSize);
				short[] buffer = new short[audioCaptureFrameSize];
				double[] audioFrame = new double[audioCaptureFrameSize];
				audioRecord.startRecording();
				Log.d(TAG, "AudioCaptureService started (buffer: "+bufferSize+")");
				
				while (audioCaptureService.runFlag) {
					try {
						int bufferReadResult = audioRecord.read(buffer, 0, audioCaptureFrameSize);
						for (int i = 0; i < audioCaptureFrameSize && i < bufferReadResult; i++) {
							audioFrame[i] = (double) buffer[i] / 32768.0;
						}
						audioState.addFrame(audioFrame);
					} catch (Exception e) {
						audioCaptureService.runFlag = false;
					}
				}
				audioRecord.stop();
			} catch (Exception e) {
				audioCaptureService.runFlag = false;
			}
		}

	}

//	private String concatValues(double[] values) {
//		StringBuilder sbFFT = new StringBuilder();
//		sbFFT.append(decimalFormat.format(java.lang.Math.abs(values[0]
//				* fftSigFigMultiplier)));
//		for (int i = 1; i < fftBlockSize; i++) {
//			sbFFT.append(",").append(
//					decimalFormat.format(java.lang.Math.abs(values[i]
//							* fftSigFigMultiplier)));
//		}
//		return sbFFT.toString();
//	}
//
//	private void saveSpectrum(String spectrum) {
//		ContentValues values = new ContentValues();
//		values.put(DbAudioCapture.C_SPECTRUM, spectrum);
//		// audioCaptureDbHelper.insertOrIgnore(values);
//	}

}