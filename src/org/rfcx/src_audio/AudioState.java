package org.rfcx.src_audio;

import java.util.ArrayList;

import com.badlogic.gdx.audio.analysis.*;

import android.util.Log;

public class AudioState {

	private static final String TAG = AudioState.class.getSimpleName();

	public static final boolean CAPTURE_SERVICE_ENABLED = true;
	public static final boolean PROCESS_SERVICE_ENABLED = true;

	public static final int CAPTURE_SAMPLE_RATE = 4000;
	public static final int FFT_RESOLUTION = 2048;
	
	private double[] fftSpectrumSum = new double[BUFFER_LENGTH];
	private int fftSpectrumSumIncrement = 0;
	private static final int fftSpectrumSumLength = 10;
	public static final int BUFFER_LENGTH = FFT_RESOLUTION * 2;
	
	private float[] fftWindowingCoeff = calcWindowingCoeff();

	private ArrayList<short[]> pcmDataBuffer = new ArrayList<short[]>();
	private static final int PCM_DATA_BUFFER_LIMIT = 100;
	
	public void addSpectrum() {
		if (pcmDataBufferLength() > 1) {
//			short[] pcmData = new short[BUFFER_LENGTH];
//			System.arraycopy(pcmDataBuffer.get(0), 0, pcmData, 0, BUFFER_LENGTH/2);
//			System.arraycopy(pcmDataBuffer.get(1), 0, pcmData, BUFFER_LENGTH/2, BUFFER_LENGTH/2);
//			addSpectrumSum(calcFFT(pcmData));
//			pcmDataBuffer.remove(0);
//			pcmDataBuffer.remove(1);
			
			addSpectrumSum(calcFFT(this.pcmDataBuffer.get(0)));
			pcmDataBuffer.remove(0);
			
			checkResetPcmDataBuffer();
//			Log.d(TAG, "Buffer: "+pcmDataBufferLength());
		}
	}

	private void addSpectrumSum(double[] fftSpectrum) {
		fftSpectrumSumIncrement++;

		for (int i = 0; i < fftSpectrum.length; i++) {
			fftSpectrumSum[i] = fftSpectrumSum[i] + fftSpectrum[i];
		}

		if (fftSpectrumSumIncrement == fftSpectrumSumLength) {
			for (int i = 0; i < fftSpectrumSum.length; i++) {
//				long lvl = Math.round(fftSpectrumSum[i] / fftSpectrumSumLength);
			}
			fftSpectrumSum = new double[BUFFER_LENGTH];
			fftSpectrumSumIncrement = 0;
		}
	}

	private double[] calcFFT(short[] array) {

		double[] real = new double[BUFFER_LENGTH];
		double[] imag = new double[BUFFER_LENGTH];
		double[] mag = new double[BUFFER_LENGTH];
		float[] new_array = new float[BUFFER_LENGTH];
		
		// For reconstruction
		// float[] real_mod = new float[BUFFER_LENGTH];
		// float[] imag_mod = new float[BUFFER_LENGTH];
		// double[] phase = new double[BUFFER_LENGTH];
		// float[] res = new float[BUFFER_LENGTH / 2];

		// Zero pad signal
		for (int i = 0; i < BUFFER_LENGTH; i++) {
			new_array[i] = (i < array.length) ? (fftWindowingCoeff[i]*array[i]) : 0;
		}

		FFT fft = new FFT(BUFFER_LENGTH, CAPTURE_SAMPLE_RATE);
		fft.forward(new_array);
//		float[] fft_cpx = fft.getSpectrum();
		float[] tmpi = fft.getImaginaryPart();
		float[] tmpr = fft.getRealPart();
		for (int i = 0; i < new_array.length; i++) {
			real[i] = (double) tmpr[i];
			imag[i] = (double) tmpi[i];
			mag[i] = Math.sqrt((real[i] * real[i]) + (imag[i] * imag[i]));
			/**** Reconstruction ****/
			// phase[i] = Math.atan2(imag[i], real[i]);
			// real_mod[i] = (float) (mag[i] * Math.cos(phase[i]));
			// imag_mod[i] = (float) (mag[i] * Math.sin(phase[i]));
		}
		// fft.inverse(real_mod, imag_mod, res);
		return mag;
	}

	private float[] calcWindowingCoeff() {
		float[] windowingCoeff = new float[BUFFER_LENGTH];
		for (int i = 0; i < BUFFER_LENGTH; i++) {
			double coeff = (0.5 * (1 - Math.cos((2 * Math.PI * i) / (BUFFER_LENGTH - 1))));
			windowingCoeff[i] = (float) coeff;
		}
		return windowingCoeff;
	}
	
	private void checkResetPcmDataBuffer() {
		if (pcmDataBufferLength() >= PCM_DATA_BUFFER_LIMIT) {
			this.pcmDataBuffer = new ArrayList<short[]>();
			Log.d(TAG,"PCM Data Buffer at limit. Buffer cleared.");
		}
	}
	
	public void cachePcmBuffer(short[] pcmData) {
		if ((pcmData.length == BUFFER_LENGTH) && (pcmDataBufferLength() < PCM_DATA_BUFFER_LIMIT)) {
//			short[] halfBuffer = new short[BUFFER_LENGTH/2];
//			System.arraycopy(pcmData, 0, halfBuffer, 0, BUFFER_LENGTH/2);
//			this.pcmDataBuffer.add(halfBuffer);
//			System.arraycopy(pcmData, BUFFER_LENGTH/2, halfBuffer, 0, BUFFER_LENGTH/2);
//			this.pcmDataBuffer.add(halfBuffer);
			
			this.pcmDataBuffer.add(pcmData);
		}
	}
	
	public int pcmDataBufferLength() {
		return this.pcmDataBuffer.size();
	}
}
