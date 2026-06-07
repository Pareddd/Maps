package com.example.maps;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
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

        String name = location.getName();
        String address = location.getAddress();

        if (name == null || name.trim().isEmpty()) {
            name = "Agen Ekspedisi / Mitra Kurir";
        }

        if (address == null || address.trim().isEmpty()) {
            address = "Melayani jasa pengiriman barang dan logistik area Makassar sekitarnya.";
        } else {
            String[] addressParts = address.split(",");
            if (addressParts.length >= 2) {
                address = addressParts[0].trim() + ", " + addressParts[1].trim();
            }
        }

        holder.tvName.setText(name);
        holder.tvAddress.setText(address);

        final String finalName = name;
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putDouble("lat", location.getLatitude());
            bundle.putDouble("lng", location.getLongitude());
            bundle.putString("name", finalName);

            try {
                Navigation.findNavController(v).navigate(R.id.mapFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(context, "Navigasi ke peta belum siap", Toast.LENGTH_SHORT).show();
            }
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