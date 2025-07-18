package server;

import database.DatabaseManager;
import model.Masa;
import model.Siparis;
import model.Urun;
import java.util.List;



import javax.swing.*;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

public class ServerApp extends JFrame {

    private DatabaseManager db;
    private JPanel mainPanel;
    private JPanel masaPanel;
    private CardLayout cardLayout;
    

    // Ana ekranda masa butonları ve masalara ait modeller
    private Map<Integer, Masa> masalar = new HashMap<>();
    private Map<Integer, MasaPanel> masaPanelleri = new HashMap<>();

    
    private JPanel anaEkran;
    private JPanel masaButonPaneli;

    public ServerApp() {
        db = new DatabaseManager();

        setTitle("Restoran Kontrol Merkezi");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Ana ekran paneli
        anaEkran = new JPanel(new BorderLayout());

        // Masa butonları paneli
        masaButonPaneli = new JPanel(new GridLayout(2, 5, 10, 10));

        // Masaları veritabanından yükle ve butonları ekle
        List<Masa> mevcutMasalar = db.masalariGetir();
        for (Masa masa : mevcutMasalar) {
            int masaNo = masa.getMasaNo();
            masalar.put(masaNo, masa);

            JButton masaButon = new JButton("Masa " + masaNo);
            masaButon.addActionListener(e -> masaButonTiklandi(masaNo));
            masaButonPaneli.add(masaButon);
        }

        // Üstte butonlar için panel 
        JPanel butonPaneli = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton masaEkleButton = new JButton("Masa Ekle");
        JButton urunGuncelleButton = new JButton("Ürün Güncelle");
        JButton masaTasiButton = new JButton("Masa Ürünlerini Taşı");
        JButton masaSilButton = new JButton("Masa Sil");
        JButton urunEkleButton = new JButton("Ürün Ekle");
        JButton urunSilButton = new JButton("Ürün Sil");

        // Butonlara tıklama olayları
        masaEkleButton.addActionListener(e -> masaEkle());
        urunGuncelleButton.addActionListener(e -> urunGuncelle());
        masaTasiButton.addActionListener(e -> masaTasi());
        masaSilButton.addActionListener(e -> masaSil());
        urunEkleButton.addActionListener(e -> urunEkle());
        urunSilButton.addActionListener(e -> urunSil());
        
        butonPaneli.add(urunEkleButton);
        butonPaneli.add(urunSilButton);
        butonPaneli.add(urunGuncelleButton);
        butonPaneli.add(masaEkleButton);
        butonPaneli.add(masaSilButton);
        butonPaneli.add(masaTasiButton);
        
        

        anaEkran.add(butonPaneli, BorderLayout.NORTH);
        anaEkran.add(masaButonPaneli, BorderLayout.CENTER);

        mainPanel.add(anaEkran, "ANA");

        masaPanel = new JPanel(new BorderLayout());
        mainPanel.add(masaPanel, "MASA");

        add(mainPanel);

        cardLayout.show(mainPanel, "ANA");
    }

    private void urunEkle() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField adField = new JTextField();
        JTextField fiyatField = new JTextField();

        panel.add(new JLabel("Ürün Adı:"));
        panel.add(adField);
        panel.add(new JLabel("Fiyatı:"));
        panel.add(fiyatField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Yeni Ürün Ekle", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String ad = adField.getText().trim();
            try {
                double fiyat = Double.parseDouble(fiyatField.getText().trim());
                if (!ad.isEmpty() && fiyat > 0) {
                    db.urunEkle(new Urun(0, ad, fiyat)); // id veritabanında otomatik atanır
                    JOptionPane.showMessageDialog(this, "Ürün başarıyla eklendi.");
                } else {
                    JOptionPane.showMessageDialog(this, "Geçerli ürün adı ve fiyat girin.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Fiyat sayı formatında olmalı.");
            }
        }
    }

