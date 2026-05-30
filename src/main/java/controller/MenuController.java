package controller;

import app.MainApp;
import dao.MenuDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
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
    @FXML private Label formTitleLabel;

    private final MenuDAO menuDAO = new MenuDAO();
    private MenuItem selected;

    @FXML public void initialize() {
        filterCombo.getItems().setAll("Semua", "Makanan", "Minuman", "Side Dish");
        filterCombo.setValue("Semua");
        categoryCombo.getItems().setAll("Makanan", "Minuman", "Side Dish");
        categoryCombo.setValue("Makanan");
        statusCombo.getItems().setAll("Aktif", "Nonaktif");
        statusCombo.setValue("Aktif");
        searchField.textProperty().addListener((o,a,b)->loadMenus());
        filterCombo.setOnAction(e->loadMenus());
        loadMenus();
    }

    private void loadMenus() {
        menuGrid.getChildren().clear();
        List<MenuItem> items = menuDAO.findAll(searchField.getText(), filterCombo.getValue(), false);
        int col=0,row=0;
        for (MenuItem item: items) {
            VBox card = new VBox(7);
            card.getStyleClass().add("menu-card");
            Label name = new Label(item.getNamaMenu()); name.getStyleClass().add("card-title");
            Label cat = new Label(item.getKategori() + " • " + item.getStatus()); cat.getStyleClass().add("muted-text");
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
    }

    @FXML private void browseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Gambar Menu");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(MainApp.getPrimaryStage());
        if (f != null) imageField.setText(f.getAbsolutePath());
    }

    @FXML private void saveMenu() {
        try {
            MenuItem item = selected == null ? new MenuItem() : selected;
            item.setNamaMenu(nameField.getText().trim());
            item.setKategori(categoryCombo.getValue());
            item.setHarga(Integer.parseInt(priceField.getText().trim()));
            item.setStok(Integer.parseInt(stockField.getText().trim()));
            item.setGambar(imageField.getText());
            item.setStatus(statusCombo.getValue());
            if (item.getNamaMenu().isBlank()) { show("Nama menu wajib diisi."); return; }
            boolean ok = selected == null ? menuDAO.insert(item) : menuDAO.update(item);
            if (ok) { clearForm(); loadMenus(); }
        } catch (NumberFormatException e) { show("Harga dan stok wajib angka."); }
    }

    private void deleteItem(MenuItem item) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Hapus/nonaktifkan menu " + item.getNamaMenu() + "?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(b -> { if (b == ButtonType.YES) { menuDAO.delete(item.getIdMenu()); loadMenus(); }});
    }

    @FXML private void clearForm() {
        selected=null;
        formTitleLabel.setText("Tambah Menu");
        nameField.clear(); priceField.clear(); stockField.clear(); imageField.clear();
        categoryCombo.setValue("Makanan"); statusCombo.setValue("Aktif");
    }

    private void show(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
}
