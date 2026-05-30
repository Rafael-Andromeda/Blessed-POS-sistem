package controller;

import app.MainApp;
import dao.IngredientDAO;
import dao.MenuDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.Ingredient;
import model.MenuIngredient;
import model.MenuItem;

import java.io.File;
import java.util.List;

public class MenuController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private GridPane menuGrid;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField imageField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<Ingredient> ingredientCombo;
    @FXML private TextField ingredientQtyField;
    @FXML private TableView<MenuIngredient> ingredientTable;
    @FXML private TableColumn<MenuIngredient, String> ingredientColumn;
    @FXML private TableColumn<MenuIngredient, Double> requiredColumn;
    @FXML private TableColumn<MenuIngredient, String> unitColumn;
    @FXML private Label formTitleLabel;

    private final MenuDAO menuDAO = new MenuDAO();
    private final IngredientDAO ingredientDAO = new IngredientDAO();
    private final ObservableList<MenuIngredient> recipeItems = FXCollections.observableArrayList();
    private MenuItem selected;

    @FXML public void initialize() {
        filterCombo.getItems().setAll("Semua", "Makanan", "Minuman", "Side Dish");
        filterCombo.setValue("Semua");
        categoryCombo.getItems().setAll("Makanan", "Minuman", "Side Dish");
        categoryCombo.setValue("Makanan");
        statusCombo.getItems().setAll("Aktif", "Nonaktif");
        statusCombo.setValue("Aktif");
        ingredientColumn.setCellValueFactory(new PropertyValueFactory<>("namaBahan"));
        requiredColumn.setCellValueFactory(new PropertyValueFactory<>("jumlahDibutuhkan"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        ingredientTable.setItems(recipeItems);
        searchField.textProperty().addListener((o,a,b)->loadMenus());
        filterCombo.setOnAction(e->loadMenus());
        loadIngredientOptions();
        loadMenus();
    }

    private void loadIngredientOptions() {
        ingredientCombo.setItems(FXCollections.observableArrayList(ingredientDAO.findAll()));
    }

    private void loadMenus() {
        menuGrid.getChildren().clear();
        List<MenuItem> items = menuDAO.findAll(searchField.getText(), filterCombo.getValue(), false);
        int col=0,row=0;
        for (MenuItem item: items) {
            VBox card = new VBox(7);
            card.getStyleClass().add("menu-card");
            Label name = new Label(item.getNamaMenu()); name.getStyleClass().add("card-title");
            Label cat = new Label(item.getKategori() + " • " + item.getStatus() + " • Stok " + item.getStok()); cat.getStyleClass().add(item.getStok() <= 0 ? "error-text" : "muted-text");
            Label price = new Label(item.getHargaFormatted()); price.getStyleClass().add("price-text");
            Button edit = new Button("Edit"); edit.getStyleClass().add("secondary-button"); edit.setOnAction(e->selectItem(item));
            Button delete = new Button("Hapus"); delete.getStyleClass().add("danger-button"); delete.setOnAction(e->deleteItem(item));
            card.getChildren().addAll(name, cat, price, edit, delete);
            menuGrid.add(card,col,row);
            col++; if(col==3){col=0;row++;}
        }
    }

    private void selectItem(MenuItem item) {
        selected=item;
        formTitleLabel.setText("Edit Menu");
        nameField.setText(item.getNamaMenu());
        categoryCombo.setValue(item.getKategori());
        priceField.setText(String.valueOf(item.getHarga()));
        stockField.setText(String.valueOf(item.getStok()));
        imageField.setText(item.getGambar());
        statusCombo.setValue(item.getStatus());
        recipeItems.setAll(menuDAO.findIngredientsByMenuId(item.getIdMenu()));
    }

    @FXML private void browseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Gambar Menu");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(MainApp.getPrimaryStage());
        if (f != null) imageField.setText(f.getAbsolutePath());
    }

    @FXML private void addIngredient() {
        Ingredient ingredient = ingredientCombo.getValue();
        if (ingredient == null) { show("Pilih bahan baku terlebih dahulu."); return; }
        double qty;
        try {
            qty = Double.parseDouble(ingredientQtyField.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) { show("Jumlah bahan wajib angka."); return; }
        if (qty <= 0) { show("Jumlah bahan harus lebih dari 0."); return; }
        for (MenuIngredient row : recipeItems) {
            if (row.getBahanBakuId() == ingredient.getIdBahan()) {
                row.setJumlahDibutuhkan(qty);
                ingredientTable.refresh();
                ingredientQtyField.clear();
                return;
            }
        }
        MenuIngredient row = new MenuIngredient();
        row.setBahanBakuId(ingredient.getIdBahan());
        row.setNamaBahan(ingredient.getNamaBahan());
        row.setSatuan(ingredient.getSatuan());
        row.setJumlahDibutuhkan(qty);
        recipeItems.add(row);
        ingredientQtyField.clear();
    }

    @FXML private void removeIngredient() {
        MenuIngredient selectedIngredient = ingredientTable.getSelectionModel().getSelectedItem();
        if (selectedIngredient != null) recipeItems.remove(selectedIngredient);
    }

    @FXML private void saveMenu() {
        try {
            MenuItem item = selected == null ? new MenuItem() : selected;
            item.setNamaMenu(nameField.getText().trim());
            item.setKategori(categoryCombo.getValue());
            item.setHarga(Integer.parseInt(priceField.getText().trim()));
            item.setStok(stockField.getText().isBlank() ? 0 : Integer.parseInt(stockField.getText().trim()));
            item.setGambar(imageField.getText());
            item.setStatus(statusCombo.getValue());
            if (item.getNamaMenu().isBlank()) { show("Nama menu wajib diisi."); return; }
            if (item.getHarga() <= 0) { show("Harga harus lebih dari 0."); return; }
            boolean ok = selected == null ? menuDAO.insertWithIngredients(item, recipeItems) : menuDAO.updateWithIngredients(item, recipeItems);
            if (ok) { clearForm(); loadMenus(); }
            else show("Menu gagal disimpan. Cek kembali data atau database.");
        } catch (NumberFormatException e) { show("Harga, stok, dan jumlah bahan wajib angka."); }
    }

    private void deleteItem(MenuItem item) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Hapus menu " + item.getNamaMenu() + " dari daftar aktif? History transaksi lama tetap aman.", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(b -> { if (b == ButtonType.YES) { menuDAO.delete(item.getIdMenu()); clearForm(); loadMenus(); }});
    }

    @FXML private void clearForm() {
        selected=null;
        formTitleLabel.setText("Tambah Menu");
        nameField.clear(); priceField.clear(); stockField.clear(); imageField.clear(); ingredientQtyField.clear(); recipeItems.clear();
        categoryCombo.setValue("Makanan"); statusCombo.setValue("Aktif");
        if (ingredientTable != null) ingredientTable.getSelectionModel().clearSelection();
    }

    private void show(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
}
