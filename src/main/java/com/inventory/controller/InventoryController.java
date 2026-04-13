package com.inventory.controller;

import com.inventory.dao.ProductDAO;
import com.inventory.model.NonPerishableProduct;
import com.inventory.model.PerishableProduct;
import com.inventory.model.Product;
import com.inventory.util.InputValidator;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;


import java.sql.SQLException;
import java.util.List;

/**
 * Central controller — manages all three views and all database interactions.
 * Connects the JavaFX UI to the ProductDAO layer.
 */
public class InventoryController {

    // -------------------------
    // Fields
    // -------------------------
    private final ProductDAO dao = new ProductDAO();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private VBox viewProducts, viewAdd, viewUpdate;

    // Stat card value labels — updated on every data load
    private Label statTotalVal, statOosVal, statInVal, statValueVal;

    // Badge on Update Stock nav item
    private Label oosBadge;

    // =========================================================
    // CONTENT PANE — holds all three views in a StackPane
    // =========================================================

    /**
     * Builds the main StackPane that holds all three views.
     * The sidebar switches between them by toggling visibility/managed.
     */
    public StackPane buildContentPane() {
        viewProducts = buildViewTab();
        viewAdd      = buildAddTab();
        viewUpdate   = buildUpdateTab();

        // Only products view is visible initially
        setVisible(viewAdd, false);
        setVisible(viewUpdate, false);

        StackPane contentPane = new StackPane(viewProducts, viewAdd, viewUpdate);
        contentPane.setAlignment(Pos.TOP_LEFT);
        return contentPane;
    }

    /**
     * Switches the active view (0=Products, 1=Add, 2=Update).
     * Called by the sidebar nav buttons in MainApp.
     */
    public void showTab(int index) {
        VBox[] views = {viewProducts, viewAdd, viewUpdate};
        for (int i = 0; i < views.length; i++) {
            setVisible(views[i], i == index);
        }
        // Always refresh data when switching to a data view
        if (index == 0) loadProducts(null);
        if (index == 2) refreshUpdateDropdown(null);
    }

    private void setVisible(VBox pane, boolean visible) {
        pane.setVisible(visible);
        pane.setManaged(visible);
    }

    /** Exposes the OOS badge label so MainApp can position it in the sidebar. */
    public Label getOosBadge() {
        oosBadge = new Label("0");
        oosBadge.setStyle(
                "-fx-background-color: #dc2626; -fx-text-fill: white;" +
                        "-fx-font-size: 10px; -fx-padding: 1 6 1 6;" +
                        "-fx-background-radius: 10px;"
        );
        return oosBadge;
    }

    // =========================================================
    // VIEW 1 — ALL PRODUCTS
    // =========================================================

