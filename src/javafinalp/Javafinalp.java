package javafinalp;

import javafx.application.Application;

import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
import javafx.geometry.Insets;//This class is used to define padding and margins for JavaFX nodes
import javafx.geometry.Pos;//This enum is used to define the alignment of nodes within their parent containers.
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Javafinalp extends Application {

    private Stage primaryStage;

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/javaproject";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        launch(args); // Start the JavaFX application
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Create UI elements for login
        Text sceneTitle = new Text("Welcome");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        sceneTitle.setFill(Color.DARKBLUE);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setTextFill(Color.DARKBLUE);
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        passwordLabel.setTextFill(Color.DARKBLUE);
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
        loginButton.setOnAction((ActionEvent event) -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (authenticateUser(username, password)) {
                primaryStage.close();
                new MainDashboard().displayDashboard(); // Call displayDashboard method
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }       });

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
        registerButton.setOnAction((ActionEvent event) -> {
            openRegisterForm();
        });

        // Create an HBox for the buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(loginButton, registerButton);

        // Create a GridPane layout for login
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));
        gridPane.setStyle("-fx-background-color: lightblue;");

        // Add UI elements to the GridPane
        gridPane.add(sceneTitle, 0, 0, 2, 1);
        gridPane.add(usernameLabel, 0, 1);
        gridPane.add(usernameField, 1, 1);
        gridPane.add(passwordLabel, 0, 2);
        gridPane.add(passwordField, 1, 2);
        gridPane.add(buttonBox, 1, 3);

        // Create a Scene with the GridPane layout
        Scene scene = new Scene(gridPane, 600, 500);

        // Set the stage
        primaryStage.setTitle("Login Page");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openRegisterForm() {
        primaryStage.hide(); // Hide the login form

        Stage registerStage = new Stage();

        // Create UI elements for registration
        Text sceneTitle = new Text("Register");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        sceneTitle.setFill(Color.DARKBLUE);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setTextFill(Color.DARKBLUE);
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        passwordLabel.setTextFill(Color.DARKBLUE);
        PasswordField passwordField = new PasswordField();

        Label emailLabel = new Label("Email:");
        emailLabel.setTextFill(Color.DARKBLUE);
        TextField emailField = new TextField();

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");
        
        registerButton.setOnAction((ActionEvent event) -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();
            
            if (!username.isEmpty() && !password.isEmpty() && !email.isEmpty()) {
                if (registerUser(username, password, email)) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Welcome, " + username + "!");
                    registerStage.close();
                    primaryStage.show(); // Show the login form again
                } else {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", "An error occurred. Please try again.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Please fill in all fields.");
            }
        });

        // Create a GridPane layout for registration
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));
        gridPane.setStyle("-fx-background-color: lightblue;");

        // Add UI elements to the GridPane
        gridPane.add(sceneTitle, 0, 0, 2, 1);
        gridPane.add(usernameLabel, 0, 1);
        gridPane.add(usernameField, 1, 1);
        gridPane.add(passwordLabel, 0, 2);
        gridPane.add(passwordField, 1, 2);
        gridPane.add(emailLabel, 0, 3);
        gridPane.add(emailField, 1, 3);
        gridPane.add(registerButton, 1, 4);

        // Create a Scene with the GridPane layout
        Scene scene = new Scene(gridPane, 600, 500);

        // Set the stage for registration
        registerStage.setTitle("Registration Page");
        registerStage.setScene(scene);
        registerStage.show();
    }

    private boolean registerUser(String username, String password, String email) {
        String sql = "INSERT INTO user_register (userid, username, password, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameters for the SQL query
            pstmt.setString(1, generateUserId());
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, email);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM user_register WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Return true if a match is found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateUserId() {
        // Generate a unique user ID (for simplicity, using a random number)
        return "ID" + (int) (Math.random() * 10000);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();//Displays the alert dialog and waits for the user to respond 
    }
}