    public void urunGuncelle(){
        JTextField fiyatField = new JTextField();
        List<Urun> urunler = db.urunleriGetir();
        if (urunler.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Güncellenecek ürün bulunamadı.");
            return;
        }

        JComboBox<Urun> urunComboBox = new JComboBox<>(urunler.toArray(new Urun[0]));

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Güncellemek istediğiniz ürünü seçin:"));
        panel.add(urunComboBox);
        panel.add(new JLabel("Fiyatı:"));
        panel.add(fiyatField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Ürün Güncelle", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Urun secilenUrun = (Urun) urunComboBox.getSelectedItem();
        try {
                double yeniFiyat = Double.parseDouble(fiyatField.getText());

                int onay = JOptionPane.showConfirmDialog(this,
                    secilenUrun.getAd() + " ürünü " + yeniFiyat + " TL olarak güncellensin mi?",
                    "Onay", JOptionPane.YES_NO_OPTION);

                    if (onay == JOptionPane.YES_OPTION) {
                        db.urunGuncelle(secilenUrun.getId(), yeniFiyat);
                        JOptionPane.showMessageDialog(this, "Ürün güncellendi: " + secilenUrun.getAd());
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Geçerli bir fiyat girin.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void urunSil() {
        List<Urun> urunler = db.urunleriGetir();
        if (urunler.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silinecek ürün bulunamadı.");
            return;
        }

        JComboBox<Urun> urunComboBox = new JComboBox<>(urunler.toArray(new Urun[0]));

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Silmek istediğiniz ürünü seçin:"));
        panel.add(urunComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Ürün Sil", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Urun secilenUrun = (Urun) urunComboBox.getSelectedItem();
            if (secilenUrun != null) {
                int onay = JOptionPane.showConfirmDialog(this,
                        secilenUrun.getAd() + " ürünü silinsin mi?",
                        "Onay", JOptionPane.YES_NO_OPTION);

                if (onay == JOptionPane.YES_OPTION) {
                    db.urunSil(secilenUrun.getId());
                    JOptionPane.showMessageDialog(this, "Ürün silindi: " + secilenUrun.getAd());
                }
            }
        }
    }
    
    private void masaEkle() {
        String input = JOptionPane.showInputDialog(this, "Yeni masa numarası girin:");
        if (input != null) {
            try {
                int yeniMasaNo = Integer.parseInt(input);

                if (masalar.containsKey(yeniMasaNo)) {
                    JOptionPane.showMessageDialog(this, "Bu masa zaten mevcut.");
                    return;
                }

                Masa yeniMasa = new Masa(yeniMasaNo);
                masalar.put(yeniMasaNo, yeniMasa);
                db.masaEkle(yeniMasa);

                // Masa butonunu GUI'de masaButonPaneli'ne ekle
                JButton masaButonu = new JButton("Masa " + yeniMasaNo);
                masaButonu.addActionListener(e -> masaButonTiklandi(yeniMasaNo));
                masaButonPaneli.add(masaButonu);
                masaButonPaneli.revalidate();
                masaButonPaneli.repaint();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı girin.");
            }
        }
    }

    private void masaTasi() {
        Integer[] masaNumaralari = masalar.keySet().toArray(new Integer[0]);

        JComboBox<Integer> kaynakMasaBox = new JComboBox<>(masaNumaralari);
        JComboBox<Integer> hedefMasaBox = new JComboBox<>(masaNumaralari);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Kaynak Masa:"));
        panel.add(kaynakMasaBox);
        panel.add(new JLabel("Hedef Masa:"));
        panel.add(hedefMasaBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Masa Ürünlerini Taşı", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int kaynakMasa = (int) kaynakMasaBox.getSelectedItem();
            int hedefMasa = (int) hedefMasaBox.getSelectedItem();

            if (kaynakMasa == hedefMasa) {
                JOptionPane.showMessageDialog(this, "Kaynak ve hedef masa farklı olmalı.");
                return;
            }

            db.masaTasi(kaynakMasa, hedefMasa);
            JOptionPane.showMessageDialog(this, "Siparişler taşındı.");
            anasayfayaDon();
        }
    }


    private void masaSil() {
        JComboBox<Masa> masaComboBox = new JComboBox<>(masalar.values().toArray(new Masa[0]));

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Silmek istediğiniz ürünü seçin:"));
        panel.add(masaComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Ürün Sil", JOptionPane.OK_CANCEL_OPTION);
        Masa secilenMasa = (Masa) masaComboBox.getSelectedItem();
        
        if (secilenMasa == null) return;

        try {
            int masaNo = secilenMasa.getMasaNo();
            if (!masalar.containsKey(masaNo)) {
                JOptionPane.showMessageDialog(this, "Bu masa mevcut değil.");
                return;
            }

            boolean siparisVar = db.masaSiparisVarMi(masaNo);
            if (siparisVar) {
                int cevap = JOptionPane.showConfirmDialog(this, 
                    "Masada ürünler var, silmek istediğinize emin misiniz?", 
                    "Onay", JOptionPane.YES_NO_OPTION);
                if (cevap != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            db.masaSil(masaNo);
            masalar.remove(masaNo);

            // Masa butonunu ana ekrandan kaldır
            JPanel masaButonPaneli = (JPanel)((BorderLayout) anaEkran.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            Component[] components = masaButonPaneli.getComponents();
            for (Component c : components) {
                if (c instanceof JButton) {
                    JButton btn = (JButton)c;
                    if (btn.getText().equals("Masa " + masaNo)) {
                        masaButonPaneli.remove(btn);
                        break;
                    }
                }
        }
        masaButonPaneli.revalidate();
        masaButonPaneli.repaint();

        JOptionPane.showMessageDialog(this, "Masa ve siparişler başarıyla silindi.");

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı girin.");
    }
    }

    //Bir masaya tıklandıktan sonraki işlev
    private void masaButonTiklandi(int masaNo) {    
        Masa masa = db.masaSiparisGetir(masaNo);
        
        if (masa == null) {
            JOptionPane.showMessageDialog(this, "Masa bulunamadı.");
            return;
        }

        masalar.put(masaNo, masa);
        MasaPanel panel = new MasaPanel(masa, db, this);
        masaPanelleri.put(masaNo, panel);

        masaPanel.removeAll();
        masaPanel.add(panel, BorderLayout.CENTER);
        masaPanel.revalidate();
        masaPanel.repaint();

        cardLayout.show(mainPanel, "MASA");
    }


    public void anasayfayaDon() {
        // Masaları veritabanından yeniden yükle
        for (Integer masaNo : masalar.keySet()) {
            Masa masa = db.masaSiparisGetir(masaNo);
            if (masa != null) {
                masalar.put(masaNo, masa);
            }
        }
        cardLayout.show(mainPanel, "ANA");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerApp app = new ServerApp();
            app.setVisible(true);
        });
    }
}