    public VBox buildViewTab() {

        // ---- Stats bar ----
        HBox statsBar = buildStatsBar();

        // ---- Search + actions bar ----
        TextField searchField = new TextField();
        searchField.setPromptText("Search by product name...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(280);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");

        Label footerLabel = new Label();
        footerLabel.getStyleClass().add("status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBar = new HBox(12, searchField, spacer, refreshBtn);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(14, 20, 0, 20));

        // ---- Table ----
        TableView<Product> tableView = buildTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        VBox tableWrap = new VBox(6, tableView, footerLabel);
        tableWrap.setPadding(new Insets(10, 20, 14, 20));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(tableWrap, Priority.ALWAYS);

        VBox layout = new VBox(statsBar, actionBar, tableWrap);
        VBox.setVgrow(tableWrap, Priority.ALWAYS);

        // ---- Events ----
        loadProducts(footerLabel);

        refreshBtn.setOnAction(e -> {
            searchField.clear();
            loadProducts(footerLabel);
        });

        searchField.textProperty().addListener((obs, o, n) -> {
            if (n.trim().isEmpty()) {
                loadProducts(footerLabel);
            } else {
                searchProducts(n.trim(), footerLabel);
            }
        });

        return layout;
    }

    // -------------------------
    // Stats Bar
    // -------------------------
    private HBox buildStatsBar() {
        VBox cardTotal = makeStatCard("Total Products", "0", "in inventory", "normal");
        VBox cardOos   = makeStatCard("Out of Stock",   "0", "need restocking", "danger");
        VBox cardIn    = makeStatCard("In Stock",       "0", "available now", "success");
        VBox cardVal   = makeStatCard("Total Value",    "KES 0", "estimated", "normal");

        // Store the value labels for later updates
        statTotalVal = (Label) cardTotal.getChildren().get(1);
        statOosVal   = (Label) cardOos.getChildren().get(1);
        statInVal    = (Label) cardIn.getChildren().get(1);
        statValueVal = (Label) cardVal.getChildren().get(1);

        HBox bar = new HBox(12, cardTotal, cardOos, cardIn, cardVal);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: transparent transparent #e2e8f0 transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );
        for (javafx.scene.Node n : bar.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }
        return bar;
    }

    private VBox makeStatCard(String label, String value, String sub, String type) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        Label val = new Label(value);
        val.getStyleClass().add(
                "danger".equals(type) ? "stat-value-danger" :
                        "success".equals(type) ? "stat-value-success" : "stat-value"
        );

        Label s = new Label(sub);
        s.getStyleClass().add("stat-sub");

        VBox card = new VBox(3, lbl, val, s);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // -------------------------
    // TableView Builder
    // -------------------------
    @SuppressWarnings("unchecked")
    private TableView<Product> buildTable() {
        TableView<Product> table = new TableView<>(productList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No products found."));
        table.getStyleClass().add("table-view");

        // ID column
        TableColumn<Product, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colId.setPrefWidth(50);
        colId.setMaxWidth(60);

        // Name column — RED text if out of stock (core requirement)
        TableColumn<Product, String> colName = new TableColumn<>("Product Name");
        colName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getName()));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(name);
                    Product p = getTableView().getItems().get(getIndex());
                    // Brief requirement: display name in RED when qty is zero
                    setStyle(p.isOutOfStock()
                            ? "-fx-text-fill: #dc2626; -fx-font-weight: bold;"
                            : "-fx-text-fill: -fx-text-base-color;");
                }
            }
        });

        // Category column
        TableColumn<Product, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCategory()));
        colCat.setPrefWidth(130);

        // Quantity column — also red when zero
        TableColumn<Product, Integer> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getQuantity()).asObject());
        colQty.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(qty));
                setStyle(qty == 0
                        ? "-fx-text-fill: #dc2626; -fx-font-weight: bold;"
                        : "");
            }
        });
        colQty.setPrefWidth(80);

        // Price column
        TableColumn<Product, Double> colPrice = new TableColumn<>("Price (KES)");
        colPrice.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getPrice()).asObject());
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("%.2f", price));
            }
        });
        colPrice.setPrefWidth(110);

        // Status column — badge style
        TableColumn<Product, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().isOutOfStock() ? "Out of Stock" : "In Stock"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                setStyle("Out of Stock".equals(status)
                        ? "-fx-text-fill: #991b1b; -fx-background-color: #fee2e2;" +
                          "-fx-background-radius: 20px; -fx-padding: 3 10 3 10;"
                        : "-fx-text-fill: #166534; -fx-background-color: #dcfce7;" +
                          "-fx-background-radius: 20px; -fx-padding: 3 10 3 10;");
            }
        });
        colStatus.setPrefWidth(110);

        // Actions column — inline Edit Stock button
        TableColumn<Product, Void> colActions = new TableColumn<>("Actions");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit Stock");
            {
                editBtn.getStyleClass().add("btn-small");
                editBtn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    showQuickEditDialog(p);
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : editBtn);
            }
        });
        colActions.setPrefWidth(100);
        colActions.setSortable(false);

        table.getColumns().addAll(colId, colName, colCat, colQty, colPrice, colStatus, colActions);

        // Row factory — highlight entire row background for out-of-stock
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                getStyleClass().removeAll("row-out-of-stock", "row-normal");
                if (!empty && p != null) {
                    getStyleClass().add(p.isOutOfStock() ? "row-out-of-stock" : "row-normal");
                }
            }
        });

        return table;
    }

    // -------------------------
    // Quick Edit Dialog (from table row)
    // -------------------------
    /**
     * Shows a compact dialog directly from the table row for fast stock editing.
     * This is the most intuitive way to update stock — no page navigation needed.
     */
    private void showQuickEditDialog(Product product) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Update Stock");
        dialog.setHeaderText("Updating: " + product.getName());

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField qtyField = new TextField(String.valueOf(product.getQuantity()));
        qtyField.getStyleClass().add("form-input");
        qtyField.setPrefWidth(200);
        qtyField.selectAll();

        Label hint = new Label("Current: " + product.getQuantity() + " units  |  " +
                "Price: KES " + String.format("%.2f", product.getPrice()));
        hint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        VBox content = new VBox(10,
                new Label("New Quantity:"),
                qtyField,
                hint
        );
        content.setPadding(new Insets(10, 0, 0, 0));
        dialog.getDialogPane().setContent(content);

        // Validate and return new qty
        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                try {
                    int qty = Integer.parseInt(qtyField.getText().trim());
                    return qty >= 0 ? qty : null;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newQty -> {
            try {
                dao.updateStock(product.getId(), newQty);
                loadProducts(null); // Refresh table
                showNotification("Stock updated for \"" + product.getName() + "\"");
            } catch (SQLException e) {
                showError("Update failed: " + e.getMessage());
            }
        });
    }

    // =========================================================
    // VIEW 2 — ADD PRODUCT
    // =========================================================

    /**
     * Builds the Add Product form.
     * Core fields: Name, Quantity, Price (as per the project brief).
     * Category is optional — defaults to Non-Perishable.
     */
    public VBox buildAddTab() {

        Label title = new Label("Add New Product");
        title.getStyleClass().add("form-section-title");

        Label sub = new Label("Enter product details below. Name, Quantity, and Price are required.");
        sub.getStyleClass().add("form-section-sub");
        sub.setWrapText(true);

        // ---- Core required fields ----
        TextField nameField  = new TextField();
        nameField.setPromptText("e.g. Unga wa Mahindi");
        nameField.getStyleClass().add("form-input");

        TextField qtyField   = new TextField();
        qtyField.setPromptText("e.g. 50");
        qtyField.getStyleClass().add("form-input");

        TextField priceField = new TextField();
        priceField.setPromptText("e.g. 120.00");
        priceField.getStyleClass().add("form-input");

        // ---- Optional: Category (keeps OOP structure intact) ----
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Non-Perishable", "Perishable");
        categoryBox.setValue("Non-Perishable");
        categoryBox.getStyleClass().add("combo-box");
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        // ---- Optional: Expiry date (only shown for perishable) ----
        DatePicker expiryPicker = new DatePicker();
        expiryPicker.setPromptText("Select expiry date");
        expiryPicker.getStyleClass().add("date-picker");
        expiryPicker.setVisible(false);
        expiryPicker.setManaged(false);

        Label expiryLabel = new Label("Expiry Date (optional):");
        expiryLabel.getStyleClass().add("form-label");
        expiryLabel.setVisible(false);
        expiryLabel.setManaged(false);

        categoryBox.setOnAction(e -> {
            boolean perishable = "Perishable".equals(categoryBox.getValue());
            expiryPicker.setVisible(perishable);
            expiryPicker.setManaged(perishable);
            expiryLabel.setVisible(perishable);
            expiryLabel.setManaged(perishable);
        });

        // ---- Feedback label ----
        Label feedback = new Label();
        feedback.setWrapText(true);

        // ---- Buttons ----
        Button addBtn   = new Button("Add Product");
        Button clearBtn = new Button("Clear Form");
        addBtn.getStyleClass().add("btn-primary");
        clearBtn.getStyleClass().add("btn-secondary");

        HBox btnRow = new HBox(12, addBtn, clearBtn);

        // ---- Form Grid ----
        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(12);

        // Row 0 — Name (full width)
        form.add(makeFormRow("Product Name *", nameField), 0, 0, 2, 1);

        // Row 1 — Qty and Price side by side
        form.add(makeFormRow("Quantity *", qtyField),     0, 1);
        form.add(makeFormRow("Price (KES) *", priceField), 1, 1);

        // Row 2 — Category
        form.add(makeFormRow("Category", categoryBox), 0, 2, 2, 1);

        // Row 3 — Expiry (conditional)
        form.add(expiryLabel,   0, 3);
        form.add(expiryPicker,  1, 3);

        // Row 4 — Buttons & feedback
        form.add(btnRow,     0, 4, 2, 1);
        form.add(feedback,   0, 5, 2, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setPercentWidth(50);
        form.getColumnConstraints().addAll(c1, c2);

        // ---- Card wrapper ----
        VBox card = new VBox(8, title, sub, form);
        card.getStyleClass().add("form-card");
        card.setMaxWidth(520);

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(24, 28, 24, 28));

        // ---- Button actions ----
        addBtn.setOnAction(e -> {
            List<String> errors = InputValidator.validateAddForm(
                    nameField, categoryBox, qtyField, priceField,
                    expiryPicker, "Perishable".equals(categoryBox.getValue())
            );

            if (!errors.isEmpty()) {
                feedback.getStyleClass().removeAll("feedback-success", "feedback-error");
                feedback.getStyleClass().add("feedback-error");
                feedback.setText("Please fix: " + String.join(", ", errors));
                return;
            }

            try {
                String name     = nameField.getText().trim();
                int qty         = Integer.parseInt(qtyField.getText().trim());
                double price    = Double.parseDouble(priceField.getText().trim());
                String category = categoryBox.getValue();

                Product newProduct;
                if ("Perishable".equals(category) && expiryPicker.getValue() != null) {
                    newProduct = new PerishableProduct(name, qty, price, expiryPicker.getValue());
                } else {
                    newProduct = new NonPerishableProduct(name, qty, price);
                }

                dao.insert(newProduct);

                feedback.getStyleClass().removeAll("feedback-success", "feedback-error");
                feedback.getStyleClass().add("feedback-success");
                feedback.setText("\"" + name + "\" added successfully! (ID: " + newProduct.getId() + ")");

                // Clear form
                nameField.clear();
                qtyField.clear();
                priceField.clear();
                expiryPicker.setValue(null);
                categoryBox.setValue("Non-Perishable");
                InputValidator.clearValidationStyles(nameField, qtyField, priceField);

                showNotification("\"" + name + "\" added to inventory");

            } catch (SQLException ex) {
                feedback.getStyleClass().removeAll("feedback-success", "feedback-error");
                feedback.getStyleClass().add("feedback-error");
                feedback.setText("Database error: " + ex.getMessage());
            }
        });

        clearBtn.setOnAction(e -> {
            nameField.clear(); qtyField.clear(); priceField.clear();
            expiryPicker.setValue(null);
            categoryBox.setValue("Non-Perishable");
            feedback.getStyleClass().removeAll("feedback-success", "feedback-error");
            feedback.setText("");
            InputValidator.clearValidationStyles(nameField, qtyField, priceField);
        });

        return wrapper;
    }

    // =========================================================
    // VIEW 3 — UPDATE STOCK
    // =========================================================

    /**
     * Builds the Update Stock view.
     * Shows a dropdown of all products, highlights out-of-stock ones,
     * and lets the user set a new quantity.
     */
    public VBox buildUpdateTab() {

        Label title = new Label("Update Stock Quantity");
        title.getStyleClass().add("form-section-title");

        Label sub = new Label("Select a product and enter the new quantity. " +
                "Out-of-stock products are marked for easy identification.");
        sub.getStyleClass().add("form-section-sub");
        sub.setWrapText(true);

        // ---- Product selector ----
        ComboBox<Product> productCombo = new ComboBox<>();
        productCombo.setPromptText("Select a product...");
        productCombo.setMaxWidth(Double.MAX_VALUE);
        productCombo.getStyleClass().add("combo-box");

        // Show name + current qty in the dropdown
        productCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setText(null); setStyle(""); return; }
                setText(p.getName() + "   (qty: " + p.getQuantity() + ")");
                // Red text in dropdown for out-of-stock items
                setStyle(p.isOutOfStock() ? "-fx-text-fill: #dc2626;" : "");
            }
        });
        productCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "Select a product..." : p.getName());
            }
        });

        // Current stock info card
        Label currentInfo = new Label();
        currentInfo.getStyleClass().add("info-label");
        currentInfo.setVisible(false);

        productCombo.setOnAction(e -> {
            Product selected = productCombo.getValue();
            if (selected != null) {
                currentInfo.setVisible(true);
                currentInfo.setText(
                        "Current stock: " + selected.getQuantity() + " units   |   " +
                                "Price: KES " + String.format("%.2f", selected.getPrice()) + "   |   " +
                                (selected.isOutOfStock() ? "STATUS: OUT OF STOCK" : "Status: In Stock")
                );
                currentInfo.setStyle(selected.isOutOfStock()
                        ? "-fx-text-fill: #dc2626;"
                        : "-fx-text-fill: #64748b;");
            }
        });

        // ---- New quantity field ----
        TextField newQtyField = new TextField();
        newQtyField.setPromptText("Enter new quantity");
        newQtyField.getStyleClass().add("form-input");

        // ---- Feedback ----
        Label feedback = new Label();
        feedback.setWrapText(true);

        // ---- Buttons ----
        Button updateBtn  = new Button("Update Stock");
        Button reloadBtn  = new Button("Reload List");
        updateBtn.getStyleClass().add("btn-primary");
        reloadBtn.getStyleClass().add("btn-secondary");

        HBox btnRow = new HBox(12, updateBtn, reloadBtn);

        // ---- Form layout ----
        VBox form = new VBox(12,
                makeFormRow("Select Product", productCombo),
                currentInfo,
                makeFormRow("New Quantity *", newQtyField),
                btnRow,
                feedback
        );

        VBox card = new VBox(8, title, sub, form);
        card.getStyleClass().add("form-card");
        card.setMaxWidth(520);

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(24, 28, 24, 28));

        // Load products into dropdown
        refreshUpdateDropdown(productCombo);

        // ---- Button actions ----
        updateBtn.setOnAction(e -> {
            Product selected = productCombo.getValue();
            String qtyText   = newQtyField.getText().trim();

            feedback.getStyleClass().removeAll("feedback-success", "feedback-error");

            if (selected == null) {
                feedback.getStyleClass().add("feedback-error");
                feedback.setText("Please select a product.");
                return;
            }
            if (qtyText.isEmpty()) {
                feedback.getStyleClass().add("feedback-error");
                feedback.setText("Please enter a new quantity.");
                return;
            }

            try {
                int newQty = Integer.parseInt(qtyText);
                if (newQty < 0) throw new NumberFormatException();

                dao.updateStock(selected.getId(), newQty);
                refreshUpdateDropdown(productCombo);
                productCombo.setValue(null);
                currentInfo.setVisible(false);
                newQtyField.clear();

                feedback.getStyleClass().add("feedback-success");
                feedback.setText("Stock for \"" + selected.getName() +
                        "\" updated to " + newQty + " units.");

                showNotification("Stock updated: " + selected.getName());

            } catch (NumberFormatException ex) {
                feedback.getStyleClass().removeAll("feedback-success", "feedback-error");
                feedback.getStyleClass().add("feedback-error");
                feedback.setText("Quantity must be a whole number (0 or more).");

            } catch (IllegalArgumentException ex) {
                // Catches productId <= 0, newQuantity < 0, or quantity > MAX_STOCK_QUANTITY
                // thrown by the updated ProductDAO.updateStock() validation
                feedback.getStyleClass().removeAll("feedback-success", "feedback-error");
                feedback.getStyleClass().add("feedback-error");
                feedback.setText("Validation error: " + ex.getMessage());

            } catch (SQLException ex) {
                // Catches product-not-found or rows-affected anomalies
                // thrown by the updated ProductDAO.updateStock() validation
                feedback.getStyleClass().removeAll("feedback-success", "feedback-error");
                feedback.getStyleClass().add("feedback-error");
                feedback.setText("Database error: " + ex.getMessage());
            }
        });

        reloadBtn.setOnAction(e -> {
            refreshUpdateDropdown(productCombo);
            feedback.getStyleClass().removeAll("feedback-success", "feedback-error");
            feedback.setText("");
            showNotification("Product list reloaded");
        });

        return wrapper;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /** Wraps a label and a control into a VBox form row. */
    private VBox makeFormRow(String labelText, javafx.scene.Node control) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        return new VBox(5, lbl, control);
    }

    /** Loads all products from DB into the table and refreshes stat cards. */
    private void loadProducts(Label footerLabel) {
        try {
            List<Product> products = dao.getAll();
            productList.setAll(products);

            long oos  = products.stream().filter(Product::isOutOfStock).count();
            long inSt = products.size() - oos;
            double val = products.stream()
                    .mapToDouble(p -> p.getQuantity() * p.getPrice())
                    .sum();

            // Update stat cards
            if (statTotalVal != null) statTotalVal.setText(String.valueOf(products.size()));
            if (statOosVal   != null) statOosVal.setText(String.valueOf(oos));
            if (statInVal    != null) statInVal.setText(String.valueOf(inSt));
            if (statValueVal != null) statValueVal.setText(
                    String.format("KES %,.0f", val));

            // Update OOS badge in sidebar
            if (oosBadge != null) oosBadge.setText(String.valueOf(oos));

            if (footerLabel != null) {
                footerLabel.setText("Showing " + products.size() + " products  |  " +
                        oos + " out of stock");
            }
        } catch (SQLException e) {
            showError("Failed to load products: " + e.getMessage());
        }
    }

    /** Searches products by name and updates the table. */
    private void searchProducts(String keyword, Label footerLabel) {
        try {
            List<Product> results = dao.searchByName(keyword);
            productList.setAll(results);
            long oos = results.stream().filter(Product::isOutOfStock).count();
            if (footerLabel != null) {
                footerLabel.setText("Found " + results.size() +
                        " result(s) for \"" + keyword + "\"  |  " + oos + " out of stock");
            }
        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    /** Reloads product list into the Update Stock dropdown. */
    private void refreshUpdateDropdown(ComboBox<Product> combo) {
        try {
            List<Product> products = dao.getAll();
            if (combo != null) {
                combo.setItems(FXCollections.observableArrayList(products));
            }
        } catch (SQLException e) {
            showError("Could not load products: " + e.getMessage());
        }
    }

    /** Shows a brief notification label (you can replace with a Toast in MainApp). */
    private void showNotification(String message) {
        System.out.println("[INFO] " + message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}