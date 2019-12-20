package mx.infornet.sgcoach;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterPlanes extends RecyclerView.Adapter<AdapterPlanes.PlanesViewHolder> {

    private Context mCtx;
    private List<Planes> planesList;

    public AdapterPlanes(Context mCtx, List<Planes> planesList) {
        this.mCtx = mCtx;
        this.planesList = planesList;
    }

    @NonNull
    @Override
    public PlanesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_planes_layout, parent, false);

        return new AdapterPlanes.PlanesViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AdapterPlanes.PlanesViewHolder holder, int position) {
        Planes planes = planesList.get(position);

        holder.nombre_plan.setText(planes.getNombre());
    }

    @Override
    public int getItemCount() {
        if (planesList != null)
            return planesList.size();
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return planesList.get(position).getId();
    }

    class PlanesViewHolder extends RecyclerView.ViewHolder{

        TextView nombre_plan;

        public PlanesViewHolder(@NonNull View itemView) {
            super(itemView);

            nombre_plan = itemView.findViewById(R.id.nombre_plan);

            CardView cardView = itemView.findViewById(R.id.card_view_item_plan);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int requesCode = getAdapterPosition();

                    int idPlan =planesList.get(requesCode).getId();
                    String nombrePlan = planesList.get(requesCode).getNombre();
                    Double precioPlan = planesList.get(requesCode).getPrecio();
                    String serviciosPlan = planesList.get(requesCode).getServicios();

                    //acá se lanza a un nuevo fragment

                    Intent inPlan = new Intent(mCtx, ShowPlanesActivity.class);
                    inPlan.putExtra("id", idPlan);
                    inPlan.putExtra("nombre", nombrePlan);
                    inPlan.putExtra("precio", precioPlan);
                    inPlan.putExtra("servicios", serviciosPlan);

                    mCtx.startActivity(inPlan);

                }
            });

        }
    }

}
