package model;

public class Urun {
    private int id;
    private String ad;
    private double fiyat;

    public Urun(int id, String ad, double fiyat) {
        this.id = id;
        this.ad = ad;
        this.fiyat = fiyat;
    }

    public int getId() { return id; }
    public String getAd() { return ad; }
    public double getFiyat() { return fiyat; }

    @Override
    public String toString() {
        return ad + " - " + fiyat + "₺";
    }
}
