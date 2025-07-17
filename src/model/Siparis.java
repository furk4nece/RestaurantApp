package model;

public class Siparis {
    private Urun urun;
    private int adet;
    private double porsiyon = 1.0;

    public Siparis(Urun urun, int adet) {
        this.urun = urun;
        this.adet = adet;
        this.porsiyon = porsiyon;
    }

    public Urun getUrun() { return urun; }
    public int getAdet() { return adet; }
    public double getPorsiyon() { return porsiyon; }

    public double getFiyat() {
        return urun.getFiyat() * adet * porsiyon;
    }

    @Override
    public String toString() {
       return urun.getAd() + " x" + adet + " (Porsiyon: " + porsiyon + ") = " + getFiyat() + "₺";
    }   
}
