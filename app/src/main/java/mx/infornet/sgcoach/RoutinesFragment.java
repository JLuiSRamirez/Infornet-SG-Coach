package mx.infornet.sgcoach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class RoutinesFragment extends Fragment {
    private View myView;

    private ProgressBar progressBar;

    private Button btn_agregar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_routines, container, false);

        myView = inflater.inflate(R.layout.fragment_routines, container, false);

        progressBar = myView.findViewById(R.id.progressbar_rutinas);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        btn_agregar = myView.findViewById(R.id.btn_agregar_nueva_rutina);

        progressBar.setVisibility(View.GONE);

        btn_agregar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Fragment agregar = new AgregarRutinaFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, agregar);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return myView;

    }
}
