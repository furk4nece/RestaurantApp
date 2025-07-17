package database;

import model.Masa;
import model.Siparis;
import model.Urun;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Tllwq123!"; // DATABASE ŞİFRENİZ NEYSE BU KISMA YAZIN !!!

    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Veritabanı bağlantısı başarılı.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Ürünleri Getir
    public List<Urun> urunleriGetir() {
        List<Urun> urunler = new ArrayList<>();
        String sql = "SELECT id, ad, fiyat FROM urunler";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                urunler.add(new Urun(rs.getInt("id"), rs.getString("ad"), rs.getDouble("fiyat")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return urunler;
    }

    // Masaya ait siparişleri getir
    public Masa masaSiparisGetir(int masaNo) {
        Masa masa = new Masa(masaNo);

        String sql = "SELECT urun_id, adet FROM siparisler WHERE masa_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, masaNo);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int urunId = rs.getInt("urun_id");
                int adet = rs.getInt("adet");
                Urun urun = urunGetir(urunId);
                if (urun != null) {
                    masa.addSiparis(new Siparis(urun, adet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return masa;
    }

    // Tek ürün getir
    public Urun urunGetir(int urunId) {
        String sql = "SELECT id, ad, fiyat FROM urunler WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, urunId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Urun(rs.getInt("id"), rs.getString("ad"), rs.getDouble("fiyat"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Siparişi ekle veya güncelle (varsa adet artır)
    public void siparisEkle(int masaNo, Siparis siparis) {
        try {
            connection.setAutoCommit(false);

            String kontrolSql = "SELECT adet FROM siparisler WHERE masa_no = ? AND urun_id = ?";
            PreparedStatement kontrolPs = connection.prepareStatement(kontrolSql);
            kontrolPs.setInt(1, masaNo);
            kontrolPs.setInt(2, siparis.getUrun().getId());
            ResultSet rs = kontrolPs.executeQuery();

            if (rs.next()) {
                // Ürün var, adet arttır
                int mevcutAdet = rs.getInt("adet");
                String guncelleSql = "UPDATE siparisler SET adet = ? WHERE masa_no = ? AND urun_id = ?";
                PreparedStatement guncellePs = connection.prepareStatement(guncelleSql);
                guncellePs.setInt(1, mevcutAdet + siparis.getAdet());
                guncellePs.setInt(2, masaNo);
                guncellePs.setInt(3, siparis.getUrun().getId());
                guncellePs.executeUpdate();
                guncellePs.close();
            } else {
                // Ürün yok, ekle
                String ekleSql = "INSERT INTO siparisler (masa_no, urun_id, adet) VALUES (?, ?, ?)";
                PreparedStatement eklePs = connection.prepareStatement(ekleSql);
                eklePs.setInt(1, masaNo);
                eklePs.setInt(2, siparis.getUrun().getId());
                eklePs.setInt(3, siparis.getAdet());
                eklePs.executeUpdate();
                eklePs.close();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Masa ekleme işlevi
    public void masaEkle(Masa masa) {
        String sql = "INSERT INTO masalar (masa_no) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, masa.getMasaNo());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //Masada bulunan ürünleri başka masaya taşı
    public void masaTasi(int kaynakMasaNo, int hedefMasaNo) {
        String sql = "UPDATE siparisler SET masa_no = ? WHERE masa_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, hedefMasaNo);
            ps.setInt(2, kaynakMasaNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Masada sipariş var mı kontrolü
    public boolean masaSiparisVarMi(int masaNo) {
        String sql = "SELECT COUNT(*) AS siparisSayisi FROM siparisler WHERE masa_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, masaNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("siparisSayisi");
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void masaSil(int masaNo) {
        try {
            connection.setAutoCommit(false);

            // Önce siparişleri sil
            String sqlSiparisSil = "DELETE FROM siparisler WHERE masa_no = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlSiparisSil)) {
                ps.setInt(1, masaNo);
                ps.executeUpdate();
            }

            // Sonra masayı sil
            String sqlMasaSil = "DELETE FROM masalar WHERE masa_no = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlMasaSil)) {
                ps.setInt(1, masaNo);
                ps.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    //Ürün ekleme işlevi
    public void urunEkle(Urun urun) {
        String sql = "INSERT INTO urunler (ad, fiyat) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, urun.getAd());
            ps.setDouble(2, urun.getFiyat());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Ürün Güncelleme fonksiyonu(şuanlık pasif)
    public void urunGuncelle(Urun urun) {
        String sql = "UPDATE urunler SET ad = ?, fiyat = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, urun.getAd());
            ps.setDouble(2, urun.getFiyat());
            ps.setInt(3, urun.getId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("Güncellenecek ürün bulunamadı.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Ürün silme işlevi
    public void urunSil(int urunId) {
        String sql = "DELETE FROM urunler WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, urunId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //Gün sonu işlevi(şuanlık pasif)
    public Map<String, Double> gunSonuRaporu() {
        Map<String, Double> urunGelirleri = new HashMap<>();
        double toplam = 0;

        String sql = "SELECT u.ad, s.adet, u.fiyat FROM siparisler s JOIN urunler u ON s.urun_id = u.id";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String urunAd = rs.getString("ad");
                int adet = rs.getInt("adet");
                double fiyat = rs.getDouble("fiyat");
                double tutar = fiyat * adet;

                urunGelirleri.put(urunAd, urunGelirleri.getOrDefault(urunAd, 0.0) + tutar);
                toplam += tutar;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        urunGelirleri.put("TOPLAM", toplam);
        return urunGelirleri;
        }

        // İstersen tüm siparişleri temizle:
        public void siparisleriTemizle() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM siparisler");
        } catch (SQLException e) {
            e.printStackTrace();
    }
}

    // Sipariş azalt veya sil
    public void siparisSil(int masaNo, Siparis siparis) {
        try {
            connection.setAutoCommit(false);

            String kontrolSql = "SELECT adet FROM siparisler WHERE masa_no = ? AND urun_id = ?";
            PreparedStatement kontrolPs = connection.prepareStatement(kontrolSql);
            kontrolPs.setInt(1, masaNo);
            kontrolPs.setInt(2, siparis.getUrun().getId());
            ResultSet rs = kontrolPs.executeQuery();

            if (rs.next()) {
                int mevcutAdet = rs.getInt("adet");
                int yeniAdet = mevcutAdet - siparis.getAdet();
                if (yeniAdet > 0) {
                    String guncelleSql = "UPDATE siparisler SET adet = ? WHERE masa_no = ? AND urun_id = ?";
                    PreparedStatement guncellePs = connection.prepareStatement(guncelleSql);
                    guncellePs.setInt(1, yeniAdet);
                    guncellePs.setInt(2, masaNo);
                    guncellePs.setInt(3, siparis.getUrun().getId());
                    guncellePs.executeUpdate();
                    guncellePs.close();
                } else {
                    String silSql = "DELETE FROM siparisler WHERE masa_no = ? AND urun_id = ?";
                    PreparedStatement silPs = connection.prepareStatement(silSql);
                    silPs.setInt(1, masaNo);
                    silPs.setInt(2, siparis.getUrun().getId());
                    silPs.executeUpdate();
                    silPs.close();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
