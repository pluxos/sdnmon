package mestrado.flow;

public class FlowEntry {

	private String switchID;
	private Integer flowID;
	private FlowMatch flowMatch;
	private FlowInstructions flowInstructions;
	private FlowStatistics flowStatistics;
	
	public FlowEntry(String switchID, FlowMatch flowMatch,
			FlowInstructions flowInstructions, FlowStatistics flowStatistics) {
		this.switchID = switchID;
		this.flowMatch = flowMatch;
		this.flowInstructions = flowInstructions;
		this.flowStatistics = flowStatistics;
		genFlowID();
	}

	public String getSwitchID() {
		return switchID;
	}

	public FlowMatch getFlowMatch() {
		return flowMatch;
	}

	public FlowInstructions getFlowInstructions() {
		return flowInstructions;
	}

	public FlowStatistics getFlowStatistics() {
		return flowStatistics;
	}

	public Integer getFlowID() {
		return flowID;
	}

	public void genFlowID() {
		String key = flowMatch.getSourceIP();
		key += flowMatch.getDestinationIP();
		key += flowMatch.getTransportSourcePortNumber();
		key += flowMatch.getTransportDestinationPortNumber();
		int i;
		int id = 0;
		for(i = 0; i<key.length(); i++){
			int ascii = (int) key.charAt(i);
			id += ascii;
		}
		this.flowID = id; //Pode sair repetido, mas chances baixas. 
	}



}
