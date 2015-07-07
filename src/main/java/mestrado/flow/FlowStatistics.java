package mestrado.flow;

import java.util.Calendar;

import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;

public class FlowStatistics {
	
	private Short tableID;
	private Long receivedPackets;
	private Long receivedBytes;
	private Integer priority;
	private Integer idleTimeout;
	private Integer hardTimeout;
	private Long duration;
	private Double currentTransmissionRate;
	private String unit = "MEGABITS";
	private Long time;
	
	public void setFlowStatistics(OFFlowStatsEntry statsEntry) {
		this.tableID = statsEntry.getTableId().getValue();
		this.receivedPackets = statsEntry.getPacketCount().getValue();
		this.receivedBytes = statsEntry.getByteCount().getValue();
		this.priority = statsEntry.getPriority();
		this.idleTimeout = statsEntry.getIdleTimeout();
		this.hardTimeout = statsEntry.getHardTimeout();
		this.duration = statsEntry.getDurationSec();
		setTime(Calendar.getInstance().getTimeInMillis());
	}
	
	/***
	 * Convert Bytes to a new Transfer Unit.
	 * @param value
	 * @param unit
	 * @param precision
	 * @return
	 */
	public Double bytesToN(Long value, String unit, int precision){
		Double newValue = null;
		switch(unit){
		
			case "KILOBITS":
				newValue = byteToBit(value) / 1000d;
				return applyPrecision(newValue, precision);
				
			case "KILOBYTES":
				newValue = value / 1000d;
				return applyPrecision(newValue, precision);
			
			case "MEGABITS":
				newValue = byteToBit(value) / (1000d * 1000d);
				return applyPrecision(newValue, precision);
				
			case "MEGABYTES":
				newValue = value / (1000d * 1000d);
				return applyPrecision(newValue, precision);
		
		}
		
		return null;
		
	}
	
	public Double calculateTransmission(Long oldBytes, Long newBytes, Long oldTime,
			Long newTime, String unit){

		try{
			Double transmissionRate = (double) ((newBytes - oldBytes) / ((newTime - oldTime) /1000));
			return bytesToN(transmissionRate.longValue(), unit, 3);
		}catch (Exception e) {
			return bytesToN(0L, unit, 3);
			// TODO: handle exception
		}	
	}
	
	private Long byteToBit(Long value){
		return value * 8 ;
	}
	
	public static Double applyPrecision(Double value, int precision){
		switch(precision){
		case 0:
			return value;
		case 1:
			return (Math.round(value * 10.0) / 10.0);
			
		case 2:
			return (Math.round(value * 100.0) / 100.0);
			
		case 3:
			return (Math.round(value * 1000.0) / 1000.0);
			
		case 4:
			return (Math.round(value * 10000.0) / 10000.0);
			
		}
		return value;
	}


	public Long getReceivedPackets() {
		return receivedPackets;
	}

	public Long getReceivedBytes() {
		return receivedBytes;
	}

	public String getUnit() {
		return unit;
	}

	public Short getTableID() {
		return tableID;
	}

	public void setTableID(Short tableID) {
		this.tableID = tableID;
	}

	public void setReceivedPackets(Long receivedPackets) {
		this.receivedPackets = receivedPackets;
	}

	public void setReceivedBytes(Long receivedBytes) {
		this.receivedBytes = receivedBytes;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Double getCurrentTransmissionRate() {
		return currentTransmissionRate;
	}

	public void setCurrentTransmissionRate(Double currentTransmissionRate) {
		this.currentTransmissionRate = currentTransmissionRate;
	}
	
	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	
}
