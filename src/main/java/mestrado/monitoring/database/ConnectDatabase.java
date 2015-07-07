package mestrado.monitoring.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectDatabase {
	
	private Connection connection = null;
	
	public Connection getConnection(){
		if(connection == null){
			connection = startConnection(); 
			if(connection == null){
				return null;
			}
			else{
				return connection;
			}
		}
		else{
			return connection;
		}
	}

	public Connection startConnection() {
		try {
			connection = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/postgres", "postgres",
					"postgres");
			return connection;

		} catch (SQLException e) {

			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return null;
		}
	}
	
	public Statement createStatement(){
		try {
			if(connection == null){
				startConnection();
			}
			return connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void closeStatement(Statement stmt){
		try {
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
