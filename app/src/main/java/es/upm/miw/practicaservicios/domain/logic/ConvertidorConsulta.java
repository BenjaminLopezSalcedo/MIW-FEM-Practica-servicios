package es.upm.miw.practicaservicios.domain.logic;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.miw.practicaservicios.R;
import es.upm.miw.practicaservicios.api.models.Feature;
import es.upm.miw.practicaservicios.api.models.Properties;
import es.upm.miw.practicaservicios.api.models.UsgsResponse;
import es.upm.miw.practicaservicios.data.Estadisticas;
import es.upm.miw.practicaservicios.data.Sismo;

public class ConvertidorConsulta {

    public static List<Sismo> extractSismos(UsgsResponse respuesta){
        List<Sismo> sismos = new ArrayList<>();
        if(respuesta != null){
            for (Feature f : respuesta.getFeatures()) {
                Sismo sismo;
                if(f!=null){
                    Properties p = f.getProperties();
                    if(p.getType().equals("earthquake") &&
                            p.getMag() != null &&
                            f.getGeometry().getCoordinates() != null
                    ){
                        List<Double> coords = f.getGeometry().getCoordinates();
                        long tiempo = (p.getTime() != null) ? p.getTime() : 0L;
                        sismo = new Sismo(
                                f.getId(),
                                p.getMag(),
                                p.getPlace(),
                                tiempo,
                                coords.get(1),
                                coords.get(0),
                                coords.get(2));
                        sismos.add(sismo);
                    }
                }
            }
        }
        return sismos;
    }

    public static Estadisticas extractEstadisticas(Context c, List<Sismo> sismos){
        Estadisticas estadisticas = new Estadisticas();

        String micro = c.getString(R.string.range_micro);
        String menor = c.getString(R.string.range_minor);
        String ligero = c.getString(R.string.range_light);
        String moderado = c.getString(R.string.range_moderate);
        String fuerte = c.getString(R.string.range_strong);

        Map<String, Integer> rangos = new LinkedHashMap<>();
        rangos.put(micro, 0);
        rangos.put(menor, 0);
        rangos.put(ligero, 0);
        rangos.put(moderado, 0);
        rangos.put(fuerte, 0);

        estadisticas.setNumTotalSismos(sismos.size());

        if(!sismos.isEmpty()){
            double sumaMag = 0;
            double sumaProf = 0;
            Sismo maxSismo = sismos.get(0);

            for (Sismo s : sismos) {
                double m = s.getMag();

                sumaMag += m;
                sumaProf += s.getProf();
                if (m > maxSismo.getMag()) {
                    maxSismo = s;
                }

                String r = m < 2 ?
                        micro : m < 4 ?
                        menor : m < 5 ?
                        ligero: m < 6 ?
                        moderado : fuerte;

                rangos.put(r, rangos.get(r) + 1);
            }

            estadisticas.setMagMax(maxSismo.getMag());
            estadisticas.setEventoMaxId(maxSismo.getId());
            estadisticas.setEventoMaxLugar(maxSismo.getLugar());
            estadisticas.setMagMedia(sumaMag/sismos.size());
            estadisticas.setProfMedia(sumaProf/sismos.size());
        }
        estadisticas.setRangos(rangos);

        return estadisticas;
    }


}
