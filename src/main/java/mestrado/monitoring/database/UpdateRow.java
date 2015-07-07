package mestrado.monitoring.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import mestrado.flow.FlowStatistics;

public class UpdateRow {
	
	public boolean updateFlow(String switchID, Integer flowID, Statement stmt, FlowStatistics fs){
		try {
			String select = "SELECT * FROM outro.flowstats " +
	                     " WHERE switch_dpid = '"+switchID+"' and flow_id = '"+flowID+"'";
			ResultSet rs = stmt.executeQuery(select);
			if(rs.next()){
				String update = "UPDATE outro.flowstats" +
						 " SET packet_count='"+fs.getReceivedPackets()+"', " +
						 "byte_count='"+fs.getReceivedBytes()+"', " 
						 +"transmission_rate='"+fs.getCurrentTransmissionRate()
						 +"', time='"+fs.getTime()+"' " +
						 "WHERE switch_dpid = '"+switchID+"' and flow_id = '"+flowID+"'";
				stmt.executeUpdate(update);
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
