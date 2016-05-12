package Server;

import Server.SupportFiles.TokenGenerator;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Token;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class Server implements Runnable {

    private static final int PORT = 8888;   //default port
    private boolean started;                //server status
    private boolean running;                //server status
    private ServerSocket serverSocket;      //server socket
    private Connection conn = null;         //connection with database

    //initialized server status
    private Server() {
        started = false;
        serverSocket = null;
    }

    public static void main(String args[]) {
        //stripe api key
        //need to replace when test or publish
        Stripe.apiKey = "sk_test_679LwhdIJInctpnlrByYVYib";
        //create server socket and start server
        Server server = new Server();
        server.start();
        server.connectDB();
    }

    //used for start server
    private void start() {
        if (!started) {
            started = true;

            try {
                serverSocket = new ServerSocket(PORT);
                running = true;

                Thread serverThread = new Thread(this);
                serverThread.start();

                System.out.println("Server started!\n");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    //establish connection with database
    private void connectDB() {
        LoadDriver();
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/securitycharging?autoReconnect=true" +
                    "&useSSL=false&user=herunlei&password=8260");

            if (conn != null) {
                System.out.println("Connection success!");
            }
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            System.out.println("The username/password is invalid!");
        }
    }

    private void LoadDriver() {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }
    }

    //cord part of server
    //used to establish connection with client
    public void run() {

        try {
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("Client Accepted!");

                    new ClientHandler(client);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //This is a handler to handler message sending form client and do the feed back based on require
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;
        private Thread runningThread;
        private boolean running;

        ClientHandler(Socket socket) {
            this.socket = socket;

            //create input and output stream for server
            try {
                writer = new PrintWriter(socket.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                running = true;

                runningThread = new Thread(this);
                runningThread.start();
            } catch (Exception e) {
                e.printStackTrace();
                disconnect();
            }
        }

        //used for disconnect
        void disconnect() {
            running = false;
            if (runningThread != null)
                runningThread.interrupt();
            runningThread = null;

            try {
                reader.close();
            } catch (Exception ignored) {
            }
            reader = null;

            try {
                writer.close();
            } catch (Exception ignored) {
            }
            writer = null;
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }

        //used for send message to client
        void sendMessage(String message) {
            if (running) {
                writer.println(message);
                writer.flush();
            }
        }

        //cord part of ClientHandler
        //in this part, it will do the all computing and send result message back to client
        public void run() {
            try {
                //initial received message and message that will send back to client
                String message;
                String received = null;

                //read message that is sent form client
                while ((message = reader.readLine()) != null && running) {
                    System.out.println("Client: " + message);

                    if (!message.equals("")) {
                        String[] temp = message.split(",");
                        switch (temp[0]) {
                            case "store":   //save payment information
                                received = "Payment saved//" + uploadPaymentDB(temp[1]);
                                break;
                            case "pay": //process payment
                                received = "Paid//" + uploadHistory(temp[1]);
                                break;
                            case "create":  //create account
                                received = "Create account//" + uploadCustomerDB(temp[1]);
                                break;
                            case "login":   //process login activity
                                received = "Account is validate//" + checkValidation(temp[1]);
                                break;
                            case "card":    //search card if it exists or not
                                received = "Card found//" + findCard(temp[1]);
                                break;
                            case "history": //collect payment history
                                received = "History found//" + findHistory(temp[1]);
                                break;
                            case "check":   //check if user has any payment method
                                received = "checked is//" + checkPayment(temp[1]);
                                break;
                        }
                    } else {
                        System.out.println("Message is empty!");
                        disconnect();
                    }

                    //send feed back to client
                    sendMessage(received);
                    System.out.println("Message sends to client: " + received);
                }
                disconnect();
                System.out.println("Client disconnected!");
            } catch (Exception e) {
                e.printStackTrace();
                disconnect();
            }
        }

        /**
         * @param result
         * @param message
         * @return
         */
        //get user token id from Stripe API
        private String getToken(String result, String message) {
            //create query and execute
            try {
                Statement statement = conn.createStatement();
                String query = "SELECT Token_ID FROM customer WHERE Username='" + message + "'";
                ResultSet resultSet = statement.executeQuery(query);
                //try to get result for executing query
                while (resultSet.next()) {
                    result = resultSet.getString("Token_ID");
                }
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return result;
        }

        /**
         * @param message
         * @return
         */
        //to check if user has any payment method
        private int checkPayment(String message) {
            String result = getToken(null, message);

            if (result != null) {
                return 1;
            }

            return 0;
        }

        /**
         * @param message
         * @return
         */
        //to get user payment card information
        private String findCard(String message) {
            String result = getToken(null, message);

            //if user has payment method
            //return payment card info with card type and card's last 4 digits
            if (result != null) {
                try {
                    Token token = Token.retrieve(result);
                    result = token.getCard().getBrand() + "-" + token.getCard().getLast4();
                } catch (AuthenticationException | InvalidRequestException | APIConnectionException | APIException | CardException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        /**
         * @param message
         * @return
         */
        //to collect all payment history of a user
        private String findHistory(String message) {
            String result = "";
            String id = requestID("customer", "ID", "Username", message);

            //create query and execute
            try {
                Statement statement = conn.createStatement();
                String query = "SELECT Invoice_Number, Payment_Date FROM history WHERE UserID='" + id + "'";
                ResultSet resultSet = statement.executeQuery(query);
                //try to get all payment history
                while (resultSet.next()) {
                    String result1 = resultSet.getString("Invoice_Number");
                    String result2 = resultSet.getString("Payment_Date");
                    result += result1 + "\t\t\t\t" + result2 + "/mark/";
                }
                resultSet.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (result.equals("")) {
                result = null;
            }

            return result;
        }

        /**
         * @param message
         * @return
         */
        //check if user account is valid or not when login
        private String checkValidation(String message) {
            String result = null;
            String[] info = message.split("//");
            try {
                Statement statement = conn.createStatement();
                String query = "SELECT Validation FROM customer WHERE Username='" + info[0]
                        + "' && Password='" + info[1] + "'";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    result = resultSet.getString("Validation");
                }
                resultSet.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        /**
         * @param message
         * @return
         */
        //check user account is exists or not
        private String[] checkAccount(String message) {
            String username = null;
            String email = null;
            String[] info = message.split("//");
            try {
                Statement statement1 = conn.createStatement();
                Statement statement2 = conn.createStatement();
                String query1 = "SELECT Username FROM customer WHERE Username='" + info[0] + "'";
                String query2 = "SELECT Email FROM customer WHERE Email='" + info[2] + "'";
                ResultSet resultSet1 = statement1.executeQuery(query1);
                ResultSet resultSet2 = statement2.executeQuery(query2);
                while (resultSet1.next()) {
                    username = resultSet1.getString("Username");
                }
                resultSet1.close();
                statement1.close();
                while (resultSet2.next()) {
                    email = resultSet2.getString("Email");
                }
                resultSet2.close();
                statement2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("The result return by database for username is: " + username);
            System.out.println("The result return by database for email is: " + email);
            return new String[]{username, email};
        }

        /**
         * @param message
         * @return
         */
        //record user payment history
        private int uploadHistory(String message) {
            String[] temp = message.split("//");
            String customer_ID = requestID("customer", "Customer_ID", "Username", temp[1]);

            // Charge the Customer instead of the card
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", temp[0] + "00"); // amount in cents, again
            chargeParams.put("currency", "cad");
            chargeParams.put("customer", customer_ID);

            try {
                Charge.create(chargeParams);

                int id = findLastID("ID", "history");
                int userId = Integer.parseInt(requestID("customer", "ID", "Username", temp[1]));

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                int seconds = calendar.get(Calendar.SECOND);

                int invoice = Integer.parseInt(String.valueOf(userId) + String.valueOf(seconds));

                // the mysql insert statement
                String query = "INSERT INTO history (ID, UserID, Amount_Paid, Captured, Currency, Disputed, " +
                        "Failure_Message, Invoice_Number, Paid, Payment_Date, Payment_Type, Refund," +
                        "Network, Reservation)"
                        + "VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt;
                preparedStmt = conn.prepareStatement(query);
                preparedStmt.setInt(1, id + 1);
                preparedStmt.setInt(2, userId);
                preparedStmt.setInt(3, Integer.parseInt(temp[0]));
                preparedStmt.setInt(4, 0);
                preparedStmt.setString(5, "cad");
                preparedStmt.setInt(6, 0);
                preparedStmt.setString(7, null);
                preparedStmt.setInt(8, invoice);
                preparedStmt.setInt(9, Integer.parseInt(temp[0]));
                preparedStmt.setString(10, dateFormat.format(calendar.getTime()));
                preparedStmt.setString(11, null);
                preparedStmt.setInt(12, 0);
                preparedStmt.setInt(13, 0);
                preparedStmt.setInt(14, 0);

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
                e.printStackTrace();
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }

            return 1;
        }

        /**
         * @param message
         * @return
         */
        //record  new account created
        private boolean uploadCustomerDB(String message) {
            String[] info = message.split("//");
            String[] result = checkAccount(message);
            if (result[0] == null && result[1] == null) {
                try {
                    int ID = findLastID("ID", "customer");

                    // the mysql insert statement
                    String query = "INSERT INTO customer (ID, Username, Password, Email, " +
                            "Customer_ID, Token_ID, Validation, Verify_Key)"
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                    // create the mysql insert preparedstatement
                    PreparedStatement preparedStmt;
                    preparedStmt = conn.prepareStatement(query);
                    preparedStmt.setInt(1, ID + 1);
                    preparedStmt.setString(2, info[0]);
                    preparedStmt.setString(3, info[1]);
                    preparedStmt.setString(4, info[2]);
                    preparedStmt.setString(5, null);
                    preparedStmt.setString(6, null);
                    preparedStmt.setInt(7, 0);
                    preparedStmt.setString(8, null);

                    // execute the preparedstatement
                    preparedStmt.execute();
                    preparedStmt.close();
                    sendEmail(info[0], info[2]);
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        /**
         * @param message
         * @return
         */
        //save user payment method
        private int uploadPaymentDB(String message) {
            try {
                String[] temp = message.split("//");

                // Create a Customer
                Map<String, Object> customerParams = new HashMap<>();
                customerParams.put("source", temp[0]);
                customerParams.put("description", temp[1]);

                Customer customer = Customer.create(customerParams);

                updater("customer", "Customer_ID", customer.getId(), "Username", temp[1]);
                updater("customer", "Token_ID", temp[0], "Username", temp[1]);
            } catch (Exception h) {
                h.printStackTrace();
                return 0;
            }
            return 1;
        }

        /**
         * @param userID
         * @param table
         * @return
         */
        //to search latest created user id
        private int findLastID(String userID, String table) {
            int ID = 0;

            try {
                Statement statement = conn.createStatement();
                String query = "SELECT " + userID + " FROM " + table + " ORDER BY ID DESC LIMIT 1";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    ID = resultSet.getInt("ID");
                }
                resultSet.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ID;
        }

        /**
         * @param table
         * @param col1
         * @param col2
         * @param col3
         * @return
         */
        //to find the id for something in specified table
        private String requestID(String table, String col1, String col2, String col3) {
            String result = null;

            try {
                Statement statement = conn.createStatement();
                String query = "SELECT " + col1 + " FROM " + table + " WHERE " + col2 + "='" + col3 + "'";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    result = resultSet.getString(col1);
                }
                resultSet.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        /**
         * @param name
         * @param email
         */
        //send verify email to user's email with verify link that contain unique verify token
        private void sendEmail(String name, String email) {
            //server email and password that use to send email
            //this is a personal gemail account
            //need to replace another email
            String username = "jamesherunlei@gmail.com";
            String password = "82607091023";

            //set property of who send verify email and information about email server
            Properties props = new Properties();
            props.put("mail.smtp.user", "Security Charging Support");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.debug", "true");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");

            //security check of server email account
            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
            session.setDebug(true);

            MimeMessage msg = new MimeMessage(session);

            //generate unique verify token
            TokenGenerator tokenGenerator = new TokenGenerator();
            String[] info = tokenGenerator.generateToken(name).split(":");

            //generate verify link
            String url = "http://172.20.10.3:8080/SecurityChargingServlet/Confirm?token=" + info[1];

            //update user payment method
            updater("customer", "Verify_Key", info[1], "Username", info[0]);

            //set verify email content
            //and send email to user
            try {
                msg.setSubject("Security Charging App sign up confirmation");
                msg.setFrom(new InternetAddress("support"));
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                msg.setText("Hi, " + info[0] + "\n\nThis message is from Security Charging App!"
                        + "\n\nPlease click follow link to confirm: " + url + "\n\nYour Security Charging App team");

                Transport transport = session.getTransport("smtps");
                transport.connect("smtp.gmail.com", Integer.valueOf("465"), username, password);
                transport.sendMessage(msg, msg.getAllRecipients());
                transport.close();

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        /**
         * @param table
         * @param col1
         * @param content1
         * @param col2
         * @param content2
         */
        //use to update database
        private void updater(String table, String col1, String content1, String col2, String content2) {
            Statement statement;
            try {
                statement = conn.createStatement();
                String query = "UPDATE " + table + " SET " + col1 + "='" + content1
                        + "' WHERE " + col2 + "='" + content2 + "'";
                System.out.println(query);
                statement.execute(query);
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
