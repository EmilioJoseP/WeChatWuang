package fempa.es.wechatwuang;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class ChatActivity extends AppCompatActivity {

    //Datos
    String clienteOServer;
    String ip;
    String nombre;
    int puerto = 1048;
    boolean mensaje0ConNombre = true;

    //Hilos
    inicioServerYEsperdaClienteThread HiloEspera;
    RecibirMensajeThread HiloEscucha;

    //Sockets
    Socket socket;
    ServerSocket serverSocket;
    boolean ConectionEstablished;

    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    ImageButton botonEnviar;
    LinearLayout layoutParaMensajes;
    LinearLayout.LayoutParams parametrosParaMensajesDerecha;
    LinearLayout.LayoutParams parametrosParaMensajesIzqui;
    LinearLayout.LayoutParams parametrosParaMensajeInicial;
    AlertDialog.Builder mensajeDesconexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_sin_listview);

        //Inicializo todos los componentes
        this.botonEnviar = findViewById(R.id.buttonEnviar);
        this.botonEnviar.setEnabled(false);
        layoutParaMensajes = findViewById(R.id.linearParañadir);

        this.parametrosParaMensajesDerecha = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.parametrosParaMensajesDerecha.setMargins(10, 10, 10, 10);
        this.parametrosParaMensajesDerecha.gravity = Gravity.END;

        this.parametrosParaMensajesIzqui = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.parametrosParaMensajesIzqui.setMargins(10, 10, 10, 10);
        this.parametrosParaMensajesIzqui.gravity = Gravity.START;

        this.parametrosParaMensajeInicial = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.parametrosParaMensajeInicial.setMargins(10, 10, 10, 10);
        this.parametrosParaMensajeInicial.gravity = Gravity.CENTER;

        mensajeDesconexion = new AlertDialog.Builder(this);
        mensajeDesconexion.setMessage("El otro usuario se ha desconectado.");
        mensajeDesconexion.setTitle("Desconectando...");
        mensajeDesconexion.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DesconectarSockets();
                Intent intent = new Intent(ChatActivity.this, ClienteServerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //Cambio del texto del menu para que el user sepa que esta esperando
        getSupportActionBar().setTitle("Esperando conexión...");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Consigo los datos que me pasan de la actividades de configuracion
        Intent intentDeDatos = getIntent();
        this.clienteOServer = intentDeDatos.getStringExtra("clienteOServer");
        this.nombre = intentDeDatos.getStringExtra("nombre");

        if (this.clienteOServer.equals("cliente")) {
            //La ip la pasamos junto con el puerto en un String
            String ipPuerto = intentDeDatos.getStringExtra("ipPuerto");
            String[] paraSplit = ipPuerto.split(":");
            this.ip = paraSplit[0];
            this.puerto = Integer.parseInt(paraSplit[1]);
        } else {
            this.puerto = intentDeDatos.getIntExtra("puerto", 1048);
        }

        if (this.clienteOServer.equals("cliente")) {
            iniciarCliente();
        } else {
            Log.d("loquesea", "Iniciando Server");
            iniciarServer();
        }
    }

    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case android.R.id.home:
                //Flecha que vuelva al Principal
                if (socket == null) {
                    runOnUiThread(new mensajeDesconexion());
                } else {
                    enviarMensaje(" : : : : : : : : : :salgo");
                }
                DesconectarSockets();
                Intent intent = new Intent(ChatActivity.this, ClienteServerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(menu);
    }

    // ----------- INICIO DE SERVIDOR ---------- //

    public void iniciarServer() {
        (HiloEspera = new inicioServerYEsperdaClienteThread()).start();
    }

    private class inicioServerYEsperdaClienteThread extends Thread {

        public void run() {
            try {
                //Abrimos el socket
                serverSocket = new ServerSocket(puerto);

                //Mensaje de la información del servidor
                MiTextView tv = new MiTextView(new ContextThemeWrapper(ChatActivity.this, R.style.TextInicial), null, 0);
                tv.setText(getIp() + ":" + puerto);
                layoutParaMensajes.addView(tv, parametrosParaMensajeInicial);

                //Creamos un socket que esta a la espera de una conexion de cliente
                socket = serverSocket.accept();

                //Una vez hay conexion con un cliente, creamos los streams de salida/entrada
                try {
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    //Envio del nombre
                    enviarMensaje(nombre);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ConectionEstablished = true;

                //Iniciamos el hilo para la escucha y procesado de mensajes
                (HiloEscucha = new RecibirMensajeThread()).start();

                HiloEspera = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ----------- ----------------- ---------- //

    // ----------- INICIO DE CLIENTE ---------- //

    public void iniciarCliente() {
        (new ClienteConectarAServerThread(this.ip)).start();
    }

    private class ClienteConectarAServerThread extends Thread {
        String mIp;

        public ClienteConectarAServerThread(String ip) {
            mIp = ip;
        }

        public void run() {
            try {
                //Creamos el socket
                socket = new Socket(mIp, puerto);

                try {
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    //Enviamos el nombre
                    enviarMensaje(nombre);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ConectionEstablished = true;

                //Iniciamos el hilo para la escucha y procesado de mensajes
                (HiloEscucha = new RecibirMensajeThread()).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ----------- ----------------- ---------- //

    // ----------- ENVIO DE MENSAJES ---------- //

    public void onClickEnviarMensaje(View v) {
        EditText et = (EditText) findViewById(R.id.editTextMensaje);
        String mensajeParaEnviar = et.getText().toString();

        //Si no hay nada escrito no se envia nada
        if (mensajeParaEnviar.length() > 0) {
            MiTextView tv = new MiTextView(new ContextThemeWrapper(this, R.style.TextDerecha), null, 0);

            tv.setText(et.getText().toString());
            layoutParaMensajes.addView(tv, this.parametrosParaMensajesDerecha);
            enviarMensaje(mensajeParaEnviar);
            et.setText("");
        }

    }

    private void enviarMensaje(String txt) {
        //Diferencio para poder hacer un join del del Thread y que le de tiempo a enviar el mensaje de desconexion.
        if (txt.equals(" : : : : : : : : : :salgo")) {
            EnviarMensajeThread emt = new EnviarMensajeThread(txt);
            emt.start();
            try {
                emt.join();
            } catch (Exception e) {

            }
        } else {
            new EnviarMensajeThread(txt).start();
        }
    }

    private class EnviarMensajeThread extends Thread {
        private String msg;

        public EnviarMensajeThread(String message) {
            msg = message;
        }

        @Override
        public void run() {
            try {
                dataOutputStream.writeUTF(msg);//Enviamos el mensaje
            } catch (IOException e) {
                e.printStackTrace();
                //Si dataOutputStream == null quiere decir que el socket ya no existe y lanzo el mensaje.
                if (serverSocket == null) {
                    runOnUiThread(new mensajeDesconexion());
                }
            }
        }
    }

    // ----------- RECIBO DE MENSAJES ---------- //

    public void mensajeRecibido(String e) {
        //El mensaje de desconxion es " : : : : : : : : : :salgo" por eso el split
        String[] lo = e.split(":");
        if (mensaje0ConNombre) {
            //solo entrará aqui la primera vez
            cambiarMenu(e);
            mensaje0ConNombre = false;
        } else if (lo.length == 11 && lo[lo.length -1].equals("salgo") ) {
            //Si entra quiere decir que se ha enviado el mensaje de desconexion
            runOnUiThread(new mensajeDesconexion());
        } else {
            //Si va bien se actualiza la interfaz con el mensaje
            runOnUiThread(new actualizarVistaUI(e));
        }
    }

    private class RecibirMensajeThread extends Thread {

        public boolean executing;
        private String line;

        public void run() {
            executing = true;
            while (executing) {
                line = "";
                line = ObtenerCadena();//Obtenemos la cadena del buffer
                if (line != "" && line.length() != 0) {
                    mensajeRecibido(line);
                }
            }
        }

        public void setExecuting(boolean execute) {
            executing = execute;
        }

        private String ObtenerCadena() {
            String cadena = "";

            try {
                cadena = dataInputStream.readUTF(); //Leemos del datainputStream una cadena UTF
                Log.d("loquesea", "Cadena reibida: " + cadena);
            } catch (Exception e) {
                e.printStackTrace();
                executing = false;
            }
            return cadena;
        }
    }

    // ----------- ----------------- ---------- //

    // ----------- ACTUALIZAR INTERFAZ ---------- //

    protected class actualizarVistaUI implements Runnable {
        //Se encarga de poner los texview personalizados
        private String text;

        public actualizarVistaUI(String text) {
            this.text = text;
        }

        public void run() {
            MiTextView tv = new MiTextView(new ContextThemeWrapper(ChatActivity.this, R.style.TextIzquierda), null, 0);

            tv.setText(this.text);
            layoutParaMensajes.addView(tv, parametrosParaMensajesIzqui);
        }
    }

    protected class mensajeDesconexion implements Runnable {
        //Solo muestra el mensaje de desconexion
        public mensajeDesconexion() {
        }

        public void run() {
            mensajeDesconexion.show();
        }
    }

    protected class cambiarMenuUI implements Runnable {
        //Solo cambia el titulo del menu con el nombre de los usuarios
        private String text;
        private ActionBar e;

        public cambiarMenuUI(String text, ActionBar e) {
            this.text = text;
            this.e = e;
        }

        public void run() {
            this.e.setTitle(this.text);
            botonEnviar.setEnabled(true);
        }
    }

    public void cambiarMenu(String e) {
        runOnUiThread(new cambiarMenuUI(e, getSupportActionBar()));
    }

    // ----------- ----------------- ---------- //

    private void DesconectarSockets() {
        if (ConectionEstablished) {
            ConectionEstablished = false;

            if (HiloEscucha != null) {
                HiloEscucha.setExecuting(false);
                HiloEscucha.interrupt();
                HiloEspera = null;
            }

            try {
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
            } catch (Exception e) {
            } finally {
                dataInputStream = null;
                try {
                    if (dataOutputStream != null)
                        dataOutputStream.close();
                } catch (Exception e) {
                } finally {
                    dataOutputStream = null;
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (Exception e) {
                    } finally {
                        socket = null;
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            }catch (Exception e) {

                            } finally {
                                serverSocket = null;
                            }

                        }
                    }
                }
            }
        }
    }

    private String getIp() {

        String ip = "";

        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "IP de Servidor: " + inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "¡Algo fue mal! " + e.toString();
        }

        return ip;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DesconectarSockets();
    }
}
