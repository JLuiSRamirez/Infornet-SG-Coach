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

public class AdapterRutinas extends RecyclerView.Adapter<AdapterRutinas.RutinasViewHolder> {

    private Context mCtx;
    private List<Rutinas> rutinasList;
    private int tipo;

    public AdapterRutinas(Context mCtx, List<Rutinas> rutinasList, int tipo) {
        this.mCtx = mCtx;
        this.rutinasList = rutinasList;
        this.tipo = tipo;
    }

    @NonNull
    @Override
    public RutinasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_rutinas_layout, parent, false);

        return new AdapterRutinas.RutinasViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AdapterRutinas.RutinasViewHolder holder, int position) {
        Rutinas rutinas = rutinasList.get(position);

        holder.nombre_rutina.setText(rutinas.getNombre());
    }

    @Override
    public int getItemCount() {
        if (rutinasList != null)
            return rutinasList.size();
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return rutinasList.get(position).getId();
    }

    class RutinasViewHolder extends RecyclerView.ViewHolder{

        TextView nombre_rutina;

        public RutinasViewHolder(@NonNull View itemView) {
            super(itemView);

            nombre_rutina = itemView.findViewById(R.id.nombre_rutina);

            CardView cardView = itemView.findViewById(R.id.card_view_item_rutina);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int requesCode = getAdapterPosition();

                    int idRutina =rutinasList.get(requesCode).getId();
                    String nombreRutina = rutinasList.get(requesCode).getNombre();
                    String descrRutina = rutinasList.get(requesCode).getDescripcion();

                    //acá se lanza a un nuevo fragment

                    if (tipo == 1) {//RUTINAS COACH

                        Intent inRutina = new Intent(mCtx, ShowMisRutinasActivity.class);
                        inRutina.putExtra("id", idRutina);
                        inRutina.putExtra("nombre", nombreRutina);
                        inRutina.putExtra("descripcion", descrRutina);
                        mCtx.startActivity(inRutina);
                    } else {
                        Intent inRutina = new Intent(mCtx, ShowRutinasActivity.class);
                        inRutina.putExtra("id", idRutina);
                        inRutina.putExtra("nombre", nombreRutina);
                        inRutina.putExtra("descripcion", descrRutina);
                        mCtx.startActivity(inRutina);
                    }


                }
            });

        }
    }

}
