package com.inventory.main;

import com.inventory.db.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.inventory.controller.InventoryController;

/**
 * Entry point for the Inventory Management System JavaFX application.
 * Responsible for building the main window layout and initializing
 * the InventoryController which handles all business logic.
 */
public class MainApp extends Application {

    // Fixed window dimensions
    private static final double WINDOW_WIDTH  = 950;
    private static final double WINDOW_HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) {

        // ---- Verify DB connection before launching UI ----
        if (!DatabaseConnection.testConnection()) {
            showDBErrorAlert();
            Platform.exit();
            return;
        }

        // ---- Build the root layout ----
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // ---- Top: Application Header ----
        VBox header = buildHeader();
        root.setTop(header);

        // ---- Center: Tab-based main content ----
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Initialize the controller — it builds and owns all tab content
        InventoryController controller = new InventoryController();

        Tab tabView   = new Tab("📦  All Products",   controller.buildViewTab());
        Tab tabAdd    = new Tab("➕  Add Product",    controller.buildAddTab());
        Tab tabUpdate = new Tab("🔄  Update Stock",   controller.buildUpdateTab());

        tabPane.getTabs().addAll(tabView, tabAdd, tabUpdate);
        root.setCenter(tabPane);

        // ---- Bottom: Status bar ----
        HBox statusBar = buildStatusBar();
        root.setBottom(statusBar);

        // ---- Scene and Stage setup ----
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Load external CSS stylesheet (Stage 5)
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        primaryStage.setTitle("Inventory Management System — BBT 2202");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        // ---- Close DB connection cleanly on exit ----
        primaryStage.setOnCloseRequest(e -> {
            DatabaseConnection.closeConnection();
            System.out.println("Application closed.");
        });
    }

    // -------------------------
    // Header Builder
    // -------------------------
    private VBox buildHeader() {
        Label title = new Label("Inventory Management System");
        title.getStyleClass().add("header-title");

        Label subtitle = new Label("BBT 2202 — Advanced Object Oriented Programming");
        subtitle.getStyleClass().add("header-subtitle");

        VBox header = new VBox(4, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 0, 15, 0));
        header.getStyleClass().add("header-box");

        return header;
    }

    // -------------------------
    // Status Bar Builder
    // -------------------------
    private HBox buildStatusBar() {
        Label status = new Label("✅  Connected to inventory_db  |  Ready");
        status.getStyleClass().add("status-label");

        HBox bar = new HBox(status);
        bar.setPadding(new Insets(6, 15, 6, 15));
        bar.getStyleClass().add("status-bar");

        return bar;
    }

    // -------------------------
    // DB Connection Error Alert
    // -------------------------
    private void showDBErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Connection Failed");
        alert.setHeaderText("Could not connect to PostgreSQL.");
        alert.setContentText(
                "Please ensure:\n" +
                        "• PostgreSQL is running\n" +
                        "• inventory_db database exists\n" +
                        "• Credentials in DatabaseConnection.java are correct"
        );
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}