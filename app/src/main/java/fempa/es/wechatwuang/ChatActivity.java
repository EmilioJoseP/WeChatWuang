package fempa.es.wechatwuang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    public String ip;
    String nombre;
    public int puerto = 1048;
    boolean mensaje0ConNombre = true;

    //Hilos
    EsperaClienteThread HiloEspera;
    RecibirMensajeThread HiloEscucha;

    //Sockets
    Socket socket;
    ServerSocket serverSocket;
    boolean ConectionEstablished;

    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    Button botonEnviar;
    LinearLayout layoutParaMensajes;
    LinearLayout.LayoutParams parametrosParaMensajesDerecha;
    LinearLayout.LayoutParams parametrosParaMensajesIzqui;
    LinearLayout.LayoutParams parametrosParaMensajeInicial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_sin_listview);

        this.botonEnviar = findViewById(R.id.buttonEnviar);
        this.botonEnviar.setEnabled(false);

        this.parametrosParaMensajesDerecha = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.parametrosParaMensajesDerecha.setMargins(10, 10, 10, 10);
        this.parametrosParaMensajesDerecha.gravity = Gravity.END;

        this.parametrosParaMensajesIzqui = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.parametrosParaMensajesIzqui.setMargins(10, 10, 10, 10);
        this.parametrosParaMensajesIzqui.gravity = Gravity.START;

        this.parametrosParaMensajeInicial = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.parametrosParaMensajeInicial.setMargins(10, 10, 10, 10);
        this.parametrosParaMensajeInicial.gravity = Gravity.CENTER;

        Intent intentDeDatos = getIntent();
        this.clienteOServer = intentDeDatos.getStringExtra("clienteOServer");
        this.nombre = intentDeDatos.getStringExtra("nombre");
        String ipPuerto = intentDeDatos.getStringExtra("ipPuerto");

        if (this.clienteOServer.equals("cliente")) {
            String[] paraSplit = ipPuerto.split(":");
            this.ip = paraSplit[0];
            this.puerto = Integer.parseInt(paraSplit[1]);
        } else {
            this.puerto = intentDeDatos.getIntExtra("puerto", 1048);
        }


        layoutParaMensajes = findViewById(R.id.linearParañadir);

        if (this.clienteOServer.equals("cliente")) {
            iniciarCliente();
        } else {
            Log.d("loquesea", "Iniciando Server");
            iniciarServer();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    //Flecha que vuelva al Principal
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ChatActivity.this, ClienteServerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(menu);
    }

    protected class actualizarListaThread implements Runnable {
        private String text;

        public actualizarListaThread(String text) {
            this.text = text;
        }

        public void run() {
            MiTextView tv = new MiTextView(new ContextThemeWrapper(ChatActivity.this, R.style.TextIzquierda), null, 0);

            tv.setText(this.text);
            layoutParaMensajes.addView(tv, parametrosParaMensajesIzqui);
        }
    }

    protected class cambiarMenuUI implements Runnable {
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

    public void mensajeRecibido(String e) {
        if (mensaje0ConNombre) {
            cambiarMenu(e);
            mensaje0ConNombre = false;
        } else {
            runOnUiThread(new actualizarListaThread(e));
        }
    }

    public void onClickEnviarMensaje(View v) {
        EditText et = (EditText) findViewById(R.id.editTextMensaje);

        MiTextView tv = new MiTextView(new ContextThemeWrapper(this, R.style.TextDerecha), null, 0);

        tv.setText(et.getText().toString());
        layoutParaMensajes.addView(tv, this.parametrosParaMensajesDerecha);
        enviarMensaje(et.getText().toString());
        et.setText("");
    }

    private void enviarMensaje(String txt) {
        new EnviarMensajeThread(txt).start();
    }

    public void iniciarServer() {
        (HiloEspera = new EsperaClienteThread()).start();
    }

    public void iniciarCliente() {
        (new ClienteConectarAServerThread(this.ip)).start();
    }

    private class EsperaClienteThread extends Thread {
        public void run() {
            try {
                Log.d("loquesea", "Server: puerto " + puerto);
                //Abrimos el socket
                serverSocket = new ServerSocket(puerto);


                MiTextView tv = new MiTextView(new ContextThemeWrapper(ChatActivity.this, R.style.TextInicial), null, 0);
                tv.setText(getIp() + ":" + puerto);
                layoutParaMensajes.addView(tv, parametrosParaMensajeInicial);


                //Mostramos un mensaje para indicar que estamos esperando en la direccion ip y el puerto...
                //AppenText("Creado el servidor\n Dirección: "+getIpAddress()+" Puerto: "+serverSocket.getLocalPort());

                //Creamos un socket que esta a la espera de una conexion de cliente
                socket = serverSocket.accept();
                Log.d("loquesea", "Server: usuario aceptado");

                //Una vez hay conexion con un cliente, creamos los streams de salida/entrada
                try {
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    enviarMensaje(nombre);
                    Log.d("loquesea", "Server: Datas okay");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ConectionEstablished = true;

                //Iniciamos el hilo para la escucha y procesado de mensajes
                (HiloEscucha = new RecibirMensajeThread()).start();

                HiloEspera = null;
            } catch (IOException e) {
                Log.d("loquesea", "Server: Petada de socket");
                e.printStackTrace();
            }
        }
    }

    private class RecibirMensajeThread extends Thread {

        public boolean executing;
        private String line;

        public void run() {
            executing = true;
            Log.d("loquesea", "Server: Esperando");
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


    private class ClienteConectarAServerThread extends Thread {
        String mIp;

        public ClienteConectarAServerThread(String ip) {
            mIp = ip;
        }

        public void run() {
            try {
                socket = new Socket(mIp, puerto);//Creamos el socket

                try {
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    enviarMensaje(nombre);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ConectionEstablished = true;

                //Iniciamos el hilo para la escucha y procesado de mensajes
                (HiloEscucha = new RecibirMensajeThread()).start();

            } catch (Exception e) {
                Log.d("loquesea", "HA PETADO EL SOCKETTTTTTTT");
                e.printStackTrace();
            }
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
            }
        }
    }

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
                    }
                }
            }
        }
    }

    //Aqui obtenemos la IP de nuestro terminal
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
