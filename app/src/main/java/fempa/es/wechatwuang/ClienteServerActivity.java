package fempa.es.wechatwuang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ClienteServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_server);

    }

    //Boton Acerca De...
    public void onClickAcercaDe(View v){
        Intent intent = new Intent(ClienteServerActivity.this, AcercaDeActivity.class);
        startActivity(intent);
    }

    //Boton Cliente
    public void onClickCliente(View v){
        Intent intent = new Intent(ClienteServerActivity.this, DatosClienteActivity.class);
        startActivity(intent);
    }

    //Boton Servidor
    public void onClickServidor(View v){
        Intent intent = new Intent(ClienteServerActivity.this, DatosServerActivity.class);
        startActivity(intent);
    }

}
