package database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;

//do the query of DB2
public class Query {

	String url = "jdbc:db2://localhost:50000/SAMPLE";
	String user = "Zhitian";
	String password = "123456";
	Connection con;
	Statement stmt;
	Data data;

	/*
	 * public static void main(String[] args) { Query st = new Query();
	 * st.connection(); // st.query(); st.writetoFile(); st.close(); }
	 */
	public Query(Data data) {
		this.data = data;
	}

	public Query() {
	}

	public void connection() {
		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");

			// Create the connection using the IBM Data Server Driver for JDBC
			// and SQLJ
			con = DriverManager.getConnection(url, user, password);
			// Commit changes manually
			con.setAutoCommit(false);

			// Create the Statement
			stmt = con.createStatement();
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load JDBC driver");
			System.out.println("Exception: " + e);
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<ShapePolygon> idsQuery(HashSet<String> markupids){
		ArrayList<ShapePolygon> shapeList = new ArrayList<ShapePolygon>();
		try {
			String query = "select * from markup2";
			ResultSet rs = stmt.executeQuery(query.toUpperCase());
			while (rs.next()) {
				if(markupids.contains(rs.getString("markup_id").trim())){
					shapeList.add(new ShapePolygon(rs.getString("polygon")));
				}	
			}
			rs.close();

		} catch (SQLException ex) {
			System.err.println("SQLException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());
				System.err.println("SQLSTATE: " + ex.getSQLState());
				System.err.println("Error code: " + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException();
			}
		}
		return shapeList;
	}

	public void centerQuery() {
		ArrayList<String> clist = new ArrayList<String>();
		try {
			String query = "select * from markup2";
			ResultSet rs = stmt.executeQuery(query.toUpperCase());
			while (rs.next()) {
				ShapePolygon sp = new ShapePolygon(rs.getString("polygon"));
				clist.add(rs.getString("markup_id") + "," + sp.cx + "," + sp.cy);

			}
			// Close the ResultSet
			rs.close();

		} catch (SQLException ex) {
			System.err.println("SQLException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());
				System.err.println("SQLSTATE: " + ex.getSQLState());
				System.err.println("Error code: " + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException();
			}
		}
		this.write(clist, "data/centroid.txt");
	}

	public void matchShape(ArrayList<Double> sketchList) {
		data.clearShapes();
		try {
			String query = "select * from markup2 fetch first 100000 rows only";
			ResultSet rs = stmt.executeQuery(query.toUpperCase());
			while (rs.next()) {
				String[] turnings = rs.getString("turnings").trim().split(",");
				ArrayList<Double> tList = new ArrayList<Double>();

				for (int i = 0; i < turnings.length; i++) {
					tList.add(Double.parseDouble(turnings[i]));
				}
				double dis = ShapePolygon.turningDistant1D(tList, sketchList);
				if (dis < 9) {
					data.addShape(rs.getString("polygon"));
					// System.out.println(rs.getString("markup_id").trim());
				}
			}
			// Close the ResultSet
			rs.close();

		} catch (SQLException ex) {
			System.err.println("SQLException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());
				System.err.println("SQLSTATE: " + ex.getSQLState());
				System.err.println("Error code: " + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException();
			}
		}
	}

	public void close() {

		// Close the Statement
		try {
			stmt.close();

			// Connection must be on a unit-of-work boundary to allow close
			con.commit();

			// Close the connection
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// query from ECCENTRICITY, area, and MINOR_AXIS / MAJOR_AXIS
	private void queryTest() {
		try {
			// this is the query
			// String query =
			// "select features.markup_id, AREA , PERIMETER , ECCENTRICITY ,
			// CIRCULARITY , MAJOR_AXIS ,MINOR_AXIS , EXTENT_RATIO ,polygon from
			// features, markup where features.markup_id = markup.markup_id
			// order by features.major_axis desc fetch first 10 rows only";
			String query = "select * from markup";

			System.out.println("Query is: \"" + query + "\"");
			// Execute a query and generate a ResultSet instance
			ResultSet rs = stmt.executeQuery(query.toUpperCase());
			System.out.println("Result is:");
			// Print all of the employee numbers to standard output device
			while (rs.next()) {
				/*
				 * System.out.println(rs.getString("major_axis") + ";" +
				 * rs.getString("minor_axis") + ";" + rs.getString("polygon"));
				 */
				// String output = rs.getString("markup_id").trim() + ",\"" +
				// rs.getString("polygon").trim() + "\",\"";
				ShapePolygon s = new ShapePolygon(rs.getString("polygon"));

				ArrayList<Double> tList = s.turningList;
				ArrayList<Double> aList = s.angleList;
				String turnings = "\"";
				for (int i = 0; i < tList.size(); i++) {
					turnings += tList.get(i);
					if (i != aList.size() - 1) {
						turnings += ",";
					}
				}
				turnings += "\"";
				String angles = "\"";
				for (int i = 0; i < aList.size(); i++) {
					angles += aList.get(i);
					if (i != aList.size() - 1) {
						angles += ",";
					}
				}
				angles += "\"";
			}
			// Close the ResultSet
			rs.close();

		} catch (SQLException ex) {
			System.err.println("SQLException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());
				System.err.println("SQLSTATE: " + ex.getSQLState());
				System.err.println("Error code: " + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained
											// exceptions
			}
		}
	} // End main

	private void write(ArrayList<String> outputList, String fileName) {

		BufferedWriter bufferWriter = null;
		try {
			File file = new File(fileName);
			bufferWriter = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < outputList.size(); i++) {
				bufferWriter.write(outputList.get(i) + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferWriter != null)
				try {
					bufferWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	// query from ECCENTRICITY, area, and MINOR_AXIS / MAJOR_AXIS
	public void writetoFile() {
		try {
			// this is the query
			// String query =
			// "select features.markup_id, AREA , PERIMETER , ECCENTRICITY ,
			// CIRCULARITY , MAJOR_AXIS ,MINOR_AXIS , EXTENT_RATIO ,polygon from
			// features, markup where features.markup_id = markup.markup_id
			// order by features.major_axis desc fetch first 10 rows only";
			String query = "select * from markup";

			System.out.println("Query is: \"" + query + "\"");
			// Execute a query and generate a ResultSet instance
			ResultSet rs = stmt.executeQuery(query.toUpperCase());
			System.out.println("Result is:");
			// Print all of the employee numbers to standard output device
			int count = 0;
			ArrayList<String> outputList = new ArrayList<String>();
			while (rs.next()) {
				/*
				 * System.out.println(rs.getString("major_axis") + ";" +
				 * rs.getString("minor_axis") + ";" + rs.getString("polygon"));
				 */
				String output = rs.getString("markup_id").trim() + ",\"" + rs.getString("polygon").trim() + "\",\"";
				ShapePolygon s = new ShapePolygon(rs.getString("polygon"));

				ArrayList<Double> tList = s.turningList;
				for (int i = 0; i < tList.size(); i++) {
					output += tList.get(i);
					if (i != tList.size() - 1) {
						output += ",";
					}
				}
				output += "\",\"";
				ArrayList<Double> aList = s.angleList;
				for (int i = 0; i < aList.size(); i++) {
					output += aList.get(i);
					if (i != aList.size() - 1) {
						output += ",";
					}
				}
				output += "\"\n";
				// System.out.print(output);
				count++;
				outputList.add(output);
				if (count % 100000 == 0) {
					System.out.println(count + "," + output);
					write(outputList, "data/markup_" + count);
					outputList.clear();
				}
			}
			// Close the ResultSet
			write(outputList, "data/markup_" + count);
			outputList.clear();
			rs.close();

		} catch (SQLException ex) {
			System.err.println("SQLException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());
				System.err.println("SQLSTATE: " + ex.getSQLState());
				System.err.println("Error code: " + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained
											// exceptions
			}
		}
	} // End main
}