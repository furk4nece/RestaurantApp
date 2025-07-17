
//Bu kısımdaki kodlama giriş ekranında bir masaya tıkladıktan sonraki çalışan kodlama kısmıdır

package server;

import database.DatabaseManager;
import model.Masa;
import model.Siparis;
import model.Urun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasaPanel extends JPanel {

    private Masa masa;
    private DatabaseManager db;
    private ServerApp app;

    private DefaultListModel<Urun> urunListModel;
    private JList<Urun> urunList;

    private DefaultListModel<Siparis> siparisListModel;
    private JList<Siparis> siparisList;

    private JButton ekleButton;
    private JButton cikarButton;
    private JButton geriButton;

    public MasaPanel(Masa masa, DatabaseManager db, ServerApp app) {
        this.masa = masa;
        this.db = db;
        this.app = app;

        setLayout(new BorderLayout());

        // Ürün listesi
        urunListModel = new DefaultListModel<>();
        urunList = new JList<>(urunListModel);
        JScrollPane urunScroll = new JScrollPane(urunList);
        urunScroll.setBorder(BorderFactory.createTitledBorder("Ürünler"));

        // Sipariş listesi
        siparisListModel = new DefaultListModel<>();
        siparisList = new JList<>(siparisListModel);
        JScrollPane siparisScroll = new JScrollPane(siparisList);
        siparisScroll.setBorder(BorderFactory.createTitledBorder("Siparişler"));

        // Butonlar
        ekleButton = new JButton("Ekle");
        cikarButton = new JButton("Çıkar");
        geriButton = new JButton("Geri");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(ekleButton);
        buttonPanel.add(cikarButton);
        buttonPanel.add(geriButton);

        add(urunScroll, BorderLayout.WEST);
        add(siparisScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Veritabanından ürünleri yükle
        List<Urun> urunler = db.urunleriGetir();
        urunler.forEach(urunListModel::addElement);

        // Masanın siparişlerini listele
        refreshSiparisList();

        // Buton İşlevleri
        ekleButton.addActionListener(this::urunEkle);
        cikarButton.addActionListener(this::urunCikar);
        geriButton.addActionListener(e -> app.anasayfayaDon());
    }

    // Sipariş listesini güncelleme
    private void refreshSiparisList() {
        siparisListModel.clear();
        Map<Integer, Siparis> toplamSiparisler = new HashMap<>();

        for (Siparis s : masa.getSiparisler()) {
        if (toplamSiparisler.containsKey(s.getUrun().getId())) {
            Siparis mevcut = toplamSiparisler.get(s.getUrun().getId());
            int yeniAdet = mevcut.getAdet() + s.getAdet();
            toplamSiparisler.put(s.getUrun().getId(), new Siparis(s.getUrun(), yeniAdet));
        } else {
            toplamSiparisler.put(s.getUrun().getId(), s);
        }
        }

        toplamSiparisler.values().forEach(siparisListModel::addElement);
    }

    //Masa toplam işlevi gösterir(şuanlık pasif)
    public double masaToplamTutar(Masa masa) {
        return masa.getSiparisler().stream()
               .mapToDouble(s -> s.getUrun().getFiyat() * s.getAdet())
               .sum();
    }

    //Masaya ürün ekleme işlevi
    private void urunEkle(ActionEvent e) {
        Urun secilenUrun = urunList.getSelectedValue();
        if (secilenUrun == null) {
            JOptionPane.showMessageDialog(this, "Lütfen bir ürün seçin.");
            return;
        }

        // Aynı üründen varsa adeti artır, yoksa yeni ekle
        boolean bulundu = false;
        for (Siparis s : masa.getSiparisler()) {
            if (s.getUrun().getId() == secilenUrun.getId()) {
                s = new Siparis(secilenUrun, s.getAdet() + 1);
                masa.getSiparisler().remove(s);
                masa.getSiparisler().add(s);
                bulundu = true;
                break;
            }
        }
        if (!bulundu) {
            masa.addSiparis(new Siparis(secilenUrun, 1));
        }

        // Veritabanına kaydet
        db.siparisEkle(masa.getMasaNo(), new Siparis(secilenUrun, 1));
        // Sipariş listesini güncelle
        masa = db.masaSiparisGetir(masa.getMasaNo());
        refreshSiparisList();
    }

    private void urunCikar(ActionEvent e) {
        Siparis secilenSiparis = siparisList.getSelectedValue();
        if (secilenSiparis == null) {
            JOptionPane.showMessageDialog(this, "Lütfen bir sipariş seçin.");
            return;
        }

        // Tek adet ise sil, değilse azalt
        db.siparisSil(masa.getMasaNo(), new Siparis(secilenSiparis.getUrun(), 1));
        masa = db.masaSiparisGetir(masa.getMasaNo());
        refreshSiparisList();
    }
}
