package mestrado.monitoring.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import mestrado.flow.FlowStatistics;
import mestrado.monitoring.poll.Scope;

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

	public void updateThread(Statement stmt, Scope scope, String status) {
		// TODO Auto-generated method stub
			String sql;
			sql = "UPDATE outro.threads SET status='"+status+"'WHERE name='"+scope.getThreadName()+"'";
			try {
				stmt.executeUpdate(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
