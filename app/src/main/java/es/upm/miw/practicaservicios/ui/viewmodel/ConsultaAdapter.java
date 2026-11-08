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
import java.util.List;
import java.util.Locale;

import es.upm.miw.practicaservicios.R;
import es.upm.miw.practicaservicios.data.ConsultaInfo;
import es.upm.miw.practicaservicios.data.ConsultaUsgs;
import es.upm.miw.practicaservicios.data.Estadisticas;

public class ConsultaAdapter extends RecyclerView.Adapter<ConsultaAdapter.ConsultaViewHolder> {

    private final LayoutInflater inflater;
    private List<ConsultaUsgs> consultas = new ArrayList<>();

    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    static class ConsultaViewHolder extends RecyclerView.ViewHolder {
        final TextView tvLugar, tvNumero, tvMagMax, tvFecha;

        ConsultaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLugar = itemView.findViewById(R.id.tvQueryLocalization);
            tvNumero = itemView.findViewById(R.id.tvEarthquakeNumber);
            tvMagMax = itemView.findViewById(R.id.tvMaxMagnitude);
            tvFecha = itemView.findViewById(R.id.tvQueryDate);
        }
    }

    public ConsultaAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ConsultaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.recyclerview_item_query_history, parent, false);
        return new ConsultaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsultaViewHolder holder, int position) {
        ConsultaUsgs c = consultas.get(position);
        ConsultaInfo info = c.getInfo();
        Estadisticas est = c.getEstadisticas();

        holder.tvLugar.setText(info != null && info.getZonaNombre() != null
                ? info.getZonaNombre()
                : "-");
        holder.tvNumero.setText(est != null
                ? String.valueOf(est.getNumTotalSismos())
                : "-");
        holder.tvMagMax.setText(est != null && est.getMagMax() != null
                ? String.format(Locale.getDefault(), "%.1f", est.getMagMax())
                : "-");
        holder.tvFecha.setText(info != null && info.getFechaConsulta() != null
                ? FMT.format(info.getFechaConsulta())
                : "-");
    }

    public void setConsultas(List<ConsultaUsgs> consultas) {
        this.consultas = consultas;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (consultas.isEmpty())
                ? 0
                : consultas.size();
    }
}
