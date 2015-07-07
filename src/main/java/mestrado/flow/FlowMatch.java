package mestrado.flow;

import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;

public class FlowMatch {
	
	private Integer inputPort;
	private String networkProtocol;
	private String sourceIP;
	private String destinationIP;
	private String transportProtocol;
	private Integer transportSourcePortNumber;
	private Integer transportDestinationPortNumber;
	
	
	public void setFlowMatch(Match match){
		try{
			//this.inputPort = match.get(MatchField.IN_PORT).getPortNumber();
			//Integer networkProtocolNumber = match.get(MatchField.ETH_TYPE).getValue();
			//this.networkProtocol = defineNetworkProtocolName(networkProtocolNumber);
			this.sourceIP = match.get(MatchField.IPV4_SRC).toString();
			this.destinationIP = match.get(MatchField.IPV4_DST).toString();
			Short transportProtocolNumber = match.get(MatchField.IP_PROTO).getIpProtocolNumber();
			this.transportProtocol = defineTransportProtocolName(transportProtocolNumber);
			defineTransportPortsNumbers(match);
		}catch(Exception e){
			//O match será NULL no Table-Flow Miss Entry. Pretendo ignorar esse fluxo no BD.
			//e.printStackTrace();
			//System.out.println("Camada 3 para baixo ...");
			return;
		}
	}


	private String defineTransportProtocolName(Short transportProtocolNumber) {
		// TODO Auto-generated method stub
		//Short tcpNumber = IpProtocol.TCP.getIpProtocolNumber();
		switch(transportProtocolNumber){
			
			case 6: //6
				return "TCP";
				
			case 17: 
				return "UDP";
	
			//Add more Internet Protocol Numbers (http://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml)
				
			default:
				return "Other";
		}
	}


	private String defineNetworkProtocolName(Integer networkProtocolNumber) {
		// TODO Auto-generated method stub
		switch(networkProtocolNumber){
		
			case 2048:
					return "IPv4";
					
			case 2054:
					return "ARP";
					
			// Insert more protocols of 2.5 and 3 layers (http://www.iana.org/assignments/ieee-802-numbers/ieee-802-numbers.xhtml).
					
			default:
					return "Other";
					
		}
		
	}
	
	private void defineTransportPortsNumbers(Match match){
		
		switch(this.transportProtocol){
			case "TCP":
				this.transportSourcePortNumber = match.get(MatchField.TCP_SRC).getPort();
				this.transportDestinationPortNumber = match.get(MatchField.TCP_DST).getPort();
				break;
				
			case "UDP":			
					this.transportSourcePortNumber = match.get(MatchField.UDP_SRC).getPort();
					this.transportDestinationPortNumber = match.get(MatchField.UDP_DST).getPort();
				break;
				
			default: 
					
		}
		
	}


	public Integer getInputPort() {
		return inputPort;
	}


	public String getNetworkProtocol() {
		return networkProtocol;
	}


	public String getSourceIP() {
		return sourceIP;
	}


	public String getDestinationIP() {
		return destinationIP;
	}


	public String getTransportProtocol() {
		return transportProtocol;
	}


	public Integer getTransportSourcePortNumber() {
		return transportSourcePortNumber;
	}


	public Integer getTransportDestinationPortNumber() {
		return transportDestinationPortNumber;
	}


	public void setSourceIP(String sourceIP) {
		this.sourceIP = sourceIP;
	}
	
	public void setDestinationIP(String destinationIP) {
		this.destinationIP = destinationIP;
	}


	public void setTransportProtocol(String transportProtocol) {
		this.transportProtocol = transportProtocol;
	}


	public void setTransportSourcePortNumber(Integer transportSourcePortNumber) {
		this.transportSourcePortNumber = transportSourcePortNumber;
	}


	public void setTransportDestinationPortNumber(
			Integer transportDestinationPortNumber) {
		this.transportDestinationPortNumber = transportDestinationPortNumber;
	}

	
}
