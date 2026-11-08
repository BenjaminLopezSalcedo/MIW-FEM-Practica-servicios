package es.upm.miw.practicaservicios.data;

/** Zona circular consultada: un punto y un radio. */
public class AreaConsulta {

    private double lat;
    private double lon;
    private double radioKm;

    public AreaConsulta() {}

    public AreaConsulta(double lat, double lon, double radioKm) {
        this.lat = lat;
        this.lon = lon;
        this.radioKm = radioKm;
    }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
    public double getRadioKm() { return radioKm; }
    public void setRadioKm(double radioKm) { this.radioKm = radioKm; }
}