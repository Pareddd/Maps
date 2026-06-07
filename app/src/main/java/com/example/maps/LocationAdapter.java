package com.example.maps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.maps.model.LocationModel;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private final List<LocationModel> locationList;
    private final Context context;

    public LocationAdapter(List<LocationModel> locationList, Context context) {
        this.locationList = locationList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationModel location = locationList.get(position);

        holder.tvName.setText(location.getName());
        holder.tvAddress.setText(location.getAddress());

        // Aksi klik kita ubah menjadi memunculkan notifikasi Toast sederhana
        // agar tidak error mencari DetailActivity.
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Membuka rute ke: " + location.getName(), Toast.LENGTH_SHORT).show();
            // Nanti kita bisa mengatur agar klik ini memindahkan koordinat di MapFragment
        });
    }

    @Override
    public int getItemCount() {
        if (locationList == null) return 0;
        return locationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }
    }
}