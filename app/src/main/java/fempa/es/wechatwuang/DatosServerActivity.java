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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    public void onClickAceptar(View v){

        EditText editTextNombre = (EditText) findViewById(R.id.editTextNombre);
        EditText editTextIp = (EditText) findViewById(R.id.editTextIp);
        EditText editTextPuerto = (EditText) findViewById(R.id.editTextPuerto);


        String textNombre = editTextNombre.getText().toString();
        String textNombreDatos = "Servidor";
        String textIp = editTextIp.getText().toString();
        String textPuerto = editTextPuerto.getText().toString();
        String textIpPuerto = textIp + ":" + textPuerto;

        Intent intent = new Intent(DatosServerActivity.this, ChatActivity.class);
        intent.putExtra("clienteOServer", textNombreDatos);
        intent.putExtra("nombre", textNombre);
        intent.putExtra("ipPuerto", textIpPuerto);

    }
}
