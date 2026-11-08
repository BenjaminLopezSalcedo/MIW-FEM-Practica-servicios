package es.upm.miw.practicaservicios.data;

import com.google.firebase.firestore.DocumentId;
import java.util.List;

/** Una consulta guardada en users/{uid}/consultas/{id}. */
public class ConsultaUsgs {

    @DocumentId
    private String id;
    private ConsultaInfo info;
    private Estadisticas resumen;
    private List<Sismo> sismos;

    public ConsultaUsgs() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ConsultaInfo getInfo() { return info; }
    public void setInfo(ConsultaInfo info) { this.info = info; }
    public Estadisticas getResumen() { return resumen; }
    public void setResumen(Estadisticas resumen) { this.resumen = resumen; }
    public List<Sismo> getSismos() { return sismos; }
    public void setSismos(List<Sismo> sismos) { this.sismos = sismos; }
}