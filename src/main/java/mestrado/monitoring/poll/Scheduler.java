package mestrado.monitoring.poll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mestrado.flow.FlowEntry;
import mestrado.flow.FlowMatch;
import net.floodlightcontroller.core.IOFSwitch;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

public class Scheduler {
	
	private Set<DatapathId> switchesSet = new HashSet<DatapathId>(); //Avoid creation of new initial threads.
	public final static Integer tableValue = 0; //Define 0 for now.
	
	/***
	 * Verify if the Thread can be created. If yes, create it.
	 * @param dpid
	 * @return
	 */
	protected Boolean createFirstThread(DatapathId dpid){
		if(switchesSet.contains(dpid)){
			return false;
		}
		switchesSet.add(dpid);
		return true;
	}
	
	/***
	 * Analyse the Thread's collected data and give a feedback to the Thread. 
	 * @param nonWildcardedFields 
	 */
	public List<Scope> analyse(List<FlowEntry> flowEntryList, IOFSwitch sw, List<String> nonWildcardedFields){
		List<Scope> scopeList = new ArrayList<Scope>();
		if(flowEntryList == null){ // No flows.
			return scopeList;
		}
		if(flowEntryList.size() > 2){ // Create new Scope 
			Integer type = verifyNoNWildcardedFields(nonWildcardedFields); // Only once, because they're all the same.
			if(type == 0){
				scopeList = outPortGranularity(sw);
			}
			
			else if(type == 1){ 
				scopeList = matchGranularity(flowEntryList, sw); // Use the IPs Addresses as default.
			}
			
			//Aumentar depois ...
		}
		//switchGranularity();
		//It's necessary to create a way to go back to a more general Thread.
		
		return scopeList;
	}

	/***
	 * Define the NoN Wildcardeds fields for Querying.
	 * @param nonWildcardedFields
	 * @return
	 */
	private Integer verifyNoNWildcardedFields(List<String> nonWildcardedFields) {
		// TODO Auto-generated method stub
		if(nonWildcardedFields.size() == 0){ //All Wildcarded.
			return 0;
		}
		else if(nonWildcardedFields.size() == 1 && nonWildcardedFields.contains("outputPort")){ // Means the scope already has the port.
			return 1;
		}
		return 2;
		//Desenvolver algum algoritmo para fazer algo mais completo. Combinações?
	
	}

	private List<Scope> outPortGranularity(IOFSwitch sw) {
		// TODO Auto-generated method stub
		List<Scope> scopeList = new ArrayList<Scope>();
		Iterator<OFPort> portsIterator = sw.getEnabledPortNumbers().iterator();
		while(portsIterator.hasNext()){ //One Scope for each port, except the Local Port.
			Integer outputPort = portsIterator.next().getPortNumber();
			if(outputPort == -2){
				break;
			}
			scopeList.add(new Scope.ScopeBuilder().
					outputPort(outputPort).
					build(sw));
		}
		return scopeList;
	}

	private List<Scope> matchGranularity(List<FlowEntry> flowEntryList, IOFSwitch sw) {
		// TODO Auto-generated method stub
		int i;
		List<Scope> scopeList = new ArrayList<Scope>();
		Set<String> temp = new HashSet<String>();
		for(i=0; i<flowEntryList.size(); i++){
			FlowEntry flowEntry = flowEntryList.get(i);
			FlowMatch flowMatch = flowEntry.getFlowMatch();
			String sourceIP = flowMatch.getSourceIP();
			String destinationIP = flowMatch.getDestinationIP();
			if(temp.contains(sourceIP+","+destinationIP) == false && 
					(sourceIP != null && destinationIP != null)){ //Evitar que crie vários escopos para um mesmo match. Caso algum fluxo do switch
																  //não tenha granularidade de ip, ignorar ...
				temp.add(sourceIP+","+destinationIP);
				scopeList.add(new Scope.ScopeBuilder().
								ip(sourceIP, destinationIP).
								build(sw));
			}
		}
		return scopeList;
	}
	
}
