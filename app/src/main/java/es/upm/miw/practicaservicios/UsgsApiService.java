package es.upm.miw.practicaservicios;

import es.upm.miw.practicaservicios.models.UsgsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UsgsApiService {

    @GET("fdsnws/event/1/query")
    Call<UsgsResponse> getEarthquakes(
            @Query("format") String format,
            @Query("starttime") String start,
            @Query("endtime") String end,
            @Query("minmagnitude") double minMag,
            @Query("latitude") double lat,
            @Query("longitude") double lon,
            @Query("maxradiuskm") double radiusKm,
            @Query("orderby") String orderBy,
            @Query("limit") int limit);
}
