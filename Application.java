/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw3;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.CheckBox;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.json.simple.parser.ParseException;

/**
 *
 * @author swath
 */
public class Hw3 extends javax.swing.JFrame {

    String MainButtonSelected = null;
    Connection connection = null;
    List<String> selectedCategories = new ArrayList<>();
    List<String> selectedSubCategories = new ArrayList<>();
    List<String> selectedAttributes = new ArrayList<>();
    ArrayList<JCheckBox> mainCategory = null;
    ArrayList<JCheckBox> subCategoryCheckBox = null;
    ArrayList<JCheckBox> attributeCheckBox = null;
    String whereClauseCategory = null;
    String whereClauseSubCategory = null;
    String whereClauseAttributes = null;
    String setUserQuery = "";
    String mainCategoryQuery = null;
    String subCategoryQuery = null;
    String attributeQuery = null;
    String setReviewQuery = null;
    String innerMaincategory = null;

    String innerSubcategory = null;

    public Hw3() {
        initComponents();
        setExtendedState(Hw3.MAXIMIZED_BOTH);
        connection = Populate.openConnection();
        if (connection != null) {
            System.out.println("Connection Successful");
        } else {
            System.out.println("No luck!! Try again!!");
        }

        try {
            PreparedStatement stmtMainCategory = connection.prepareStatement("Select distinct name from main_category");
            ResultSet rs = stmtMainCategory.executeQuery();
            mainCategory = new ArrayList<>();

            while (rs.next()) {
                JCheckBox checkBoxMain = new JCheckBox();
                checkBoxMain.setText(rs.getString(1));
                ActionListener actionListenerCategory = new ActionHandlerCategory();
                checkBoxMain.addActionListener(actionListenerCategory);
                mainCategory.add(checkBoxMain);
            }

            stmtMainCategory.close();
            CategoryPanel.setLayout(new GridLayout(0, 1, 5, 5));
            for (JCheckBox ch : mainCategory) {
                CategoryPanel.add(ch);
                CategoryPanel.revalidate();
                CategoryPanel.repaint();
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }//end of Hw3 constructor

    //
    private void getsubCategory() throws SQLException {
        String sql = null;
        //get the andOrSelector
        String BusinessAndOrSelector = Business_Search_AND_OR.getSelectedItem().toString();

        if (subCategoryCheckBox != null) {
            for (JCheckBox ch : subCategoryCheckBox) {
                SubCategoryPanel.remove(ch);
                SubCategoryPanel.repaint();
                SubCategoryPanel.revalidate();
                //SubCategoryPanel.updateUI();
            }

        }
        subCategoryQuery = "select s.name,s.business_id from sub_category s where s.business_id in(";
        innerMaincategory = "(Select distinct m.business_id from main_category m where m.name in(";

        //check if the consition in And or OR
        if (Business_Search_AND_OR.getSelectedItem().toString() == "OR") {

            whereClauseCategory = "";

            for (int i = 0; i < selectedCategories.size(); i++) {
                if (i == 0) {
                    this.whereClauseCategory = "'" + selectedCategories.get(i) + "'";
                } else {
                    this.whereClauseCategory += "," + "'" + selectedCategories.get(i) + "'";
                }
            }

            innerMaincategory = innerMaincategory + this.whereClauseCategory + "))";
        } else if (Business_Search_AND_OR.getSelectedItem().toString() == "AND") {
            //  }
            for (int i = 0; i < selectedCategories.size(); i++) {
                if (i == 0) {
                    this.whereClauseCategory = "'" + selectedCategories.get(i) + "'";
                } else {
                    this.whereClauseCategory += "," + "'" + selectedCategories.get(i) + "'";
                }
            }
            innerMaincategory = innerMaincategory + this.whereClauseCategory + ") group by m.business_id having count(distinct m.name)=" + (selectedCategories.size()) + ")";
        }
        try {
            subCategoryQuery = subCategoryQuery + innerMaincategory;
            sql = "select distinct name from( " + subCategoryQuery + "))";
            subCategoryQuery = "select distinct business_id from (" + subCategoryQuery + "))";
            System.out.println(subCategoryQuery);

            PreparedStatement stmtSubCategory = connection.prepareStatement(sql);
            System.out.println(sql);

            ResultSet rsSubCategory = stmtSubCategory.executeQuery();
            subCategoryCheckBox = new ArrayList<>();
            while (rsSubCategory.next()) {
                JCheckBox checkBoxMain = new JCheckBox();
                checkBoxMain.setText(rsSubCategory.getString(1));
                ActionListener actionListener = new ActionHandlerSubCategory();
                checkBoxMain.addActionListener(actionListener);
                subCategoryCheckBox.add(checkBoxMain);
            }
            stmtSubCategory.close();
            SubCategoryPanel.setLayout(new GridLayout(0, 1, 5, 5));
            for (JCheckBox ch : subCategoryCheckBox) {
                SubCategoryPanel.add(ch);
                SubCategoryPanel.revalidate();
                SubCategoryPanel.repaint();
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    private void getAttributes() {
        String sql = null;
        String outerattributeQuery = "";
        String BusinessAndOrSelector = Business_Search_AND_OR.getSelectedItem().toString();

        if (attributeCheckBox != null) {
            for (JCheckBox ch : attributeCheckBox) {
                AttributePanel.remove(ch);
                AttributePanel.repaint();
                AttributePanel.revalidate();
            }
        }
        attributeQuery = "select  a.attribute_name,a.business_id from attributes a "
                + "where a.business_id in(";
        innerSubcategory = "select distinct s.business_id from sub_category s where s.name in(";

        if (Business_Search_AND_OR.getSelectedItem().toString() == "OR") {
            for (int j = 0; j < selectedSubCategories.size(); j++) {
                if (j == 0) {
                    this.whereClauseSubCategory = "'" + selectedSubCategories.get(j) + "'";
                } else {
                    this.whereClauseSubCategory += ", " + "'" + selectedSubCategories.get(j) + "'";
                }
            }

            innerSubcategory = innerSubcategory + this.whereClauseSubCategory + ") and s.business_id in(select distinct m.business_id from "
                    + "main_category m where m.name in(" + this.whereClauseCategory + ")))";
            attributeQuery = attributeQuery + innerSubcategory;

        } else {
            for (int j = 0; j < selectedSubCategories.size(); j++) {

                if (j == 0) {
                    this.whereClauseSubCategory = "'" + selectedSubCategories.get(j) + "'";
                } else {
                    this.whereClauseSubCategory += "," + "'" + selectedSubCategories.get(j) + "'";
                }
            }

            innerSubcategory = innerSubcategory + this.whereClauseSubCategory + ") and s.business_id in(SELECT  distinct m.business_id\n"
                    + "                     FROM main_category m\n"
                    + "                WHERE m.name IN (" + this.whereClauseCategory + ")  group by m.business_id having count(distinct m.name)=" + (selectedCategories.size()) + ")group by s.business_id having "
                    + "count(distinct s.name)=" + selectedSubCategories.size() + ")";
            attributeQuery = attributeQuery + innerSubcategory;

        }

        outerattributeQuery = "select  distinct attribute_name from(" + attributeQuery + ")";
        System.out.print("Attribute Query" + attributeQuery);
        System.out.println("InnerQuery: " + innerSubcategory);
        try {
            PreparedStatement stmtAttribute = connection.prepareStatement(outerattributeQuery);

            ResultSet rsAttribute = stmtAttribute.executeQuery();
            attributeCheckBox = new ArrayList<>();
            while (rsAttribute.next()) {
                JCheckBox checkBoxMain = new JCheckBox();
                checkBoxMain.setText(rsAttribute.getString(1));
                ActionListener actionListener = new ActionHandlerAttribute();
                checkBoxMain.addActionListener(actionListener);
                attributeCheckBox.add(checkBoxMain);
            }
            stmtAttribute.close();
            AttributePanel.setLayout(new GridLayout(0, 1, 5, 5));
            for (JCheckBox ch2 : attributeCheckBox) {
                AttributePanel.add(ch2);
                AttributePanel.revalidate();
                AttributePanel.repaint();
            }

        }//end of try block//end of try block
        catch (SQLException e) {
            e.printStackTrace();

        }// end of catch block

    }

    // User Query
    private String getLoad_User() {

        if (UserRadioButton.isSelected()) {
            boolean flag = false;
            setUserQuery = "select user_id,name,review_count,average_stars from yelp_users";
            String yelpingSince = ((JTextField) MemberSinceDateChooser.getDateEditor().getUiComponent()).getText();
            String ReviewCount = Review_Count_value_text.getText();
            String Friends = Number_of_Friends_text.getText();
            String AverageStars = Average_Stars_value_text.getText();
            String AndOrSelector = AND_OR_User.getSelectedItem().toString();
            String NumberOfVotes = Number_of_votes_value_text.getText();

            if (!yelpingSince.equals("")) {
                try {
                    setUserQuery = setUserQuery + " where YELPING_SINCE > '" + yelpingSince + "' ";
                    flag = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!ReviewCount.equals("")) {
                String sysmbol = Review_Count_Checkbox.getSelectedItem().toString();
                if (flag) {
                    setUserQuery = setUserQuery + "  " + AndOrSelector + " REVIEW_COUNT " + sysmbol + " " + ReviewCount + " ";
                } else {
                    setUserQuery = setUserQuery + " where " + " REVIEW_COUNT " + sysmbol + " " + ReviewCount + " ";
                    flag = true;
                }

            }
            if (!Friends.equals("")) {
                String sysmbol = Number_of_Friends_Checkbox.getSelectedItem().toString();
                if (flag) {
                    setUserQuery = setUserQuery + "  " + AndOrSelector + " FRIEND_COUNT " + sysmbol + " " + Friends + " ";
                } else {
                    setUserQuery = setUserQuery + " where " + " FRIEND_COUNT " + sysmbol + " " + Friends + " ";
                    flag = true;
                }

            }
            if (!AverageStars.equals("")) {
                String sysmbol = average_stars.getSelectedItem().toString();
                if (flag) {
                    setUserQuery = setUserQuery + "  " + AndOrSelector + " AVERAGE_STARS " + sysmbol + " " + AverageStars + " ";
                } else {
                    setUserQuery = setUserQuery + " where " + " AVERAGE_STARS " + sysmbol + " " + AverageStars + " ";
                    flag = true;
                }

            }
            if (!NumberOfVotes.equals("")) {
                String sysmbol = Number_of_Votes_ComboBox.getSelectedItem().toString();
                if (flag) {
                    setUserQuery = setUserQuery + "  " + AndOrSelector + " VOTES_COUNT " + sysmbol + " " + NumberOfVotes + " ";
                } else {
                    setUserQuery = setUserQuery + " where VOTES_COUNT " + sysmbol + " " + NumberOfVotes + " ";
                    flag = true;
                }
            }
            return setUserQuery;
        }

        return null;
    }//end of getLoad_User;

    private void printUser(String Query, boolean isUserQurey) throws SQLException {
        try {
            QueryResultTextArea.setText(Query);
            Statement statement = this.connection.createStatement();
            System.out.println("Statement created");
            if (isUserQurey) {
                statement.executeUpdate("alter session set nls_date_format = 'yyyy-MM'");
            } else {
                statement.executeUpdate("alter session set nls_date_format = 'yyyy-MM-dd'");
            }
            ResultSet rs = statement.executeQuery(Query);
            ResultSetMetaData rsmd = rs.getMetaData();
            DefaultTableModel model = new DefaultTableModel();
            model.setColumnCount(rsmd.getColumnCount());
            Vector<String> cols = new Vector();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                cols.add(rsmd.getColumnName(i + 1));
            }
            model.setColumnIdentifiers(cols);
            while (rs.next()) {
                Vector<String> rows = new Vector();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    rows.add(rs.getString(rsmd.getColumnName(i + 1)));
                }
                model.addRow(rows);
            }
            QueryResultTable.setModel(model);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void printBusiness(String Query, boolean isBusinessSelected) throws SQLException {
        try {
            QueryResultTextArea.setText(Query);
            Statement statement = this.connection.createStatement();
            System.out.println("Statement created");
            if (isBusinessSelected) {
                statement.executeUpdate("alter session set nls_date_format = 'yyyy-MM-dd'");

            }
            ResultSet rs = statement.executeQuery(Query);
            ResultSetMetaData rsmd = rs.getMetaData();
            DefaultTableModel model = new DefaultTableModel();
            model.setColumnCount(rsmd.getColumnCount());
            Vector<String> cols = new Vector();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                cols.add(rsmd.getColumnName(i + 1));
            }
            model.setColumnIdentifiers(cols);
            while (rs.next()) {
                Vector<String> rows = new Vector();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    rows.add(rs.getString(rsmd.getColumnName(i + 1)));
                }
                model.addRow(rows);
            }
            QueryResultTable.setModel(model);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        Business_User_Group = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        BusinessRadioButton = new javax.swing.JRadioButton();
        UserRadioButton = new javax.swing.JRadioButton();
        BusinessPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        CategoryPanel = new javax.swing.JPanel();
        Business_Search_AND_OR = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        SubCategoryPanel = new javax.swing.JPanel();
        AttributePanelScroll = new javax.swing.JScrollPane();
        AttributePanel = new javax.swing.JPanel();
        User_Panel = new javax.swing.JPanel();
        Number_of_Friends_Checkbox = new javax.swing.JComboBox();
        Review_Count_Checkbox = new javax.swing.JComboBox();
        average_stars = new javax.swing.JComboBox();
        Number_of_Votes_ComboBox = new javax.swing.JComboBox();
        yelping_since_value = new javax.swing.JLabel();
        Number_of_Votes = new javax.swing.JLabel();
        Review_Count = new javax.swing.JLabel();
        Number_of_Friends = new javax.swing.JLabel();
        Average_Stars = new javax.swing.JLabel();
        yelping_since1 = new javax.swing.JLabel();
        Review_Count_value = new javax.swing.JLabel();
        Number_of_Friends_value = new javax.swing.JLabel();
        Average_Stars_value = new javax.swing.JLabel();
        Number_of_Votes_value = new javax.swing.JLabel();
        Review_Count_value_text = new javax.swing.JTextField();
        Member_Since_value_text = new javax.swing.JTextField();
        Average_Stars_value_text = new javax.swing.JTextField();
        Number_of_Friends_text = new javax.swing.JTextField();
        Number_of_votes_value_text = new javax.swing.JTextField();
        AND_OR_User = new javax.swing.JComboBox();
        MemberSinceDateChooser = new com.toedter.calendar.JDateChooser();
        Execute_Query_Button = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        QueryResultTextArea = new javax.swing.JTextArea();
        ReviewPanel = new javax.swing.JPanel();
        reviewFromDate = new com.toedter.calendar.JDateChooser();
        reviewToDate = new com.toedter.calendar.JDateChooser();
        jLabel1 = new javax.swing.JLabel();
        reviewStarsCombo = new javax.swing.JComboBox();
        starsLabel = new javax.swing.JLabel();
        reviewVotesCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        reviewStarsValue = new java.awt.TextField();
        reviewVotesValue = new java.awt.TextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        QueryResultTable = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        ReviewTextField = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Business_User_Group.add(BusinessRadioButton);
        BusinessRadioButton.setText("Business");
        BusinessRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BusinessRadioButtonActionPerformed(evt);
            }
        });

        Business_User_Group.add(UserRadioButton);
        UserRadioButton.setText("User");
        UserRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UserRadioButtonActionPerformed(evt);
            }
        });

        BusinessPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Business_Search", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        BusinessPanel.setMaximumSize(new java.awt.Dimension(763, 456));
        BusinessPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane2.setMaximumSize(new java.awt.Dimension(500, 492));

        CategoryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Categories", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        CategoryPanel.setMaximumSize(new java.awt.Dimension(500, 490));

        javax.swing.GroupLayout CategoryPanelLayout = new javax.swing.GroupLayout(CategoryPanel);
        CategoryPanel.setLayout(CategoryPanelLayout);
        CategoryPanelLayout.setHorizontalGroup(
            CategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 285, Short.MAX_VALUE)
        );
        CategoryPanelLayout.setVerticalGroup(
            CategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 465, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(CategoryPanel);

        BusinessPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 231, 369));

        Business_Search_AND_OR.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AND", "OR" }));
        BusinessPanel.add(Business_Search_AND_OR, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 18, 85, -1));

        jScrollPane1.setMaximumSize(new java.awt.Dimension(239, 369));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(239, 369));

        SubCategoryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Subcategories"));
        SubCategoryPanel.setMaximumSize(new java.awt.Dimension(237, 367));
        SubCategoryPanel.setMinimumSize(new java.awt.Dimension(237, 367));

        javax.swing.GroupLayout SubCategoryPanelLayout = new javax.swing.GroupLayout(SubCategoryPanel);
        SubCategoryPanel.setLayout(SubCategoryPanelLayout);
        SubCategoryPanelLayout.setHorizontalGroup(
            SubCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 237, Short.MAX_VALUE)
        );
        SubCategoryPanelLayout.setVerticalGroup(
            SubCategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 367, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(SubCategoryPanel);
        SubCategoryPanel.getAccessibleContext().setAccessibleName("Sub-Categories");

        BusinessPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(261, 54, 230, 370));

        AttributePanelScroll.setMaximumSize(new java.awt.Dimension(227, 369));
        AttributePanelScroll.setMinimumSize(new java.awt.Dimension(227, 369));

        AttributePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Attributes"));
        AttributePanel.setMaximumSize(new java.awt.Dimension(225, 367));
        AttributePanel.setMinimumSize(new java.awt.Dimension(225, 367));

        javax.swing.GroupLayout AttributePanelLayout = new javax.swing.GroupLayout(AttributePanel);
        AttributePanel.setLayout(AttributePanelLayout);
        AttributePanelLayout.setHorizontalGroup(
            AttributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 225, Short.MAX_VALUE)
        );
        AttributePanelLayout.setVerticalGroup(
            AttributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 367, Short.MAX_VALUE)
        );

        AttributePanelScroll.setViewportView(AttributePanel);
        AttributePanel.getAccessibleContext().setAccessibleName("Attributes");

        BusinessPanel.add(AttributePanelScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 50, 210, 370));

        User_Panel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User Search", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        User_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Number_of_Friends_Checkbox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "select", "=", ">", "<", " " }));
        Number_of_Friends_Checkbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Number_of_Friends_CheckboxActionPerformed(evt);
            }
        });
        User_Panel.add(Number_of_Friends_Checkbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 170, 150, -1));

        Review_Count_Checkbox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "select", "=", ">", "<", " " }));
        Review_Count_Checkbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Review_Count_CheckboxActionPerformed(evt);
            }
        });
        User_Panel.add(Review_Count_Checkbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 130, 150, -1));

        average_stars.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "select", "=", ">", "<" }));
        average_stars.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                average_starsActionPerformed(evt);
            }
        });
        User_Panel.add(average_stars, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 210, 150, -1));

        Number_of_Votes_ComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "select", "=", ">", "<" }));
        Number_of_Votes_ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Number_of_Votes_ComboBoxActionPerformed(evt);
            }
        });
        User_Panel.add(Number_of_Votes_ComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 250, 150, -1));

        yelping_since_value.setBackground(new java.awt.Color(255, 255, 255));
        yelping_since_value.setText("Value:");
        User_Panel.add(yelping_since_value, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 90, 50, -1));

        Number_of_Votes.setText("Number of Votes ");
        User_Panel.add(Number_of_Votes, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 250, -1, -1));

        Review_Count.setText("Review Count");
        User_Panel.add(Review_Count, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 130, 142, -1));

        Number_of_Friends.setText("Number of Friends");
        User_Panel.add(Number_of_Friends, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 170, 122, -1));

        Average_Stars.setText("Average Stars");
        User_Panel.add(Average_Stars, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 210, -1, -1));

        yelping_since1.setBackground(new java.awt.Color(255, 255, 255));
        yelping_since1.setText("Member Since:");
        User_Panel.add(yelping_since1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 90, 136, -1));

        Review_Count_value.setText("Value:");
        User_Panel.add(Review_Count_value, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 130, 50, -1));

        Number_of_Friends_value.setText("Value:");
        User_Panel.add(Number_of_Friends_value, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 170, -1, -1));

        Average_Stars_value.setText("Value:");
        User_Panel.add(Average_Stars_value, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 210, -1, -1));

        Number_of_Votes_value.setText("Value:");
        User_Panel.add(Number_of_Votes_value, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 250, -1, -1));

        Review_Count_value_text.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Review_Count_value_textActionPerformed(evt);
            }
        });
        User_Panel.add(Review_Count_value_text, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 130, 103, -1));

        Member_Since_value_text.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Member_Since_value_textActionPerformed(evt);
            }
        });
        User_Panel.add(Member_Since_value_text, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 90, 103, -1));
        User_Panel.add(Average_Stars_value_text, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 210, 103, -1));

        Number_of_Friends_text.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Number_of_Friends_textActionPerformed(evt);
            }
        });
        User_Panel.add(Number_of_Friends_text, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 170, 103, -1));
        User_Panel.add(Number_of_votes_value_text, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 250, 103, -1));

        AND_OR_User.setMaximumRowCount(2);
        AND_OR_User.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AND", "OR" }));
        AND_OR_User.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AND_OR_UserActionPerformed(evt);
            }
        });
        User_Panel.add(AND_OR_User, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 50, -1, -1));

        MemberSinceDateChooser.setDateFormatString("YYYY-MM\n\n");
        User_Panel.add(MemberSinceDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 90, 150, -1));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addComponent(BusinessRadioButton)
                                .addGap(42, 42, 42)
                                .addComponent(UserRadioButton))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(36, 36, 36)
                                .addComponent(User_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, 605, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 118, Short.MAX_VALUE))
                    .addComponent(BusinessPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BusinessRadioButton)
                    .addComponent(UserRadioButton))
                .addGap(18, 18, 18)
                .addComponent(BusinessPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 434, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(User_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(76, Short.MAX_VALUE))
        );

        Execute_Query_Button.setText("Execute Query");
        Execute_Query_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Execute_Query_ButtonActionPerformed(evt);
            }
        });

        QueryResultTextArea.setColumns(20);
        QueryResultTextArea.setRows(5);
        jScrollPane3.setViewportView(QueryResultTextArea);

        ReviewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Review", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        ReviewPanel.setMaximumSize(new java.awt.Dimension(500, 800));
        ReviewPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ReviewPanelMouseClicked(evt);
            }
        });
        ReviewPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        reviewFromDate.setDateFormatString("YYYY-MM-dd");
        ReviewPanel.add(reviewFromDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(81, 45, 143, -1));

        reviewToDate.setDateFormatString("YYYY-MM-dd");
        ReviewPanel.add(reviewToDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(81, 85, 143, -1));

        jLabel1.setText("from");
        ReviewPanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 51, 58, -1));

        reviewStarsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ">", "<", "=" }));
        ReviewPanel.add(reviewStarsCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 130, 100, -1));

        starsLabel.setText("stars");
        ReviewPanel.add(starsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 88, -1));

        reviewVotesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ">", "<", "=" }));
        ReviewPanel.add(reviewVotesCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 280, 100, -1));

        jLabel2.setText("Votes");
        ReviewPanel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 286, -1, -1));

        reviewStarsValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reviewStarsValueActionPerformed(evt);
            }
        });
        ReviewPanel.add(reviewStarsValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 230, 100, -1));
        ReviewPanel.add(reviewVotesValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 390, 100, -1));

        jLabel3.setText("value");
        ReviewPanel.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 235, -1, -1));

        jLabel4.setText("value");
        ReviewPanel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 396, -1, -1));

        jLabel5.setText("to");
        ReviewPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 91, 41, -1));

        QueryResultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        QueryResultTable.setCellSelectionEnabled(true);
        QueryResultTable.setFocusCycleRoot(true);
        QueryResultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                QueryResultTableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(QueryResultTable);

        jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane6.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane6.setAutoscrolls(true);
        jScrollPane6.setPreferredSize(new java.awt.Dimension(100, 2000));

        ReviewTextField.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane6.setViewportView(ReviewTextField);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(64, 64, 64)
                        .addComponent(ReviewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(95, 95, 95)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 738, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 459, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(Execute_Query_Button, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(47, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(577, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane4)
                    .addComponent(ReviewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(Execute_Query_Button)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ReviewPanel.getAccessibleContext().setAccessibleName("Review ");

        pack();
    }// </editor-fold>                        

    private void Number_of_Friends_CheckboxActionPerformed(java.awt.event.ActionEvent evt) {                                                           
        // TODO add your handling code here:
        System.out.println(Number_of_Friends_Checkbox.getSelectedItem());
    }                                                          

    private void Review_Count_CheckboxActionPerformed(java.awt.event.ActionEvent evt) {                                                      
        // TODO add your handling code here:
        System.out.println(Review_Count_Checkbox.getSelectedItem());
    }                                                     

    private void average_starsActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // TODO add your handling code here:
        System.out.println(average_stars.getSelectedItem());
    }                                             

    private void Number_of_Votes_ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {                                                         
        // TODO add your handling code here:
        System.out.println(Number_of_Votes_ComboBox.getSelectedItem());
    }                                                        

    private void Member_Since_value_textActionPerformed(java.awt.event.ActionEvent evt) {                                                        
        // TODO add your handling code here:
    }                                                       

    private void Number_of_Friends_textActionPerformed(java.awt.event.ActionEvent evt) {                                                       
        // TODO add your handling code here:
    }                                                      

    private void Execute_Query_ButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        // TODO add your handling code here:

        String sqlQueryStatement = null;
        if (MainButtonSelected == "User") {
            boolean userSelected = true;
            String query = getLoad_User();
            try {
                printUser(query, userSelected);

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        } else {
            boolean businessSelected = true;
            getReviews();
            String businessQuery = "Select b.business_id,b.name,b.state,b.city,b.stars from business b  "
                    + "where b.business_id in( " + this.setReviewQuery;
            System.out.println(businessQuery);
            try {
                printBusiness(businessQuery, businessSelected);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }

    }                                                    

    private void BusinessRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                    
        // TODO add your handling code here:
        if (BusinessRadioButton.isSelected()) {
            this.MainButtonSelected = "Business";
        }

        System.out.println(this.MainButtonSelected);
    }                                                   

    private void UserRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                
        // TODO add your handling code here:
        if (UserRadioButton.isSelected()) {
            this.MainButtonSelected = "User";
        }
        System.out.println(this.MainButtonSelected);
    }                                               

    private void AND_OR_UserActionPerformed(java.awt.event.ActionEvent evt) {                                            
        // TODO add your handling code here:
    }                                           

    private void Review_Count_value_textActionPerformed(java.awt.event.ActionEvent evt) {                                                        
        // TODO add your handling code here:
        System.out.println("Text : " + Review_Count_value_text.getText());
    }                                                       

    private void reviewStarsValueActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        // TODO add your handling code here:
    }                                                

    private void QueryResultTableMouseClicked(java.awt.event.MouseEvent evt) {                                              
        // TODO add your handling code here:

        reviewTable();
    }                                             

    private void ReviewPanelMouseClicked(java.awt.event.MouseEvent evt) {                                         
        // TODO add your handling code here:
        getReviews();
    }                                        

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //getLoad_User();//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Hw3().setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JComboBox AND_OR_User;
    private javax.swing.JPanel AttributePanel;
    private javax.swing.JScrollPane AttributePanelScroll;
    private javax.swing.JLabel Average_Stars;
    private javax.swing.JLabel Average_Stars_value;
    private javax.swing.JTextField Average_Stars_value_text;
    private javax.swing.JPanel BusinessPanel;
    private javax.swing.JRadioButton BusinessRadioButton;
    private javax.swing.JComboBox Business_Search_AND_OR;
    private javax.swing.ButtonGroup Business_User_Group;
    private javax.swing.JPanel CategoryPanel;
    private javax.swing.JButton Execute_Query_Button;
    private com.toedter.calendar.JDateChooser MemberSinceDateChooser;
    private javax.swing.JTextField Member_Since_value_text;
    private javax.swing.JLabel Number_of_Friends;
    private javax.swing.JComboBox Number_of_Friends_Checkbox;
    private javax.swing.JTextField Number_of_Friends_text;
    private javax.swing.JLabel Number_of_Friends_value;
    private javax.swing.JLabel Number_of_Votes;
    private javax.swing.JComboBox Number_of_Votes_ComboBox;
    private javax.swing.JLabel Number_of_Votes_value;
    private javax.swing.JTextField Number_of_votes_value_text;
    private javax.swing.JTable QueryResultTable;
    private javax.swing.JTextArea QueryResultTextArea;
    private javax.swing.JPanel ReviewPanel;
    private javax.swing.JTable ReviewTextField;
    private javax.swing.JLabel Review_Count;
    private javax.swing.JComboBox Review_Count_Checkbox;
    private javax.swing.JLabel Review_Count_value;
    private javax.swing.JTextField Review_Count_value_text;
    private javax.swing.JPanel SubCategoryPanel;
    private javax.swing.JRadioButton UserRadioButton;
    private javax.swing.JPanel User_Panel;
    private javax.swing.JComboBox average_stars;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private com.toedter.calendar.JDateChooser reviewFromDate;
    private javax.swing.JComboBox reviewStarsCombo;
    private java.awt.TextField reviewStarsValue;
    private com.toedter.calendar.JDateChooser reviewToDate;
    private javax.swing.JComboBox reviewVotesCombo;
    private java.awt.TextField reviewVotesValue;
    private javax.swing.JLabel starsLabel;
    private javax.swing.JLabel yelping_since1;
    private javax.swing.JLabel yelping_since_value;
    // End of variables declaration                   

    private void reviewTable() {
        String reviewQuery = "";

        int selectedRowID = QueryResultTable.getSelectedRow();
        if (UserRadioButton.isSelected()) {
            String id = (String) QueryResultTable.getValueAt(selectedRowID, QueryResultTable.getColumnModel().getColumnIndex("USER_ID"));
            reviewQuery = reviewQuery + "Select b.name,r.text from review r,business b where b.business_id=r.business_id and r.USER_ID = '" + id + "'";
        } else {
            String id = (String) QueryResultTable.getValueAt(selectedRowID, QueryResultTable.getColumnModel().getColumnIndex("BUSINESS_ID"));
            reviewQuery = reviewQuery + " select  u.name, text from review r, yelp_users u where r.user_id=u.user_id and r.business_id= '" + id + "'";

        }
        QueryResultTextArea.setText(reviewQuery);
        try {
            PreparedStatement stmt = connection.prepareStatement(reviewQuery);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            DefaultTableModel model = new DefaultTableModel();
            model.setColumnCount(rsmd.getColumnCount());
            Vector<String> cols = new Vector();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                cols.add(rsmd.getColumnName(i + 1));
            }
            model.setColumnIdentifiers(cols);
            while (rs.next()) {
                Vector<String> rows = new Vector();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    rows.add(rs.getString(rsmd.getColumnName(i + 1)));
                }
                model.addRow(rows);
            }

            ReviewTextField.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            ReviewTextField.setModel(model);
            ReviewTextField.getColumnModel().getColumn(1).setPreferredWidth(2000);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    
    
        private void getReviews() {
        if (MainButtonSelected == "Business") {
            boolean flag = false;
            setReviewQuery = "select r.business_id from review r";
            String fromDate = ((JTextField) reviewFromDate.getDateEditor().getUiComponent()).getText();
            String toDate = ((JTextField) reviewToDate.getDateEditor().getUiComponent()).getText();
            String stars = reviewStarsValue.getText();
            String starsOperator = reviewStarsCombo.getSelectedItem().toString();
            String votes = reviewVotesValue.getText();
            String votesOpeartor = reviewVotesCombo.getSelectedItem().toString();

            String BusinessAndOrSelector = Business_Search_AND_OR.getSelectedItem().toString();

            if (!fromDate.equals("") && toDate.equals("")) {
                try {
                    setReviewQuery = setReviewQuery + " where r.publish_date between '" + fromDate + "' " + "and" + "'" + toDate + "'";
                    flag = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!votes.equals("")) {
                String symbol = votesOpeartor;
                if (flag) {
                    setReviewQuery = setReviewQuery + "  " + BusinessAndOrSelector + " r.number_of_votes " + symbol + " " + votes + " ";
                } else {
                    setReviewQuery = setReviewQuery + " where " + " r.number_of_votes " + symbol + " " + votes + " ";
                    flag = true;
                }

            }
            if (!stars.equals("")) {
                String sysmbol = starsOperator;
                if (flag) {
                    setReviewQuery = setReviewQuery + "  " + BusinessAndOrSelector + " r.stars " + sysmbol + " " + stars + " ";
                } else {
                    setReviewQuery = setReviewQuery + " where " + " r.stars " + sysmbol + " " + stars + " ";
                    flag = true;
                }

            }

            if (flag) {
                this.setReviewQuery += " and ";
            } else {
                this.setReviewQuery += " where ";
            }

            this.setReviewQuery = this.setReviewQuery + " r.business_id in(";

            if (!(selectedAttributes.isEmpty())) {
                this.setReviewQuery = this.setReviewQuery + "select a.business_id from attributes a where a.attribute_name in(";

                if (Business_Search_AND_OR.getSelectedItem().toString() == "OR") {
                    for (int j = 0; j < selectedAttributes.size(); j++) {
                        if (j == 0) {
                            this.whereClauseAttributes = "'" + selectedAttributes.get(j) + "'";
                        } else {
                            this.whereClauseAttributes += ", " + "'" + selectedAttributes.get(j) + "'";
                        }
                    }
                    this.setReviewQuery = setReviewQuery + this.whereClauseAttributes + ") and a.business_id in(select distinct s.business_id from "
                            + "sub_category s where s.name in(" + this.whereClauseSubCategory + ") and s.business_id in(select m.business_id"
                            + "  from  main_category m where m.name in(" + this.whereClauseCategory + " )))))";
                } else {
                    for (int j = 0; j < selectedAttributes.size(); j++) {

                        if (j == 0) {
                            this.whereClauseAttributes = "'" + selectedAttributes.get(j) + "'";
                        } else {
                            this.whereClauseAttributes += "," + "'" + selectedAttributes.get(j) + "'";
                        }

                    }
//        
//   
                    this.setReviewQuery = setReviewQuery + this.whereClauseAttributes + ") and a.business_id in("
                            + "select distinct s.business_id from sub_category s where s.name in( "
                            + this.whereClauseSubCategory + ") and s.business_id in(SELECT  distinct m.business_id\n"
                            + "                     FROM main_category m\n"
                            + "                WHERE m.name IN (" + this.whereClauseCategory + ")  group by m.business_id having count(distinct m.name)=" + (selectedCategories.size()) + ")group by s.business_id having "
                            + "count(distinct s.name)=" + selectedSubCategories.size()
                            + ") group by a.business_id having count(a.attribute_name)=" + selectedAttributes.size() + "))";
                }

                System.out.println(this.setReviewQuery);

            } else {
                //what if attributes is null;
                if (!(selectedSubCategories.isEmpty())) {

                    this.setReviewQuery = this.setReviewQuery + innerSubcategory + ")";
                    System.out.println(this.setReviewQuery);

                } else {
                    this.setReviewQuery = this.setReviewQuery + innerMaincategory + "))";
                    System.out.println(this.setReviewQuery);
                }

            }

        }
    }
        
        private class ActionHandlerCategory implements ActionListener {

        public ActionHandlerCategory() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (selectedSubCategories.size() > 0) {
                selectedSubCategories.clear();
            }
            if (selectedAttributes.size() > 0) {
                selectedAttributes.clear();
            }

            if (subCategoryCheckBox != null) {
                for (JCheckBox ch : subCategoryCheckBox) {
                    SubCategoryPanel.remove(ch);
                    SubCategoryPanel.repaint();
                    SubCategoryPanel.revalidate();
                }
            }
//                
            if (attributeCheckBox != null) {
                for (JCheckBox ch : attributeCheckBox) {
                    AttributePanel.remove(ch);
                    AttributePanel.repaint();
                    AttributePanel.revalidate();
                }
            }

            JCheckBox ch = (JCheckBox) e.getSource();
            if (ch.isSelected()) {
                if (!selectedCategories.contains(ch.getText())) {
                    selectedCategories.add(ch.getText());
                }
            } else {
                if (selectedCategories.contains(ch.getText())) {
                    selectedCategories.remove(ch.getText());
                }
            }

            try {
                getsubCategory();

            } catch (SQLException ex) {
                Logger.getLogger(Hw3.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public class ActionHandlerSubCategory implements ActionListener {

        public ActionHandlerSubCategory() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedAttributes.size() > 0) {
                selectedAttributes.clear();
            }

//            
            JCheckBox ch = (JCheckBox) e.getSource();
            if (ch.isSelected()) {
                if (!selectedSubCategories.contains(ch.getText())) {
                    selectedSubCategories.add(ch.getText());
                }
            } else {
                if (selectedSubCategories.contains(ch.getText())) {
                    selectedSubCategories.remove(ch.getText());
                }
            }

            getAttributes();

        }

    }

    private class ActionHandlerAttribute implements ActionListener {

        public ActionHandlerAttribute() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox ch = (JCheckBox) e.getSource();
            if (ch.isSelected()) {
                if (!selectedAttributes.contains(ch.getText())) {
                    selectedAttributes.add(ch.getText());
                }
            } else {
                if (selectedAttributes.contains(ch.getText())) {
                    selectedAttributes.remove(ch.getText());
                }
            }
            

        }
    }


}
