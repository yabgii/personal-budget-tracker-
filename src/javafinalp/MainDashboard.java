package javafinalp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainDashboard extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/javaproject";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private List<Source> sources = new ArrayList<>();
    private List<Expenditure> expenditures = new ArrayList<>();
    private VBox sourcesBox = new VBox(10);
    private VBox expendituresBox = new VBox(10);
 private Label netIncomeLabel = new Label();
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        displayDashboard();
    }

    public void displayDashboard() {
        Stage dashboardStage = new Stage();

        // Create UI elements for the dashboard
        Text pageTitle = new Text("Dashboard");
        pageTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        pageTitle.setFill(Color.DARKBLUE);

        // Create buttons for sidebar options
        Button addSourceButton = new Button("Add Source");
        Button addExpenditureButton = new Button("Add Expenditure");
        Button logoutButton = new Button("Log Out");

        // Set button styles
        addSourceButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
        addExpenditureButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
        logoutButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");

        // Set button actions
        addSourceButton.setOnAction(e -> openAddSourceForm(dashboardStage));
        addExpenditureButton.setOnAction(e -> openAddExpenditureForm(dashboardStage));
        logoutButton.setOnAction(e -> {
            // Implement action for logging out
            dashboardStage.close();
            // Optionally, return to the login page
            new Javafinalp().start(new Stage());
        });

        // Create a VBox for the sidebar
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: lightblue;");
        sidebar.getChildren().addAll(
                addSourceButton,
                addExpenditureButton,
                logoutButton
        );

        // Create a VBox for displaying sources
        sourcesBox.setPadding(new Insets(10));
        sourcesBox.setStyle("-fx-background-color: WHITE;");
        Text sourcesTitle = new Text("Income Sources");
        sourcesTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        sourcesTitle.setUnderline(true);
        sourcesBox.getChildren().add(sourcesTitle);
        refreshSources();

        // Create a VBox for displaying expenditures
        expendituresBox.setPadding(new Insets(30));
        expendituresBox.setStyle("-fx-background-color: WHITE;");
        Text expendituresTitle = new Text("Expenditures");
        expendituresTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        expendituresTitle.setUnderline(true);
        expendituresBox.getChildren().add(expendituresTitle);
        refreshExpenditures();

        // Create a BorderPane layout for the dashboard
        BorderPane dashboardLayout = new BorderPane();
        dashboardLayout.setLeft(sidebar);
        dashboardLayout.setTop(pageTitle);
        dashboardLayout.setCenter(sourcesBox);
        dashboardLayout.setRight(expendituresBox);

          // Create a VBox for the bottom section to display net income
        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setStyle("-fx-background-color: lightgrey;");
        netIncomeLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        bottomBox.getChildren().add(netIncomeLabel);

        dashboardLayout.setBottom(bottomBox);
        
        // Create a Scene with the BorderPane layout
        Scene scene = new Scene(dashboardLayout, 800, 600);

        // Set the stage for the dashboard
        dashboardStage.setTitle("Dashboard");
        dashboardStage.setScene(scene);
        dashboardStage.show();
    }

    private void refreshSources() {
        sourcesBox.getChildren().clear();
        Text sourcesTitle = new Text("Income Sources");
        sourcesTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        sourcesTitle.setUnderline(true);
        sourcesBox.getChildren().add(sourcesTitle);

        fetchSourcesFromDatabase();
        
        double totalSources = 0.0;
        

        for (Source source : sources) {
            Label sourceLabel = new Label(source.getSourceName() + " - " + source.getAmount());
            sourceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

            Button updateButton = new Button("Update");
            Button deleteButton = new Button("Delete");

            updateButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

            HBox buttonBox = new HBox(10); // Create an HBox to contain buttons
            buttonBox.getChildren().addAll(updateButton, deleteButton);

            VBox sourceBox = new VBox(10); // Create a VBox to contain source label and buttons
            sourceBox.getChildren().addAll(sourceLabel, buttonBox); // Add source label and buttonBox to the source VBox

            sourcesBox.getChildren().add(sourceBox); // Add source VBox to the sourcesBox
 totalSources += source.getAmount();


            // Set delete button action
            deleteButton.setOnAction(e -> {
                if (deleteSourceFromDatabase(source.getSourceId())) {
                    sourcesBox.getChildren().remove(sourceBox);
                    showAlert(Alert.AlertType.INFORMATION, "Source Deleted", "Income source deleted successfully.");
                         
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete income source.");
                }
            });

            // Set update button action
            updateButton.setOnAction(e -> openUpdateSourceForm(source));
        }
        Label totalSourcesLabel = new Label("Total Income: " + totalSources);
        totalSourcesLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        totalSourcesLabel.setTextFill(Color.GREEN);
        sourcesBox.getChildren().add(totalSourcesLabel);
                updateNetIncome();
    }

    private void refreshExpenditures() {
        expendituresBox.getChildren().clear();
        Text expendituresTitle = new Text("Expenditures");
        expendituresTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        expendituresTitle.setUnderline(true);
        expendituresBox.getChildren().add(expendituresTitle);

        fetchExpendituresFromDatabase();
 double totalExpenditures = 0.0;
        for (Expenditure expenditure : expenditures) {
            Label expenditureLabel = new Label(expenditure.getExpenditureName() + " - " + expenditure.getAmount());
            expenditureLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

            Button updateButton = new Button("Update");
            Button deleteButton = new Button("Delete");

            updateButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

            HBox buttonBox = new HBox(10); // Create an HBox to contain buttons
            buttonBox.getChildren().addAll(updateButton, deleteButton);

            VBox expenditureBox = new VBox(20); // Create a VBox to contain expenditure label and buttons
            expenditureBox.getChildren().addAll(expenditureLabel, buttonBox); // Add expenditure label and buttonBox to the expenditure VBox

            expendituresBox.getChildren().add(expenditureBox); // Add expenditure VBox to the expendituresBox

             totalExpenditures += expenditure.getAmount();
            // Set delete button action
            deleteButton.setOnAction(e -> {
                if (deleteExpenditureFromDatabase(expenditure.getExpenditureId())) {
                    expendituresBox.getChildren().remove(expenditureBox);
                    showAlert(Alert.AlertType.INFORMATION, "Expenditure Deleted", "Expenditure deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete expenditure.");
                }
                
            });

            // Set update button action
            updateButton.setOnAction(e -> openUpdateExpenditureForm(expenditure));
        }
         Label totalExpendituresLabel = new Label("Total Expenditures: " + totalExpenditures);
        totalExpendituresLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        totalExpendituresLabel.setTextFill(Color.RED);
        expendituresBox.getChildren().add(totalExpendituresLabel);
         updateNetIncome();
    }

    private void openAddSourceForm(Stage parentStage) {
        Stage addSourceStage = new Stage();

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label sourceNameLabel = new Label("Source Name:");
        TextField sourceNameField = new TextField();

        Label amountLabel = new Label("Amount:");
        TextField amountField = new TextField();

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            String sourceName = sourceNameField.getText();
            String amount = amountField.getText();

            if (!sourceName.isEmpty() && !amount.isEmpty()) {
                try {
                    double amountValue = Double.parseDouble(amount);
                    if (addSourceToDatabase(sourceName, amountValue)) {
                        showAlert(Alert.AlertType.INFORMATION, "Source Added", "Income source added successfully.");
                        addSourceStage.close();
                        refreshSources();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to add income source.");
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid amount.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
            }
        });

        layout.getChildren().addAll(sourceNameLabel, sourceNameField, amountLabel, amountField, saveButton);

        Scene scene = new Scene(layout, 300, 200);
        addSourceStage.setTitle("Add Source");
        addSourceStage.setScene(scene);
        addSourceStage.show();
    }

    private void openAddExpenditureForm(Stage parentStage) {
        Stage addExpenditureStage = new Stage();

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label expenditureNameLabel = new Label("Expenditure Name:");
        TextField expenditureNameField = new TextField();

        Label amountLabel = new Label("Amount:");
        TextField amountField = new TextField();

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            String expenditureName = expenditureNameField.getText();
            String amount = amountField.getText();

            if (!expenditureName.isEmpty() && !amount.isEmpty()) {
                try {
                    double amountValue = Double.parseDouble(amount);
                    if (addExpenditureToDatabase(expenditureName, amountValue)) {
                        showAlert(Alert.AlertType.INFORMATION, "Expenditure Added", "Expenditure added successfully.");
                        addExpenditureStage.close();
                        refreshExpenditures();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to add expenditure.");
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid amount.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
            }
        });

        layout.getChildren().addAll(expenditureNameLabel, expenditureNameField, amountLabel, amountField, saveButton);

        Scene scene = new Scene(layout, 300, 200);
        addExpenditureStage.setTitle("Add Expenditure");
        addExpenditureStage.setScene(scene);
        addExpenditureStage.show();
    }

    private void openUpdateSourceForm(Source source) {
        Stage updateSourceStage = new Stage();

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label sourceNameLabel = new Label("Source Name:");
        TextField sourceNameField = new TextField(source.getSourceName());

        Label amountLabel = new Label("Amount:");
        TextField amountField = new TextField(String.valueOf(source.getAmount()));

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            String sourceName = sourceNameField.getText();
            String amount = amountField.getText();

            if (!sourceName.isEmpty() && !amount.isEmpty()) {
                try {
                    double amountValue = Double.parseDouble(amount);
                    if (updateSourceInDatabase(source.getSourceId(), sourceName, amountValue)) {
                        showAlert(Alert.AlertType.INFORMATION, "Source Updated", "Income source updated successfully.");
                        updateSourceStage.close();
                        refreshSources();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update income source.");
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid amount.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
            }
        });

        layout.getChildren().addAll(sourceNameLabel, sourceNameField, amountLabel, amountField, saveButton);

        Scene scene = new Scene(layout, 300, 200);
        updateSourceStage.setTitle("Update Source");
        updateSourceStage.setScene(scene);
        updateSourceStage.show();
    }

    private void openUpdateExpenditureForm(Expenditure expenditure) {
        Stage updateExpenditureStage = new Stage();

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label expenditureNameLabel = new Label("Expenditure Name:");
        TextField expenditureNameField = new TextField(expenditure.getExpenditureName());

        Label amountLabel = new Label("Amount:");
        TextField amountField = new TextField(String.valueOf(expenditure.getAmount()));

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            String expenditureName = expenditureNameField.getText();
            String amount = amountField.getText();

            if (!expenditureName.isEmpty() && !amount.isEmpty()) {
                try {
                    double amountValue = Double.parseDouble(amount);
                    if (updateExpenditureInDatabase(expenditure.getExpenditureId(), expenditureName, amountValue)) {
                        showAlert(Alert.AlertType.INFORMATION, "Expenditure Updated", "Expenditure updated successfully.");
                        updateExpenditureStage.close();
                        refreshExpenditures();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update expenditure.");
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid amount.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
            }
        });

        layout.getChildren().addAll(expenditureNameLabel, expenditureNameField, amountLabel, amountField, saveButton);

        Scene scene = new Scene(layout, 300, 200);
        updateExpenditureStage.setTitle("Update Expenditure");
        updateExpenditureStage.setScene(scene);
        updateExpenditureStage.show();
    }

    private boolean addSourceToDatabase(String sourceName, double amount) {
        String sql = "INSERT INTO sources (source_id, source_name, amount, date) VALUES (?, ?, ?, CURDATE())";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, generateSourceID());
            pstmt.setString(2, sourceName);
            pstmt.setDouble(3, amount);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addExpenditureToDatabase(String expenditureName, double amount) {
        String sql = "INSERT INTO expenditures (expenditure_id, expenditure_name, amount, date) VALUES (?, ?, ?, CURDATE())";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, generateExpenditureID());
            pstmt.setString(2, expenditureName);
            pstmt.setDouble(3, amount);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateSourceInDatabase(String sourceId, String sourceName, double amount) {
        String sql = "UPDATE sources SET source_name = ?, amount = ? WHERE source_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sourceName);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, sourceId);
 updateNetIncome();
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateExpenditureInDatabase(String expenditureId, String expenditureName, double amount) {
        String sql = "UPDATE expenditures SET expenditure_name = ?, amount = ? WHERE expenditure_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expenditureName);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, expenditureId);
 updateNetIncome();
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateSourceID() {
        // Generate a unique source ID (for simplicity, using a random number)
        return "SID" + (int) (Math.random() * 10000);
    }

    private String generateExpenditureID() {
        // Generate a unique expenditure ID (for simplicity, using a random number)
        return "EID" + (int) (Math.random() * 10000);
    }
    
    //method to calculate net income
        private void updateNetIncome() {
        double totalIncome = sources.stream().mapToDouble(Source::getAmount).sum();
        double totalExpenditures = expenditures.stream().mapToDouble(Expenditure::getAmount).sum();
        double netIncome = totalIncome-totalExpenditures  ;

        netIncomeLabel.setText("Net Income: " + netIncome);
        if (netIncome < 0) {
            netIncomeLabel.setTextFill(Color.RED);
        } else {
            netIncomeLabel.setTextFill(Color.GREEN);
        }
    }

    private void fetchSourcesFromDatabase() {
        sources.clear();
        String sql = "SELECT * FROM sources";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String sourceId = rs.getString("source_id");
                String sourceName = rs.getString("source_name");
                double amount = rs.getDouble("amount");
                Source source = new Source(sourceId, sourceName, amount);
                sources.add(source);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchExpendituresFromDatabase() {
        expenditures.clear();
        String sql = "SELECT * FROM expenditures";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String expenditureId = rs.getString("expenditure_id");
                String expenditureName = rs.getString("expenditure_name");
                double amount = rs.getDouble("amount");
                Expenditure expenditure = new Expenditure(expenditureId, expenditureName, amount);
                expenditures.add(expenditure);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean deleteSourceFromDatabase(String sourceId) {
        String sql = "DELETE FROM sources WHERE source_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
   
            pstmt.setString(1, sourceId);

            int affectedRows = pstmt.executeUpdate();
            updateNetIncome();
          refreshSources();
          pstmt.executeUpdate();
          
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
         
    }

    private boolean deleteExpenditureFromDatabase(String expenditureId) {
        String sql = "DELETE FROM expenditures WHERE expenditure_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expenditureId);

            int affectedRows = pstmt.executeUpdate();
             updateNetIncome();
              refreshExpenditures();
                pstmt.executeUpdate();
                
                
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

   

    public class Source {
        private String sourceId;
        private String sourceName;
        private double amount;

        public Source(String sourceId, String sourceName, double amount) {
            this.sourceId = sourceId;
            this.sourceName = sourceName;
            this.amount = amount;
        }

        // Getters and setters
        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    public class Expenditure {
        private String expenditureId;
        private String expenditureName;
        private double amount;

        public Expenditure(String expenditureId, String expenditureName, double amount) {
            this.expenditureId = expenditureId;
            this.expenditureName = expenditureName;
            this.amount = amount;
        }

        // Getters and setters
        public String getExpenditureId() {
            return expenditureId;
        }

        public void setExpenditureId(String expenditureId) {
            this.expenditureId = expenditureId;
        }

        public String getExpenditureName() {
            return expenditureName;
        }

        public void setExpenditureName(String expenditureName) {
            this.expenditureName = expenditureName;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}
