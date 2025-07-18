package model;

import java.util.ArrayList;
import java.util.List;

public class Masa {
    private int masaNo;
    private List<Siparis> siparisler;

    public Masa(int masaNo) {
        this.masaNo = masaNo;
        this.siparisler = new ArrayList<>();
    }

    public int getMasaNo() { return masaNo; }

    public List<Siparis> getSiparisler() {
        return siparisler;
    }

    public void addSiparis(Siparis s) {
        siparisler.add(s);
    }

    public void removeSiparis(Siparis s) {
        siparisler.remove(s);
    }

    public double toplamTutar() {
        return siparisler.stream().mapToDouble(Siparis::getFiyat).sum();
    }

    @Override
    public String toString() {
        return "Masa " + masaNo;
    }
    
}
