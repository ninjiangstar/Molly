package sql;

import java.sql.Statement;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class startDatabase extends Thread{
	
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost?user=root&password=root&useSSL = false";

	
		/** A very basic SQL script runner
		 * @param filename the sql script to run
		 */
		private void runScript(String filename) {
			BufferedReader br = null;
			FileReader fr = null;
			Connection conn = null;
			Statement st = null;
			try {
				Class.forName(JDBC_DRIVER);
				conn = DriverManager.getConnection(DB_URL);
				st = conn.createStatement();
				fr = new FileReader(filename);
				br = new BufferedReader(fr);
				String line = "", multiLineStatement = "";
				boolean multiLine = false;
				while ((line = br.readLine()) != null) {
					// Basic support to ignore blank lines and comment lines
					if (!line.equals("")) {
						if (line.length() >= 2 && line.charAt(line.length()-1) == '(') {
							multiLine = true;
						} else if (multiLine && line.equals(");")) {
							multiLine = false;
							st.execute(multiLineStatement + ");");
							multiLineStatement = "";
						} else if (!multiLine) {
							st.execute(line);
						}
						
						if (multiLine) {
							multiLineStatement += line;
						}
						
					}
				}
				System.out.println("Successfully ran SQL script: " + filename);
			} catch (FileNotFoundException fnfe) {
				System.out.println("Startup fnfe: " + fnfe.getMessage());
			} catch (IOException ioe) {
				System.out.println("Startup ioe: " + ioe.getMessage());
			} catch (SQLException sqle) {
				System.out.println ("Startup SQLException: ");
				sqle.printStackTrace();
			} catch (ClassNotFoundException cnfe) {
				System.out.println ("Startup ClassNotFoundException: " + cnfe.getMessage());
			}  finally {
				try {
					if (br != null) {
						br.close();
					}
				} catch (IOException e) { /* Do nothing */ }
				try {
					if (fr != null) {
						fr.close();
					}
				} catch (IOException e) { /* Do nothing */ }
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException e) { /* Do nothing */ }
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) { /* Do nothing */ }
			}
		}
		
		
		@Override
		public void run() {
			runScript("src/sql/Spotify.sql");
			System.out.println("run in startDatabase");
			
		}
		
		public static void main(String[] args) {
			startDatabase s = new startDatabase();
			s.start();
		}
		
	
	}
	

