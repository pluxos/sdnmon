package mestrado.monitoring.database;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFBucket;
import org.projectfloodlight.openflow.protocol.OFGroupAdd;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;


import mestrado.flow.FlowEntry;
import mestrado.flow.FlowInstructions;
import mestrado.flow.FlowMatch;
import mestrado.flow.FlowStatistics;

public class InsertRow {
	
	public void insertFlowDB(Statement stmt, FlowEntry flowEntry){
		
			FlowMatch fm = flowEntry.getFlowMatch();
			FlowInstructions fi = flowEntry.getFlowInstructions();
			FlowStatistics fs = flowEntry.getFlowStatistics(); //Melhor fazer um mapeamento no controlador dos buckets!
			insertDB("outro.match", stmt, fm.getSourceIP(), fm.getDestinationIP(), 
					fm.getTransportProtocol(), fm.getTransportSourcePortNumber(), 
					fm.getTransportDestinationPortNumber(), 
					flowEntry.getFlowID(), flowEntry.getSwitchID());
			//
			insertDB("outro.action", stmt, flowEntry.getFlowID(), flowEntry.getSwitchID(),
					 fi.getType(), fi.getID());
			//
			insertDB("outro.flowstats", stmt, flowEntry.getFlowID(), flowEntry.getSwitchID(),
					fs.getTableID(), fs.getReceivedPackets(), fs.getReceivedBytes(),
					fs.getCurrentTransmissionRate(), fs.getUnit(), fs.getTime());
		
	}
	
	public void insertDB(String table, Statement stmt, Object... args){
		int i = 0;
		String values = "";
		System.out.println("ARGS:"+args.length);
		while(i < args.length){
			values += "'"+args[i]+"'";
			if(i < (args.length - 1))
				values += ",";
			i++;
		}
		String sql = "INSERT INTO " + table + " VALUES("+values+")";
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//
	public void insertIntoBucketDescription(Statement stmt, 
			OFGroupAdd ofGroup, String switchDPID){
		
		try {
			List<OFBucket> bucketsList = ofGroup.getBuckets();
			Integer groupID = ofGroup.getGroup().getGroupNumber();
			String sql = "";
			int i;
			for(i=0; i < bucketsList.size(); i++){
				OFBucket bucket = bucketsList.get(i);
				List<OFAction> actions = bucket.getActions();
				OFAction action = actions.get(0);
				OFActionOutput actionOutput = (OFActionOutput) action;
				Integer portNumber = actionOutput.getPort().getPortNumber();
				Integer weight = bucket.getWeight();
				String row = "INSERT INTO outro.bucketdesc VALUES("+"'"+switchDPID+"',"+"'"+groupID+"',"+"'"
				+portNumber+"',"+"'"+weight+"');";
				sql += row;
			}
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			DeleteRow dr = new DeleteRow();
			dr.removeGroup(stmt, ofGroup.getGroup().getGroupNumber());
			insertIntoBucketDescription(stmt, ofGroup, switchDPID);
			//Ignorar a verificação do inserção de buckets novamente.
		}
		
	}

}
