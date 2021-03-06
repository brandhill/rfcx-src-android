package org.rfcx.src_util;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.rfcx.src_android.RfcxSource;

public class DeviceCpuUsage {
	
	public static final int REPORTING_SAMPLE_COUNT = 60;
	
	private float cpuUsageNow = 0;
	private float cpuUsageAvg = 0;
	private float[] prevCpuUsage = new float[REPORTING_SAMPLE_COUNT];
	
	private float cpuClockNow = 0;
	private float cpuClockAvg = 0;
	private float[] prevCpuClock = new float[REPORTING_SAMPLE_COUNT];
	private boolean updateClockSpeed = false;
	
	public static final int SAMPLE_LENGTH_MS = 360;
	
	public int getCpuUsageAvg() {
		return Math.round(100*cpuUsageAvg);
	}
	
	public int getCpuClockAvg() {
		return Math.round(cpuClockAvg/1000);
	}
	
	public void updateCpuUsage() {
		updateUsage();
		incrementAvg();
		updateClockSpeed = !updateClockSpeed;
	}
	
	private void incrementAvg() {
		float usageAvgTotal = 0;
		float clockAvgTotal = 0;
		for (int i = 0; i < REPORTING_SAMPLE_COUNT-1; i++) {
			this.prevCpuUsage[i] = this.prevCpuUsage[i+1];
			usageAvgTotal = usageAvgTotal + this.prevCpuUsage[i+1];
			this.prevCpuClock[i] = this.prevCpuClock[i+1];
			clockAvgTotal = clockAvgTotal + this.prevCpuClock[i+1];
		}
		this.prevCpuUsage[REPORTING_SAMPLE_COUNT-1] = this.cpuUsageNow;
		this.cpuUsageAvg = (usageAvgTotal + this.cpuUsageNow) / REPORTING_SAMPLE_COUNT;
		this.prevCpuClock[REPORTING_SAMPLE_COUNT-1] = this.cpuClockNow;
		this.cpuClockAvg = (clockAvgTotal + this.cpuClockNow) / REPORTING_SAMPLE_COUNT;
	}
	
	private void updateUsage() {
		try {
	        RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
	        String load = reader.readLine();
	        String[] toks = load.split(" ");
	        long idle1 = Long.parseLong(toks[5]);
	        long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
	        try {
	            Thread.sleep(SAMPLE_LENGTH_MS);
	        } catch (Exception e) {}
	        reader.seek(0);
	        load = reader.readLine();
	        reader.close();
	        toks = load.split(" ");
	        long idle2 = Long.parseLong(toks[5]);
	        long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
	            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
	        this.cpuUsageNow = (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
		if (updateClockSpeed) {
			try {
				RandomAccessFile scaling_cur_freq = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq", "r");
				this.cpuClockNow = Integer.parseInt(scaling_cur_freq.readLine());
				scaling_cur_freq.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
