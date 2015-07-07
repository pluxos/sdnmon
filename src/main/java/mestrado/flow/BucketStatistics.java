package mestrado.flow;

public class BucketStatistics {
	
	private Integer groupID;
	private Integer bucketID; 
	private Long receivedPackets;
	private Long receivedBytes;
	private Double transmissionRate;
	private String unit;
	
	public void setBucketStatistics(Integer groupID, Integer bucketID,
			Integer actionID, FlowStatistics flowStats) { // Armazenamento Interno!
		this.groupID = groupID;
		this.bucketID = bucketID;
		this.receivedPackets = flowStats.getReceivedPackets();
		this.receivedBytes   = flowStats.getReceivedPackets();
		//this.transmissionRate = flowStats.getTransmissionRate();
		this.unit = flowStats.getUnit();
	}

	public Integer getGroupID() {
		return groupID;
	}

	public Integer getBucketID() {
		return bucketID;
	}

	public Long getReceivedPackets() {
		return receivedPackets;
	}

	public Long getReceivedBytes() {
		return receivedBytes;
	}

	public Double getTransmissionRate() {
		return transmissionRate;
	}

	public String getUnit() {
		return unit;
	}


}
