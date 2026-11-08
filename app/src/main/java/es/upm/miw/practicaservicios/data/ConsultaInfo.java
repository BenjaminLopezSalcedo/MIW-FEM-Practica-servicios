package es.upm.miw.practicaservicios.data;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/** Datos de la petición: qué se pidió y cuándo. */
public class ConsultaInfo {

    @ServerTimestamp
    private Date fechaConsulta;
    private String zonaNombre;
    private AreaConsulta area;
    private String fechaInicio;
    private String fechaFin;
    private Double magMin;

    public ConsultaInfo() {}

    public Date getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(Date fechaConsulta) { this.fechaConsulta = fechaConsulta; }
    public String getZonaNombre() { return zonaNombre; }
    public void setZonaNombre(String zonaNombre) { this.zonaNombre = zonaNombre; }
    public AreaConsulta getArea() { return area; }
    public void setArea(AreaConsulta areaConsulta) { this.area = areaConsulta; }
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    public Double getMagMin() { return magMin; }
    public void setMagMin(Double magMin) { this.magMin = magMin; }
}