package mestrado.monitoring.database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mestrado.flow.FlowEntry;
import mestrado.flow.FlowInstructions;
import mestrado.flow.FlowMatch;
import mestrado.flow.FlowStatistics;
import mestrado.monitoring.poll.ThreadMon;

public class RetrieveRow {
	
	ConstructObjectJson constructObjectJson = new ConstructObjectJson();
	
	
	/**************************INTERNAL USE ****************************************/
	public FlowEntry retrieve(String switchID, Integer flowID, Statement stmt){
		
		try {
			ResultSet rs = retrieveResultSet("outro.match", stmt, switchID, flowID);
			if(!rs.isBeforeFirst()){
				return null;
			}
			FlowMatch fm = getFlowMatch(rs);
			rs = retrieveResultSet("outro.action", stmt, switchID, flowID);
			FlowInstructions fi = getFlowInstructions(rs);
			rs = retrieveResultSet("outro.flowstats", stmt, switchID, flowID);
			FlowStatistics fs = getFlowStatistics(rs);
			FlowEntry flowEntry = new FlowEntry(switchID, fm, fi, fs);
			rs.close();
			return flowEntry;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public ResultSet retrieveResultSet(String table, Statement stmt, String switchID, Integer flowID){
		
		ResultSet rs;
		try {
			rs = stmt.executeQuery("SELECT * FROM "+table+"" +
					" WHERE switch_dpid ='" +switchID+ "' and flow_id = '"+flowID+"'");
			return rs;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public FlowMatch getFlowMatch(ResultSet rs){
		FlowMatch fm = new FlowMatch();
		try {
			while (rs.next())
			{
				fm.setSourceIP(rs.getString(1));
				fm.setDestinationIP(rs.getString(2));
				fm.setTransportProtocol(rs.getString(3));
				fm.setTransportSourcePortNumber(rs.getInt(4));
				fm.setTransportDestinationPortNumber(rs.getInt(5));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fm; 
		
	}
	
	public FlowInstructions getFlowInstructions(ResultSet rs){
		FlowInstructions fi = new FlowInstructions();
		try {
			while (rs.next())
			{
				fi.setType(rs.getString(3));
				fi.setID(rs.getInt(4));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fi; 
		
	}
	
	public FlowStatistics getFlowStatistics(ResultSet rs){
		FlowStatistics fs = new FlowStatistics();
		try {
			while (rs.next())
			{
				fs.setTableID(rs.getShort(3));
				fs.setReceivedPackets(rs.getLong(4));
				fs.setReceivedBytes(rs.getLong(5));
				fs.setCurrentTransmissionRate(rs.getDouble(6));
				fs.setUnit(rs.getString(7));
				fs.setTime(rs.getLong(8));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fs; 
	}
	
	public boolean existThread(Statement stmt, String name) {
		// TODO Auto-generated method stub
		ResultSet rs;
		String sql = "SELECT exists(SELECT 1 FROM outro.threads WHERE name= '"+name+"')";
		try {
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				if(rs.getBoolean(1) == true){
					return true;
				}
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	
	public List<ThreadMon> getThreads(Statement stmt) {
		// TODO Auto-generated method stub
		List<ThreadMon> list = new ArrayList<ThreadMon>();
		ResultSet rs;
		String sql = "SELECT * FROM outro.threads";
		try {
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				list.add(new ThreadMon(rs.getString(1),//Name
				rs.getString(2),//Switch ID
				rs.getInt(3),//Port Number
				rs.getString(4),//Source IP
				rs.getString(5),//Destination IP
				rs.getString(6)));//Status
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	
	/****************************EXTERNAL USE **********************************/
	
	public List<Map<String, Object>> getFlowsList(Connection connection){
		try {
			List<Map<String, Object>> flowsList = new ArrayList<Map<String, Object>>();
			Statement stmt = connection.createStatement();
			ResultSet rs;
			rs = stmt.executeQuery("SELECT DISTINCT source_ip, destionation_ip, transport_protocol," +
					" source_port, destination_port FROM outro.match");
			while (rs.next())
			{
				Map<String, Object> flow = constructObjectJson.constructFlowList(rs.getString(1), 
						rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5));
				flowsList.add(flow);
			}
			stmt.close();
			return flowsList;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Map<String, List<Map<String, Object>>> getFlowTraffic(Connection connection, Integer flowID){
		try {
			Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String, Object>>>();
			Statement stmt = connection.createStatement();
			Statement stmt2 = connection.createStatement();
			ResultSet rs;
			rs = stmt.executeQuery("SELECT switch_dpid, transmission_rate" +
					" FROM outro.flowstats WHERE flow_id ='"+flowID+"'");
			while (rs.next())
			{
				ResultSet rs2 = stmt2.executeQuery("SELECT type, id " +
						"FROM outro.action WHERE flow_id ='"+flowID+"' and switch_dpid ='"+rs.getString(1)+"'");
				if(rs2.next()){
					if(rs2.getString(1).equals("GROUP")){
						map = querySwitchBuckets(connection, rs.getString(1), flowID, rs.getDouble(2), map);//
					}
					else if(rs2.getString(1).equals("OUTPUT")){
						map = constructObjectJson.constructSwitchPortMap(rs.getString(1), rs2.getInt(2), null, rs.getDouble(2), null, map);
					}
				}
				//Arrumar a parte da remoção dos fluxos, para não faltar algum.
			}
			if(map.size() == 0){
				return null;
			}
			stmt.close();
			stmt2.close();
			return map;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	private Map<String, List<Map<String, Object>>> querySwitchBuckets(Connection connection, String switchDPID, 
			Integer flowID, Double transmissionRate, Map<String, List<Map<String, Object>>> map) {
		// TODO Auto-generated method stub
		Statement stmt;
		Statement stmt2;
		Statement stmt3;
		try {
			stmt = connection.createStatement();
			stmt2 = connection.createStatement();
			stmt3 = connection.createStatement();
			ResultSet rs;
			rs = stmt.executeQuery("SELECT id " +
				"FROM outro.action WHERE flow_id ='"+flowID+"' and switch_dpid ='"+switchDPID+"' ");
			while (rs.next()){ //Apenas 1 registro
				//Total Weight
				ResultSet rs2;
				Integer groupID = rs.getInt(1);
				Integer totalWeight = 0;
				rs2 = stmt2.executeQuery("SELECT SUM(weight)" +
						"FROM outro.bucketdesc WHERE switch_dpid ='"+switchDPID+"'" +
								"and group_id ='"+groupID+"' ");
				while (rs2.next()){ //Apenas 1 registro
					totalWeight = rs2.getInt(1);
				}
				//Port and Weight
				ResultSet rs3;
				rs3 = stmt3.executeQuery("SELECT port_number, weight" +
					" FROM outro.bucketdesc WHERE switch_dpid ='"+switchDPID+"'" +
							"and group_id ='"+groupID+"' ");
				while (rs3.next()){
					Integer portNumber = rs3.getInt(1);
					Integer weight = rs3.getInt(2);
					map = constructObjectJson.constructSwitchPortMap(switchDPID, 
							portNumber, weight, transmissionRate, totalWeight, map);
				}
			}
			stmt.close();
			stmt2.close();
			stmt3.close();
			return map;
	    }catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
