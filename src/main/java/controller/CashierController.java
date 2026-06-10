package controller;

import dao.MenuDAO;
import dao.PromoDAO;
import dao.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.CartItem;
import model.MenuItem;
import model.Promo;
import model.Transaction;
import utils.CurrencyUtil;
import utils.ReceiptPrinter;
import utils.SessionManager;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CashierController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private GridPane menuGrid;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> itemColumn;
    @FXML private TableColumn<CartItem, Integer> qtyColumn;
    @FXML private TableColumn<CartItem, String> priceColumn;
    @FXML private TableColumn<CartItem, String> totalColumn;
    @FXML private ComboBox<String> orderTypeCombo;
    @FXML private ComboBox<String> paymentCombo;
    @FXML private ComboBox<Promo> promoCombo;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label grandTotalLabel;
    @FXML private VBox qrisBox;
    @FXML private ImageView qrisImage;
    @FXML private Button processButton;
    @FXML private Button confirmQrisButton;

    private final MenuDAO menuDAO = new MenuDAO();
    private final PromoDAO promoDAO = new PromoDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        categoryCombo.setItems(FXCollections.observableArrayList("Semua", "Makanan", "Minuman", "Side Dish"));
        categoryCombo.setValue("Semua");
        orderTypeCombo.setItems(FXCollections.observableArrayList("Dine In", "Take Away"));
        orderTypeCombo.setValue("Dine In");
        paymentCombo.setItems(FXCollections.observableArrayList("Cash", "QRIS", "E-Wallet"));
        paymentCombo.setValue("Cash");
        List<Promo> promos = new ArrayList<>();
        Promo none = new Promo();
        none.setIdPromo(0);
        none.setNamaPromo("Tanpa Promo");
        none.setJenisPromo("Nominal");
        none.setNilaiPromo(0);
        promos.add(none);
        promos.addAll(promoDAO.findAll(true));
        promoCombo.setItems(FXCollections.observableArrayList(promos));
        promoCombo.setValue(none);

        itemColumn.setCellValueFactory(new PropertyValueFactory<>("namaMenu"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("qty"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("hargaFormatted"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalFormatted"));
        cartTable.setItems(cartItems);
        cartTable.setRowFactory(tv -> {
            TableRow<CartItem> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) editQty(row.getItem());
            });
            return row;
        });
        searchField.textProperty().addListener((obs, o, n) -> loadMenus());
        categoryCombo.setOnAction(e -> loadMenus());
        promoCombo.setOnAction(e -> updateTotals());
        paymentCombo.setOnAction(e -> updatePaymentMode());
        loadQrisImage();
        updatePaymentMode();
        loadMenus();
        updateTotals();
    }

    private void loadQrisImage() {
        try {
            qrisImage.setImage(new Image(getClass().getResourceAsStream("/assets/qris/qris.png")));
        } catch (Exception e) {
            qrisImage.setImage(null);
        }
    }

    private void updatePaymentMode() {
        // Saat ganti metode pembayaran, sembunyikan dulu qrisBox (akan muncul setelah klik proses)
        qrisBox.setVisible(false);
        qrisBox.setManaged(false);
        confirmQrisButton.setVisible(false);
        confirmQrisButton.setManaged(false);
        processButton.setVisible(true);
        processButton.setManaged(true);
        processButton.setText("Proses Pembayaran & Cetak Nota");
    }

    /**
     * Memuat gambar menu dari path yang tersimpan di database.
     * Mendukung path absolut maupun relatif, lalu fallback ke gambar default.
     */
    private Image loadMenuImage(String gambarPath) {
        if (gambarPath != null && !gambarPath.isBlank()) {
            File file = new File(gambarPath);
            if (file.exists() && file.isFile()) {
                try {
                    return new Image(file.toURI().toString(), 130, 95, true, true);
                } catch (Exception ignored) {}
            }
        }

        try {
            InputStream is = getClass().getResourceAsStream("/images/default-logo.png");
            if (is != null) return new Image(is, 130, 95, true, true);
        } catch (Exception ignored) {}

        return null;
    }

    private void loadMenus() {
        menuGrid.getChildren().clear();
        List<MenuItem> items = menuDAO.findAll(searchField.getText(), categoryCombo.getValue(), true);
        int col = 0, row = 0;
        for (MenuItem item : items) {
            VBox card = new VBox(8);
            card.getStyleClass().add("menu-card");

            Image img = loadMenuImage(item.getGambar());
            if (img != null) {
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(130);
                imageView.setFitHeight(95);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                card.getChildren().add(imageView);
            }

            Label name = new Label(item.getNamaMenu());
            name.getStyleClass().add("card-title");
            Label category = new Label(item.getKategori() + " • Stok " + item.getStok());
            category.getStyleClass().add(item.getStok() <= 0 ? "error-text" : "muted-text");
            Label price = new Label(item.getHargaFormatted());
            price.getStyleClass().add("price-text");
            Button add = new Button(item.getStok() <= 0 ? "Habis" : "+");
            add.getStyleClass().add(item.getStok() <= 0 ? "danger-button" : "round-button");
            add.setDisable(item.getStok() <= 0);
            add.setOnAction(e -> addToCart(item));
            card.getChildren().addAll(name, category, price, add);
            menuGrid.add(card, col, row);
            col++;
            if (col == 3) { col = 0; row++; }
        }
    }

    private void addToCart(MenuItem item) {
        for (CartItem c : cartItems) {
            if (c.getIdMenu() == item.getIdMenu()) {
                if (c.getQty() + 1 > item.getStok()) { showAlert("Stok menu " + item.getNamaMenu() + " tidak mencukupi."); return; }
                c.setQty(c.getQty() + 1);
                cartTable.refresh();
                updateTotals();
                return;
            }
        }
        if (item.getStok() <= 0) { showAlert("Stok menu " + item.getNamaMenu() + " habis."); return; }
        cartItems.add(new CartItem(item, 1));
        updateTotals();
    }

    @FXML private void removeSelectedItem() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) cartItems.remove(selected);
        updateTotals();
    }

    @FXML private void clearCart() {
        cartItems.clear();
        updateTotals();
    }

    private void editQty(CartItem item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getQty()));
        dialog.setTitle("Ubah Qty");
        dialog.setHeaderText(item.getNamaMenu());
        dialog.setContentText("Jumlah:");
        dialog.showAndWait().ifPresent(value -> {
            try {
                int qty = Integer.parseInt(value);
                if (qty <= 0) cartItems.remove(item);
                else if (qty > item.getMenuItem().getStok()) showAlert("Qty melebihi stok menu. Stok tersedia: " + item.getMenuItem().getStok());
                else item.setQty(qty);
                cartTable.refresh();
                updateTotals();
            } catch (NumberFormatException ex) { showAlert("Qty harus angka."); }
        });
    }

    private int subtotal() {
        return cartItems.stream().mapToInt(CartItem::getTotal).sum();
    }

    private int discount() {
        int subtotal = subtotal();
        Promo promo = promoCombo.getValue();
        int diskon = 0;
        if (promo != null && promo.getIdPromo() != 0 && subtotal >= promo.getMinimalPembelian()) {
            if ("Persen".equals(promo.getJenisPromo())) diskon = subtotal * promo.getNilaiPromo() / 100;
            else if ("Nominal".equals(promo.getJenisPromo())) diskon = promo.getNilaiPromo();
        }
        if (diskon > subtotal) diskon = subtotal;
        return diskon;
    }

    private void updateTotals() {
        int subtotal = subtotal();
        int diskon = discount();
        subtotalLabel.setText(CurrencyUtil.formatRupiah(subtotal));
        discountLabel.setText(CurrencyUtil.formatRupiah(diskon));
        grandTotalLabel.setText(CurrencyUtil.formatRupiah(subtotal - diskon));
    }

    @FXML private void processPayment() {
        if (cartItems.isEmpty()) { showAlert("Keranjang masih kosong."); return; }
        if ("QRIS".equals(paymentCombo.getValue())) {
            // Tampilkan QRIS setelah kasir tekan tombol proses
            qrisBox.setVisible(true);
            qrisBox.setManaged(true);
            confirmQrisButton.setVisible(true);
            confirmQrisButton.setManaged(true);
            processButton.setVisible(false);
            processButton.setManaged(false);
            return;
        }
        completePayment();
    }

    @FXML private void confirmQrisPayment() {
        if (!"QRIS".equals(paymentCombo.getValue())) { showAlert("Metode pembayaran saat ini bukan QRIS."); return; }
        completePayment();
    }

    private void completePayment() {
        if (cartItems.isEmpty()) { showAlert("Keranjang masih kosong."); return; }
        if (!SessionManager.isLoggedIn()) { showAlert("Session login tidak ditemukan."); return; }
        int subtotal = subtotal();
        int diskon = discount();
        Transaction trx = new Transaction();
        trx.setIdUser(SessionManager.getCurrentUser().getIdUser());
        trx.setKasir(SessionManager.getCurrentUser().getNama());
        Promo promo = promoCombo.getValue();
        trx.setIdPromo(promo == null || promo.getIdPromo() == 0 ? null : promo.getIdPromo());
        trx.setTipeOrder(orderTypeCombo.getValue());
        trx.setMetodePembayaran(paymentCombo.getValue());
        trx.setSubtotal(subtotal);
        trx.setDiskon(diskon);
        trx.setTotal(subtotal - diskon);
        try {
            Transaction saved = transactionDAO.save(trx, new ArrayList<>(cartItems));
            Path receipt = ReceiptPrinter.saveReceipt(saved, new ArrayList<>(cartItems));
            showInfo("Transaksi berhasil. Nota tersimpan di: " + receipt.toAbsolutePath());
            cartItems.clear();
            updateTotals();
            updatePaymentMode(); // reset tampilan tombol & qrisBox
            loadMenus();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Gagal menyimpan transaksi: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Transaksi tersimpan, tetapi nota gagal dibuat: " + ex.getMessage());
        }
    }

    private void showAlert(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
}
