package es.upm.miw.practicaservicios.ui.viewmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.upm.miw.practicaservicios.R;
import es.upm.miw.practicaservicios.data.Sismo;

public class EarthquakeAdapter extends RecyclerView.Adapter<EarthquakeAdapter.EarthquakeViewHolder> {

    private final LayoutInflater inflater;
    private List<Sismo> sismos = new ArrayList<>();

    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    static class EarthquakeViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMag, tvPlace, tvDepth, tvTime;

        EarthquakeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMag = itemView.findViewById(R.id.tvEarthquakeMag);
            tvPlace = itemView.findViewById(R.id.tvEarthquakePlace);
            tvDepth = itemView.findViewById(R.id.tvEarthquakeDepth);
            tvTime = itemView.findViewById(R.id.tvEarthquakeTime);
        }
    }

    public EarthquakeAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public EarthquakeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.recyclerview_item_earthquake, parent, false);
        return new EarthquakeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EarthquakeViewHolder holder, int position) {
        Sismo s = sismos.get(position);
        holder.tvMag.setText(String.format(Locale.getDefault(), "%.1f", s.getMag()));
        holder.tvPlace.setText(s.getLugar() != null
                ? s.getLugar()
                : "-");
        holder.tvDepth.setText(String.format(Locale.getDefault(), "%.0f km", s.getProf()));
        holder.tvTime.setText(FMT.format(new Date(s.getTiempo())));
    }

    public void setSismos(List<Sismo> sismos) {
        this.sismos = sismos;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (sismos.isEmpty())
                ? 0
                : sismos.size();
    }
}
