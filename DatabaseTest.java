import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DatabaseTest extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost/yourdatabase?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Look2the1!";
    private Label batchUpdateLabel;
    private Label nonBatchUpdateLabel;

    @Override
    public void start(Stage primaryStage) {
        // Initialize labels for update status
        primaryStage.setTitle("Exercise35_01");
        batchUpdateLabel = new Label("Please perform the batch update.");
        nonBatchUpdateLabel = new Label("Please perform the batch update.");

        // Initialize buttons
        Button btnBatchUpdate = new Button("Batch Update");
        Button btnNonBatchUpdate = new Button("Non-Batch Update");
        Button btnConnectToDb = new Button("Connect to Database");

        btnBatchUpdate.setOnAction(event -> performUpdate(true));

        btnNonBatchUpdate.setOnAction(event -> performUpdate(false));

        btnConnectToDb.setOnAction(e -> showDatabaseConnectionDialog(primaryStage));

        VBox buttonLayout = new VBox(10, btnBatchUpdate, btnNonBatchUpdate);
        buttonLayout.setAlignment(Pos.CENTER);

        VBox labelLayout = new VBox(5, batchUpdateLabel, nonBatchUpdateLabel);
        labelLayout.setAlignment(Pos.CENTER_LEFT);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.getChildren().addAll(labelLayout, buttonLayout, btnConnectToDb);

        Scene scene = new Scene(mainLayout, 400, 200);
        primaryStage.setTitle("Exercise35_01");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDatabaseConnectionDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Connect to DB");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));

        TextField tfJdbcDriver = new TextField();
        TextField tfDatabaseUrl = new TextField();
        TextField tfUsername = new TextField();
        PasswordField pfPassword = new PasswordField();

        grid.add(new Label("JDBC Driver"), 0, 0);
        grid.add(tfJdbcDriver, 1, 0);
        grid.add(new Label("Database URL"), 0, 1);
        grid.add(tfDatabaseUrl, 1, 1);
        grid.add(new Label("Username"), 0, 2);
        grid.add(tfUsername, 1, 2);
        grid.add(new Label("Password"), 0, 3);
        grid.add(pfPassword, 1, 3);

        Button btnConnect = new Button("Connect to DB");
        Button btnClose = new Button("Close Dialog");
        btnClose.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10, btnConnect, btnClose);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox dialogVBox = new VBox(20, grid, buttonBox);
        Scene dialogScene = new Scene(dialogVBox);

        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void performUpdate(boolean isBatchUpdate) {
        new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                long startTime = System.currentTimeMillis();
                if (isBatchUpdate) {
                    performBatchUpdate(conn);
                } else {
                    performNonBatchUpdate(conn);
                }
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;

                String resultText = (isBatchUpdate ? "Batch" : "Non-Batch") + " update completed. Time elapsed: "
                        + elapsedTime + " ms";
                if (isBatchUpdate) {
                    updateLabel(batchUpdateLabel, resultText);
                } else {
                    updateLabel(nonBatchUpdateLabel, resultText);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                updateLabel(isBatchUpdate ? batchUpdateLabel : nonBatchUpdateLabel, "Failed to perform update.");
            }
        }).start();
    }

    private void updateLabel(Label label, String text) {
        javafx.application.Platform.runLater(() -> label.setText(text));
    }

    private static void performBatchUpdate(Connection conn) {
        String sql = "INSERT INTO Temp (num1, num2, num3) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < 1000; i++) {
                pstmt.setDouble(1, Math.random());
                pstmt.setDouble(2, Math.random());
                pstmt.setDouble(3, Math.random());
                pstmt.addBatch();

                if ((i + 1) % 100 == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                }
            }

            pstmt.executeBatch();
            conn.commit();

            long endTime = System.currentTimeMillis();

            System.out.println("Batch update completed. Time elapsed: " + (endTime - startTime) + " ms");
        } catch (SQLException e) {
            System.out.println("SQLException in performBatchUpdate");
            e.printStackTrace();
        }
    }

    private static void performNonBatchUpdate(Connection conn) {
        String sql = "INSERT INTO Temp (num1, num2, num3) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < 1000; i++) {
                pstmt.setDouble(1, Math.random());
                pstmt.setDouble(2, Math.random());
                pstmt.setDouble(3, Math.random());
                pstmt.executeUpdate();
            }

            long endTime = System.currentTimeMillis();

            System.out.println("Non-batch update completed. Time elapsed: " + (endTime - startTime) + " ms");
        } catch (SQLException e) {
            System.out.println("SQLException in performNonBatchUpdate");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Load the JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        launch(args);
    }
}
