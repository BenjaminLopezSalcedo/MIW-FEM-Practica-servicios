package es.upm.miw.practicaservicios.data;

import java.util.Map;

/** Estadísticas calculadas sobre TODOS los sismos recibidos, no solo sobre los guardados. */
public class Estadisticas {

    private int NumTotalSismos; // número total de terremotos reales obtenidos en la consulta
    private int guardados; // número de terremotos guardados en la lista (max 100)
    private boolean truncado; // true si la API alcanzó el límite puede haber más sismos de los contados
    private Double magMax;
    private Double magMedia;
    private Double profMedia;
    private String eventoMaxId;
    private String eventoMaxLugar;
    private Map<String, Integer> rangos; //micro (<2), menor (2-3.9), ligero (4-4.9), moderado (5-5.9), fuerte (>=6)

    public Estadisticas() {}

    public int getNumTotalSismos() { return NumTotalSismos; }
    public void setNumTotalSismos(int numTotalSismos) { this.NumTotalSismos = numTotalSismos; }
    public int getGuardados() { return guardados; }
    public void setGuardados(int guardados) { this.guardados = guardados; }
    public boolean isTruncado() { return truncado; }
    public void setTruncado(boolean truncado) { this.truncado = truncado; }
    public Double getMagMax() { return magMax; }
    public void setMagMax(Double magMax) { this.magMax = magMax; }
    public Double getMagMedia() { return magMedia; }
    public void setMagMedia(Double magMedia) { this.magMedia = magMedia; }
    public Double getProfMedia() { return profMedia; }
    public void setProfMedia(Double profMedia) { this.profMedia = profMedia; }
    public String getEventoMaxId() { return eventoMaxId; }
    public void setEventoMaxId(String eventoMaxId) { this.eventoMaxId = eventoMaxId; }
    public String getEventoMaxLugar() { return eventoMaxLugar; }
    public void setEventoMaxLugar(String eventoMaxLugar) { this.eventoMaxLugar = eventoMaxLugar; }
    public Map<String, Integer> getRangos() { return rangos; }
    public void setRangos(Map<String, Integer> rangos) { this.rangos = rangos; }
}
