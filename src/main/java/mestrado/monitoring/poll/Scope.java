package mestrado.monitoring.poll;

import java.util.ArrayList;
import java.util.List;

import net.floodlightcontroller.core.IOFSwitch;

import org.projectfloodlight.openflow.protocol.OFFlowStatsRequest;
import org.projectfloodlight.openflow.protocol.OFFlowStatsRequest.Builder;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;

public class Scope {
	
	/***
	 * No futuro, substituir o nonWildcardedFields por flags (http://www.vipan.com/htdocs/bitwisehelp.html), 
	 * permitindo que seja desenvolvido sistemas de escolhas mais completos. 
	 */

	//Keep this order.
	private final String switchID;
	private final String sourceIP;
	private final String destinationIP;
	// Insert more fields to match (ARP, IP, srcMac, dstMac, ...)
	private final Integer outputPort;
	private Integer table = Scheduler.tableValue; //For now, use only 0.
	private final String threadName;
	private final List<String> nonWildcardedFields; //Selected Fields.
	private OFFlowStatsRequest scopeStatsRequest;

	
	private Scope(ScopeBuilder builder, IOFSwitch sw){
		this.switchID = builder.switchID;
		this.sourceIP = builder.sourceIP;
		this.destinationIP = builder.destinationIP;
		this.outputPort = builder.outputPort;
		this.threadName = builder.threadName;
		this.nonWildcardedFields = builder.nonWildcardedFields;
		process(sw);
	}

	/***
	 * Responsible to define a name for the Thread, construct the Openflow Stats Request Message and the NoN Wildcarded List.
	 * @param sw 
	 * @return
	 */
	public void process(IOFSwitch sw){
		Builder bd = sw.getOFFactory().buildFlowStatsRequest();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		constructFlowStatisticsRequest(bd, mb);
		bd.setMatch(mb.build());
		bd.setTableId(TableId.of(this.table)).build();
		this.scopeStatsRequest = bd.build();
	}
	
	/***
	 * Create Flow Statistis Request. It will be used to query the switch flow's counters.
	 * @param bd
	 * @param mb
	 */
	private void constructFlowStatisticsRequest(Builder bd, Match.Builder mb) {
		// TODO Auto-generated method stub
		
		int i;
		for(i =0; i<nonWildcardedFields.size(); i++){
			String field = nonWildcardedFields.get(i);
			switch(field){
					case "sourceIP":
						mb.setExact(MatchField.ETH_TYPE, EthType.IPv4); //Criar escopo com apenas IPv4 a priori.
						mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(sourceIP));
						break;
						
					case "destinationIP":
						mb.setExact(MatchField.IPV4_DST, IPv4Address.of(destinationIP));
						break;
						
					//... Other fields
						
					case "outputPort":
						bd.setOutPort(OFPort.of(outputPort));
						break;
						
					default:
			}
		}	
		
	}
	
	//######################GETTERS#################################
	
	public String getThreadName(){
		return threadName;
	}
	
	public OFFlowStatsRequest getScopeStatsRequest(){
		return scopeStatsRequest;
	}

	public List<String> getNoNWildcardedFields() {
		return nonWildcardedFields;
	}
	
	public String getSwitchID() {
		return switchID;
	}

	public String getSourceIP() {
		return sourceIP;
	}

	public String getDestinationIP() {
		return destinationIP;
	}

	public Integer getOutputPort() {
		return outputPort;
	}
	
	//#############################################################
	
	public static class ScopeBuilder {
		
		private String switchID;
		private String sourceIP;
		private String destinationIP;
		// Insert more fields to match (ARP, IP, srcMac, dstMac, ...)
		private Integer outputPort;
		private String threadName = "";
		private List<String> nonWildcardedFields = new ArrayList<>();

		
		public ScopeBuilder ip(String sourceIP, String destinationIP){
			this.sourceIP = sourceIP;
			this.destinationIP = destinationIP;
			this.nonWildcardedFields.add("sourceIP");
			this.nonWildcardedFields.add("destinationIP");
			this.threadName += (sourceIP + "," + destinationIP + ",");
			return this;
		}
		
		public ScopeBuilder outputPort(Integer outputPort){
			this.outputPort = outputPort;
			this.nonWildcardedFields.add("outputPort");
			this.threadName += (outputPort +",");
			return this;
		}
		
		/***
		 * Outros campos ...
		 */
		
		public Scope build(IOFSwitch sw){
			this.switchID = sw.getId().toString();
			this.threadName = sw.getId().toString() + "," + threadName ;
			this.threadName = threadName.substring(0, threadName.length() - 1); // Remove the last comma.
			return new Scope(this, sw);
		}

	}
}
