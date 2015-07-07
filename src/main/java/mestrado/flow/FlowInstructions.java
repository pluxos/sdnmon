package mestrado.flow;

import java.util.List;

import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionGroup;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;

public class FlowInstructions {
	
	
	private Integer id;
	private String type;
    //Instead of a list of Instruction, let just one!
	public void setFlowInstructions(List<OFInstruction> list) {
		try{
			OFInstructionApplyActions oiaa = (OFInstructionApplyActions) list.get(0);
			List<OFAction> listOFActions= (List<OFAction>) oiaa.getActions();
			OFAction action = (OFAction) listOFActions.get(0);
			processAux(action);
		}catch (ClassCastException c) { // No caso de pacote camada 3 pra baixo.
			// TODO: handle exception
			c.printStackTrace();
		}
	}
	
	public void processAux(OFAction action){
		switch(action.getType()) {
		case OUTPUT:
			OFActionOutput oaf = (OFActionOutput) action;
			this.id = oaf.getPort().getPortNumber();
			this.type = action.getType().toString();
			break;
			
		case GROUP:
			OFActionGroup oag = (OFActionGroup) action;
			this.id = oag.getGroup().getGroupNumber();
			this.type = action.getType().toString();
			break;	
		
		}
	}

	public Integer getID() {
		return id;
	}

	public String getType() {
		return type;
	}

	public void setID(Integer id) {
		this.id = id;
	}

	public void setType(String type) {
		this.type = type;
	}

}
