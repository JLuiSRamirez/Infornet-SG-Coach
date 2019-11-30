package mx.infornet.sgcoach;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class EditarPerfilFragment extends Fragment {

    private View myView;

    private String nombre, biografia, horario, gimnasio;
    private TextView tv_nombre, tv_biografia, tv_horario, tv_gimnasio;
    private Button btn_edit;
    LayoutInflater layoutInflater;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_perfil, container, false);

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getActivity(), "coaches", null, 3);
        SQLiteDatabase db = conn.getWritableDatabase();

        btn_edit = myView.findViewById(R.id.btn_edit);

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment editar = new EditarPerfilFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, editar);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        try {
            String query = "SELECT * FROM coaches";

            Cursor cursor = db.rawQuery(query, null);

            for(cursor.moveToFirst(); !cursor.isAfterLast();cursor.moveToNext()) {
                nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                biografia = cursor.getString(cursor.getColumnIndex("biografia"));
                horario = cursor.getString(cursor.getColumnIndex("horarios"));
                gimnasio = cursor.getString(cursor.getColumnIndex("gimnasio"));
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        db.close();

        tv_nombre = myView.findViewById(R.id.nombre_coach);
        tv_biografia = myView.findViewById(R.id.biografia);
        tv_gimnasio = myView.findViewById(R.id.gimnasio);
        tv_horario = myView.findViewById(R.id.horario);

        tv_nombre.setText(nombre);
        tv_biografia.setText(biografia);
        tv_gimnasio.setText(gimnasio);
        tv_horario.setText(horario);

        return myView;
    }
}
