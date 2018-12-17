package fempa.es.wechatwuang;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class Adapter extends ArrayAdapter<String> {
    Context context;
    int layout;
    ArrayList<String> listaMensajes;
    String soy;

    public Adapter(Context context, int resource, ArrayList<String> objects, String soy) {
        super(context, resource, objects);
        this.context = context;
        this.layout = resource;
        this.listaMensajes = objects;
        this.soy = soy;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflador = LayoutInflater.from(this.context);
        View vistaADevolver = inflador.inflate(this.layout, null);

        //Para rellenar los datos
        TextView mensaje = vistaADevolver.findViewById(R.id.textViewMensaje);

        String[] e = this.listaMensajes.get(position).split(":");

        if (this.soy.equals("server") && e[e.length - 1].equals("s")) {
            mensaje.setGravity(Gravity.END);
        } else if (this.soy.equals("server") && e[e.length - 1].equals("c")) {
            mensaje.setGravity(Gravity.START);
        }

        if (this.soy.equals("cliente") && e[e.length - 1].equals("c")) {
            mensaje.setGravity(Gravity.END);
        } else if (this.soy.equals("cliente") && e[e.length - 1].equals("s")) {
            mensaje.setGravity(Gravity.START);
        }

        String mensajeParaImprimir = "";

        for (int i = 0; i < e.length; i++) {
            if (i != (e.length - 1)) {
                if (i == (e.length - 2)) {
                    mensajeParaImprimir += e[i];
                } else {
                    mensajeParaImprimir += e[i] + ":";
                }
            }
        }
        //Los relleno...
        mensaje.setText(mensajeParaImprimir);

        return vistaADevolver;
    }
}

