package mestrado.monitoring.poll;

public class MatchMon {
	
	private String dpid;
	private Short outputPort;
	private String sourceIP;
	private String destinationIP;
	
	
	public MatchMon(String dpid, Short outputPort, String sourceIP,
			String destinationIP) {
		
		this.dpid = dpid;
		this.outputPort = outputPort;
		this.sourceIP = sourceIP;
		this.destinationIP = destinationIP;
	}
	
	public String getDpid() {
		return dpid;
	}

	public Short getOutputPort() {
		return outputPort;
	}

	public String getSourceIP() {
		return sourceIP;
	}

	public String getDestinationIP() {
		return destinationIP;
	}
	
	public String toString(){
		return dpid+","+outputPort+","+sourceIP+","+destinationIP;
	}

}
