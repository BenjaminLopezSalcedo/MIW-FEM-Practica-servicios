package es.upm.miw.practicaservicios.data;

/** Un terremoto, con solo los campos que usa la app. */
public class Sismo {

    private String id;
    private double mag;
    private String lugar;
    private long tiempo;
    private double lat;
    private double lon;
    private double prof;

    public Sismo() {}

    public Sismo(String id, double mag, String lugar, long tiempo,
                 double lat, double lon, double prof) {
        this.id = id;
        this.mag = mag;
        this.lugar = lugar;
        this.tiempo = tiempo;
        this.lat = lat;
        this.lon = lon;
        this.prof = prof;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getMag() { return mag; }
    public void setMag(double mag) { this.mag = mag; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public long getTiempo() { return tiempo; }
    public void setTiempo(long tiempo) { this.tiempo = tiempo; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
    public double getProf() { return prof; }
    public void setProf(double prof) { this.prof = prof; }
}