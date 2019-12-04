package mx.infornet.sgcoach;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterAlimentacion extends RecyclerView.Adapter<AdapterAlimentacion.AlimentacionViewHolder> {

    private Context mCtx;
    private List<Alimentacion> alimentacionList;

    public AdapterAlimentacion(Context mCtx, List<Alimentacion> alimentacionList) {
        this.mCtx = mCtx;
        this.alimentacionList = alimentacionList;
    }

    @NonNull
    @Override
    public AlimentacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_alimentacion_layout, parent, false);

        return new AdapterAlimentacion.AlimentacionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AdapterAlimentacion.AlimentacionViewHolder holder, int position) {
        Alimentacion alimentacion = alimentacionList.get(position);

        holder.nombre_alim.setText(alimentacion.getNombre());
    }

    @Override
    public int getItemCount() {
        if (alimentacionList != null)
            return alimentacionList.size();
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return alimentacionList.get(position).getId();
    }

    class AlimentacionViewHolder extends RecyclerView.ViewHolder{

        TextView nombre_alim;

        public AlimentacionViewHolder(@NonNull View itemView) {
            super(itemView);

            nombre_alim = itemView.findViewById(R.id.nombre_alim);

            CardView cardView = itemView.findViewById(R.id.card_view_item);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int requesCode = getAdapterPosition();

                    int idAlimentacion = alimentacionList.get(requesCode).getId();
                    String nombreAlimentacion = alimentacionList.get(requesCode).getNombre();
                    String descrAlimentacion = alimentacionList.get(requesCode).getDescripcion();
                    String categoriaAlimentacion = alimentacionList.get(requesCode).getCategoria();

                    //acá se lanza a un nuevo fragment

                    Intent inAlim = new Intent(mCtx, ShowAlimentacionActivity.class);
                    inAlim.putExtra("id", idAlimentacion);
                    inAlim.putExtra("nombre", nombreAlimentacion);
                    inAlim.putExtra("descripcion", descrAlimentacion);
                    inAlim.putExtra("categoria", categoriaAlimentacion);

                    mCtx.startActivity(inAlim);

                }
            });

        }
    }
}
