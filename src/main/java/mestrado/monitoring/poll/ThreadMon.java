package mestrado.monitoring.poll;

public class ThreadMon {
	
	private String name;
	private String switchID;
	private Integer outPort;
	private String sourceIP;
	private String destinationIP;
	private String status;
	
	public ThreadMon(String name, String switchID, Integer outPort,
			String sourceIP, String destinationIP, String status) {
		this.name = name;
		this.switchID = switchID;
		this.outPort = outPort;
		this.sourceIP = sourceIP;
		this.destinationIP = destinationIP;
		this.status = status;
	}
	
	
	public String getName() {
		return name;
	}

	public String getSwitchID() {
		return switchID;
	}

	public Integer getOutPort() {
		return outPort;
	}

	public String getSourceIP() {
		return sourceIP;
	}

	public String getDestinationIP() {
		return destinationIP;
	}
	
	public String getStatus(){
		return status;
	}
	
	// Other fields ...

}
