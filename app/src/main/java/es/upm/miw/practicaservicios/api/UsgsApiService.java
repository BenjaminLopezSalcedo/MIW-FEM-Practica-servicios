package es.upm.miw.practicaservicios.api;

import es.upm.miw.practicaservicios.api.models.UsgsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UsgsApiService {

    @GET("fdsnws/event/1/query")
    Call<UsgsResponse> getEarthquakes(
            @Query("format") String format,
            @Query("starttime") String start,
            @Query("endtime") String end,
            @Query("minmagnitude") Double minMag,    // null = sin filtro
            @Query("latitude") Double lat,           // null = sin zona
            @Query("longitude") Double lon,          // null = sin zona
            @Query("maxradiuskm") Double radiusKm,   // null = sin zona
            @Query("orderby") String orderBy,
            @Query("limit") int limit);
}
