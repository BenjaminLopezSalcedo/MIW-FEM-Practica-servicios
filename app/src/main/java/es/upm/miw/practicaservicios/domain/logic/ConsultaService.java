package es.upm.miw.practicaservicios.domain.logic;

import android.content.Context;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import es.upm.miw.practicaservicios.api.UsgsApiService;
import es.upm.miw.practicaservicios.api.models.UsgsResponse;
import es.upm.miw.practicaservicios.data.AreaConsulta;
import es.upm.miw.practicaservicios.data.ConsultaInfo;
import es.upm.miw.practicaservicios.data.ConsultaUsgs;
import es.upm.miw.practicaservicios.data.Estadisticas;
import es.upm.miw.practicaservicios.data.Sismo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConsultaService {

    private static final String API_BASE_URL = "https://earthquake.usgs.gov/";
    private static final int LIMIT_API = 500;    // máximo de eventos que pedimos a USGS
    private static final int MAX_SISMOS = 100;   // máximo de sismos que guardamos en la lista

    private final Context context;
    private final UsgsApiService api;

    public ConsultaService(Context context) {
        this.context = context.getApplicationContext();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.api = retrofit.create(UsgsApiService.class);
    }

    public static ConsultaInfo globalLastHourInfo() {
        DateTimeFormatter fmt = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC);
        Instant ahora = Instant.now();

        ConsultaInfo info = new ConsultaInfo();
        info.setZonaNombre("Global");
        info.setArea(null);
        info.setMagMin(null);
        info.setFechaInicio(fmt.format(ahora.minus(1, ChronoUnit.HOURS)));
        info.setFechaFin(fmt.format(ahora));
        return info;
    }

    public void askForSismos(ConsultaInfo info, Callback<UsgsResponse> callback) {
        AreaConsulta area = info.getArea();
        Double lat = (area != null)
                ? area.getLat()
                : null;
        Double lon = (area != null)
                ? area.getLon()
                : null;
        Double radio = (area != null)
                ? area.getRadioKm()
                : null;

        Call<UsgsResponse> call = api.getEarthquakes(
                "geojson",
                info.getFechaInicio(),
                info.getFechaFin(),
                info.getMagMin(),
                lat,
                lon,
                radio,
                "time",
                LIMIT_API);

        call.enqueue(callback);
    }

    public ConsultaUsgs buildConsulta(ConsultaInfo info, UsgsResponse respuesta) {
        List<Sismo> todos = ConvertidorConsulta.extractSismos(respuesta);

        Estadisticas estadisticas = ConvertidorConsulta.extractEstadisticas(context, todos);
        estadisticas.setTruncado(respuesta.getFeatures().size() >= LIMIT_API);

        todos.sort((a, b) -> Double.compare(b.getMag(), a.getMag()));
        List<Sismo> guardar = todos.size() > MAX_SISMOS
                ? new ArrayList<>(todos.subList(0, MAX_SISMOS)) : todos;
        estadisticas.setGuardados(guardar.size());

        ConsultaUsgs consulta = new ConsultaUsgs();
        consulta.setInfo(info);
        consulta.setEstadisticas(estadisticas);
        consulta.setSismos(guardar);
        return consulta;
    }
}
