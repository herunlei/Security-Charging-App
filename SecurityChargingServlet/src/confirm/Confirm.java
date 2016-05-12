package confirm;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Confirm
 */
@WebServlet("/Confirm")
public class Confirm extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor. 
	 */
	public Confirm() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection conn;	//create connection with database

		try{
			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//try to connect to database
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/securitycharging?"
					+ "user=herunlei&password=8260");

			// Execute SQL query
			Statement stmt = conn.createStatement();
			String sql = "UPDATE customer SET Validation=1 WHERE Verify_Key='" + request.getParameter("token") + "'";
			System.out.println("The query is: "+sql);
			stmt.execute(sql);

			stmt.close();
			conn.close();

			System.out.println("Account confirmed!");
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		} 
		// TODO Auto-generated method stub
		response.getWriter().append("Congratulations!Your account is activated!");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
