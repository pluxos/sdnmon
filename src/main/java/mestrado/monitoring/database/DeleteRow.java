package mestrado.monitoring.database;

/***
 * Não colocar os ACKS no BD nem no Storage! Por algum motivo o TCP não está funcionando OK quando o fluxo dura menos de 5 segundos, 
 * por isso mudei para 10 segundos a duração do fluxo. Acredito que tem algo haver com o Storage ...
 */

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class DeleteRow {
	
	public void deleteRowsTimeExpiration(Statement stmt){
			long time = Calendar.getInstance().getTimeInMillis();
			long maxAllowedInterval = 2000; // 2 seconds. Total almost 5.
					
			String deleteSQL = "DELETE FROM outro.match " +
					"WHERE (flow_id, switch_dpid) IN " +
					"(SELECT M.flow_id, M.switch_dpid FROM outro.match M INNER JOIN outro.flowstats F " +
					"ON F.flow_id = M.flow_id and F.switch_dpid = M.switch_dpid" +
					" WHERE time < " + (time - maxAllowedInterval) + ") ";
			try {
				stmt.execute(deleteSQL);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}	

	public void removeGroup(Statement stmt, Integer groupID){
		String sql = "DELETE FROM outro.bucketdesc" +
				" WHERE group_id = '"+groupID+"'";
		try {
			stmt.execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
