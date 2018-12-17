package fempa.es.wechatwuang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import javax.xml.datatype.Duration;

public class DatosClienteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_cliente);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //Flecha que vuelva al Principal
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(DatosClienteActivity.this, ClienteServerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

        }

        return super.onOptionsItemSelected(menu);
    }

    //Boton Aceptar
    //Al Aceptar si todos los datos esta bien se pondra el Activity Chat
    public void onClickAceptar(View v){
        boolean bool = true;
        EditText editTextNombre = (EditText) findViewById(R.id.editTextNombre);
        EditText editTextIp = (EditText) findViewById(R.id.editTextIp);
        EditText editTextPuerto = (EditText) findViewById(R.id.editTextPuerto);


        String textNombre = editTextNombre.getText().toString();
        String textNombreDatos = "Cliente";
        String textIp = editTextIp.getText().toString();
        String textPuerto = editTextPuerto.getText().toString();
        String textIpPuerto = textIp + ":" + textPuerto;
        int numPuerto = 0;
        int numIp0 = 0;
        int numIp1 = 0;
        int numIp2 = 0;
        int numIp3 = 0;
        String numIp[] = textIp.split("\\.");

        //Comprobamos si nos pasa numero o texto en Puerto
        try {
            numPuerto = Integer.parseInt(textPuerto);
        } catch (Exception e) {

            Toast.makeText(this, "Error en el puerto", Toast.LENGTH_LONG).show();
        }

        //Comprobamos si nos pasa numero o texto en Ip
        try {
            numIp0 = Integer.parseInt(numIp[0]);
            numIp1 = Integer.parseInt(numIp[1]);
            numIp2 = Integer.parseInt(numIp[2]);
            numIp3 = Integer.parseInt(numIp[3]);
        } catch (Exception e) {

            Toast.makeText(this, "Error en la ip", Toast.LENGTH_LONG).show();
        }

        //Comprobamos si los compas estan rellenos
        if(textNombre.equals("") || textNombreDatos.equals("") || textIp.equals("") || textIpPuerto.equals("")){

            Toast.makeText(this, "Error, rellene todod los campos", Toast.LENGTH_LONG).show();
            bool = false;
        }

        //Comprobamos si el puerto no es menos que 1024 y tiene menos de 4 o 5 de longitud
        if ((numPuerto <= 1024 && (textIp.length() == 4 || textIp.length() == 5))) {
            Toast.makeText(this, "Introduzca un puerto correcto", Toast.LENGTH_LONG).show();
            bool = false;
        }

        //Comprobamos la posicion 0 de la Ip
        if((numIp0 < 1 || numIp0 >255) && (numIp1 < 1 || numIp1 >255) && (numIp2 < 1 || numIp2 >255) && (numIp3 < 1 || numIp3 >255)){
            Toast.makeText(this, numIp0  +"" + numIp1+ "" +numIp2 +""+ numIp3, Toast.LENGTH_LONG).show();
            bool = false;
        }

        //Si bool es true, hacemos el intent
        if (bool) {
            Intent intent = new Intent(DatosClienteActivity.this, ChatActivity.class);
            intent.putExtra("clienteOServer", textNombreDatos);
            intent.putExtra("nombre", textNombre);
            intent.putExtra("ipPuerto", textIpPuerto);

            Toast.makeText(this, "Todo ok", Toast.LENGTH_LONG).show();
        }
    }
}
