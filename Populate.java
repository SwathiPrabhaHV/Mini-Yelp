package hw3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * This program extracts the data from JSON format and populates it to a Oracle
 * database
 */
public class Populate {

    static Connection con = null;
    static ArrayList<String> main_category = new ArrayList<>();

    public static void main(String[] args) {
        openConnection(); //connect to the Oracle database
        dropTables(); //drop tables if the table already exists
        insertMainCategory(args[0]); //insert the category
        insertBusiness(args[0]); //insert business,subCatgeory,attributes
        insertYelpUser(args[1]); //insert the Yelp users
       insertReview(args[2]); //insert the reviews
    }

    //getConnection to the database
    public static Connection openConnection() {
           // Load the Oracle database driver 
        //DriverManager.registerDriver(new oracle.jdbc.OracleDriver()); 
        try {
            String host = "localhost";
            String port = "1521";
            String dbName = "ORCL";
            String userName = "Scott";
            String password = "tiger";

            // Construct the JDBC URL 
            String dbURL = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
            //getConnection 
            con = DriverManager.getConnection(dbURL, userName, password);
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
            return con;
        }

    }

    //method to insert main category names and id into Main_category table     
    private static void insertMainCategory(String filename) {

        //add all the main category elements
        main_category.add("Active Life");
        main_category.add("Arts & Entertainment");
        main_category.add("Automotive");
        main_category.add("Car Rental");
        main_category.add("Cafes");
        main_category.add("Beauty & Spas");
        main_category.add("Convenience Stores");
        main_category.add("Dentists");
        main_category.add("Doctors");
        main_category.add("Drugstores");
        main_category.add("Department Stores");
        main_category.add("Education");
        main_category.add("Event Planning & Services");
        main_category.add("Flowers & Gifts");
        main_category.add("Food");
        main_category.add("Health & Medical");
        main_category.add("Home Services");
        main_category.add("Home & Garden");
        main_category.add("Hospitals");
        main_category.add("Hotels & Travel");
        main_category.add("Hardware Stores");
        main_category.add("Grocery");
        main_category.add("Medical Centers");
        main_category.add("Nurseries & Gardening");
        main_category.add("Nightlife");
        main_category.add("Restaurants");
        main_category.add("Shopping");
        main_category.add("Transportation");

        JSONParser jsonParser = new JSONParser();
        String line = null;
        try {
            BufferedReader in
                    = new BufferedReader(
                            new FileReader(filename));

            line = in.readLine();
            int i = 0;

            while (line != null) {
                JSONObject obj = (JSONObject) jsonParser.parse(line);
                JSONArray jArray = (JSONArray) obj.get("categories");
                Iterator iterate = jArray.iterator();
                while (iterate.hasNext()) {
                    String getCategory = iterate.next().toString();
                    if (main_category.contains(getCategory)) {
                        PreparedStatement stmt = con.prepareStatement("Insert into main_category values(?,?)");
                        stmt.setString(1, getCategory);
                        stmt.setString(2, obj.get("business_id").toString());
                        stmt.executeUpdate();
                        System.out.println("main Category" + i);
                        stmt.close();
                    } else {
                        PreparedStatement stmtSubcategory = con.prepareStatement("Insert into sub_category values(?,?)");
                        stmtSubcategory.setString(1, getCategory);
                        stmtSubcategory.setString(2, obj.get("business_id").toString());
                        stmtSubcategory.executeUpdate();
                        System.out.println("Sub category" + i);
                        stmtSubcategory.close();
                    }

                }
                line = in.readLine();

            }
        } catch (ParseException | SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    //method to parse and insert business and attributes
    private static void insertBusiness(String fileName) {
        //call Json 
        JSONParser jsonParser = new JSONParser();
        String line = "";
        try (BufferedReader in
                = new BufferedReader(
                        new FileReader(fileName))) {
                    line = in.readLine();

                    int i = 0;
                    while (line != null) {
                        JSONObject obj = (JSONObject) jsonParser.parse(line);

                        obj = (JSONObject) jsonParser.parse(line);
                        PreparedStatement stmt = con.prepareStatement("INSERT INTO business VALUES(?,?,?,?,?,?,?,?,?,?,?)");

                        stmt.setString(1, obj.get("business_id").toString());
                        stmt.setString(2, obj.get("full_address").toString());
                        stmt.setString(3, obj.get("open").toString());
                        stmt.setString(4, obj.get("city").toString());
                        stmt.setString(5, obj.get("state").toString());
                        stmt.setDouble(6, (Double) obj.get("latitude"));
                        stmt.setDouble(7, (Double) obj.get("longitude"));
                        stmt.setLong(8, (Long) obj.get("review_count"));
                        stmt.setString(9, obj.get("name").toString());
                        stmt.setDouble(10, (Double) obj.get("stars"));
                        stmt.setString(11, obj.get("type").toString());
                        stmt.execute();
                        stmt.close();

/*---------------------------------attribute parsing--------------------------------------------------------------------------------------*/
                        PreparedStatement stmt3 = con.prepareStatement("INSERT INTO attributes VALUES(?,?)");
                        JSONObject attribute = (JSONObject) obj.get("attributes");
                        Iterator<Map.Entry<Object, Object>> iterator = attribute.entrySet().iterator();
              
                        while (iterator.hasNext()) {
                            Map.Entry<Object, Object> entry = iterator.next();

                            if (entry.getValue() instanceof JSONObject) {
                                JSONObject jo = (JSONObject) entry.getValue();
                                Iterator<Map.Entry<Object, Object>> iterator2 = jo.entrySet().iterator();
                                while (iterator2.hasNext()) {
                                    Map.Entry<Object, Object> entry1 = iterator2.next();
                                    String attribute_string = entry.getKey() + "_" + entry1.getKey() + "_" + entry1.getValue();
                                    stmt3.setString(1, attribute_string);
                                    stmt3.setString(2, obj.get("business_id").toString());
                                    stmt3.executeUpdate();
                                }
                            } else {
                                String attribute_string = entry.getKey() + "_" + entry.getValue();
                                stmt3.setString(1, attribute_string);
                                stmt3.setString(2, obj.get("business_id").toString());
                                stmt3.executeUpdate();
                            }
                        }
                        stmt3.close();
                        System.out.println("Attribute line" + (++i));
                        line = in.readLine();
                    }
                    in.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.out.println("File Not Found: " + line);
                } catch (ParseException | IOException e2) {
                    e2.printStackTrace();
                    System.out.println("Parse Exception : " + line);
                } catch (SQLException e3) {
                    e3.printStackTrace();
                    System.out.println("IOException : " + line);

                }
    }

    //method to parse and insert Yelp User into database
    private static void insertYelpUser(String fileName) {
        
        File f = new File(fileName);
        try (BufferedReader i2
                = new BufferedReader(
                        new FileReader(f.getAbsolutePath()))) {
                    JSONParser jsonParser = new JSONParser();
                    String line = i2.readLine();
                    int i = 0;
                    while (line != null) {
                        JSONObject obj = (JSONObject) jsonParser.parse(line);

                        PreparedStatement stmt = con.prepareStatement("INSERT INTO yelp_users VALUES(?,?,?,?,?,?,?,?,?,?)");
                        
                        stmt.setString(3, obj.get("yelping_since").toString());
                        stmt.setLong(4, (Long) obj.get("review_count"));
                        stmt.setString(2, obj.get("name").toString());
                        stmt.setString(1, obj.get("user_id").toString());
                        stmt.setLong(8, (Long) obj.get("fans"));
                        stmt.setDouble(5, (Double) obj.get("average_stars"));
                        
                        stmt.setString(9, obj.get("compliments").toString());
                        stmt.setString(10, obj.get("elite").toString());

                        JSONArray jArray = (JSONArray) obj.get("friends");
                        Iterator iterate = jArray.iterator();
                        long number_of_friends = 0;
                        while (iterate.hasNext()) {
                            number_of_friends = number_of_friends + 1;
                            iterate.next();

                        }
                        stmt.setLong(6, (Long) number_of_friends);
                        JSONObject votes_user = (JSONObject) obj.get("votes");
                        long funny = (Long) votes_user.get("funny");
                        long useful = (Long) votes_user.get("useful");
                        long cool = (Long) votes_user.get("cool");
                        long number_of_votes = funny + useful + cool;
                        stmt.setLong(7, (Long) number_of_votes);

                        stmt.executeUpdate();                             
                        stmt.close();                            
                        line = i2.readLine();
                        System.out.println(i++);
                    }
                    i2.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ParseException | IOException | SQLException e2) {
                    e2.printStackTrace();
                }
    }

    //method to parse and insert Reviews into database
    private static void insertReview(String fileName) {

        File f = new File(fileName);

        try (BufferedReader i2
                = new BufferedReader(
                        new FileReader(f.getAbsolutePath()))) {
                    JSONParser jsonParser = new JSONParser();
                    String line = i2.readLine();
                    int i = 0;
                    while (line != null) {
                        i++;
                        JSONObject obj = (JSONObject) jsonParser.parse(line);
                        PreparedStatement stmt = con.prepareStatement("INSERT INTO review VALUES(?,?,?,?,?,?,?,?)");

                        stmt.setString(1, obj.get("user_id").toString());
                        stmt.setString(2, obj.get("review_id").toString());
                        stmt.setLong(3, (Long) obj.get("stars"));
                        stmt.setString(4, obj.get("date").toString());
                        stmt.setString(5, obj.get("text").toString());
                        stmt.setString(6, obj.get("business_id").toString());
                        stmt.setString(7, obj.get("type").toString());                           
                       
                       // JSONObject votes = (JSONObject) obj.get("votes");
                        JSONObject votes_user = (JSONObject) obj.get("votes");
                        long funny = (Long) votes_user.get("funny");
                        long useful = (Long) votes_user.get("useful");
                        long cool = (Long) votes_user.get("cool");
                        long number_of_votes = funny + useful + cool;
                        stmt.setLong(8,  (Long) number_of_votes);
                       System.out.println("Review"+i);
                        stmt.executeUpdate();
                        stmt.close();
                      
                        line = i2.readLine();

                    }
                    i2.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ParseException | IOException | SQLException e2) {
                    e2.printStackTrace();
                }
    }

    //method to drop tables 
    private static void dropTables() {

        try {
            PreparedStatement stmtMainCategory = con.prepareStatement("drop table main_category ");
            stmtMainCategory.executeQuery();
            PreparedStatement stmtSubCategory = con.prepareStatement("drop table sub_category");
            stmtSubCategory.executeQuery();
            PreparedStatement stmtAttributes = con.prepareStatement("drop table attributes");
            stmtAttributes.executeQuery();
            PreparedStatement stmtyelp_users = con.prepareStatement("drop table yelp_users");
            stmtyelp_users.executeQuery();
            PreparedStatement stmt = con.prepareStatement("drop table business");
            stmt.executeQuery();
            PreparedStatement stmt2 = con.prepareStatement("drop table review");
            stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Populate.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
