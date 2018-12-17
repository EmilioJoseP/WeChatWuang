package fempa.es.wechatwuang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DatosServerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_datos_server);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    //Flecha que vuelva al Principal
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(DatosServerActivity.this, ClienteServerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

        }

        return super.onOptionsItemSelected(menu);
    }

    //Boton Aceptar
    //Al Aceptar si todos los datos esta bien se pondra el Activity Chat
    public void onClickAceptarServer(View v){

        EditText editTextNombre = (EditText) findViewById(R.id.editTextNombre);
        EditText editTextPuerto = (EditText) findViewById(R.id.editTextPuerto);

        String textNombre = editTextNombre.getText().toString();
        String textNombreDatos = "server";
        String textPuerto = editTextPuerto.getText().toString();
        int numPuerto = 1048;
        boolean bool = true;

        //Comprobamos si nos pasa numero o texto en Puerto
        try {
            numPuerto = Integer.parseInt(textPuerto);

            //Comprobamos si el puerto no es menos que 1024 y tiene menos de 4 o 5 de longitud
            if ((numPuerto <= 1024 && (textPuerto.length() == 4 || textPuerto.length() == 5))) {
                Toast.makeText(this, "Introduzca un puerto correcto", Toast.LENGTH_LONG).show();
                bool = false;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error en el puerto", Toast.LENGTH_LONG).show();
            bool = false;
        }

        if(textNombre.equals("")){
            Toast.makeText(this, "Error, rellena el nombre", Toast.LENGTH_LONG).show();
            bool = false;
        }

        if (bool) {
            Intent intent = new Intent(DatosServerActivity.this, ChatActivity.class);
            intent.putExtra("clienteOServer", textNombreDatos);
            intent.putExtra("nombre", textNombre);
            intent.putExtra("puerto", numPuerto);
            startActivity(intent);
        }
    }
}
