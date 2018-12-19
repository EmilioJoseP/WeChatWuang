package fempa.es.wechatwuang;

import android.content.Intent;
import android.graphics.Typeface;
import android.icu.text.MeasureFormat;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
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

    //ListView lista;
   // ArrayList<String> mensajes;
   // ArrayAdapter<String> arrayAdapter;
   // Adapter miAdapatador;
    Button botonEnviar;
    LinearLayout layoutParaMensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_sin_listview);

        botonEnviar = findViewById(R.id.buttonEnviar);
        botonEnviar.setEnabled(false);

        Intent intentDeDatos = getIntent();
        this.clienteOServer = intentDeDatos.getStringExtra("clienteOServer");
        this.nombre = intentDeDatos.getStringExtra("nombre");
        String ipPuerto = intentDeDatos.getStringExtra("ipPuerto");
        if (this.clienteOServer.equals("cliente")) {
            String[] paraSplit = ipPuerto.split(":");
            this.ip = paraSplit[0];
            this.puerto = Integer.parseInt(paraSplit[1]);
            //Toast.makeText(this,  this.ip, Toast.LENGTH_SHORT).show();
            //Toast.makeText(this,  this.puerto + "", Toast.LENGTH_SHORT).show();
        } else {
            this.puerto = intentDeDatos.getIntExtra("puerto", 1048);
        }

       // this.mensajes = new ArrayList<>();
        //this.mensajes.add(getIp());
        //this.lista = findViewById(R.id.lista);

        //this.arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, this.mensajes);

        layoutParaMensajes = findViewById(R.id.linearParañadir);
       // TextView tv = new TextView(this);
       // tv.setText("efñwefñoe");
        if (this.clienteOServer.equals("cliente")) {
        //    this.miAdapatador = new Adapter(this, R.layout.mensaje_recibido, this.mensajes, "cliente");
            //linear.addView(tv);
            iniciarCliente();
        } else {
            Log.d("loquesea", "Iniciando Server");
        //    this.miAdapatador = new Adapter(this, R.layout.mensaje_recibido, this.mensajes, "server");
            //linear.addView(tv);
            iniciarServer();
        }



     //   this.lista.setAdapter(this.miAdapatador);
    }

    protected class actualizarListaThread implements Runnable {
        private String text;
        private String quien;

        public actualizarListaThread(String text, String quien) {
            this.text = text;
            this.quien = quien;
        }

        public void run() {
       //     miAdapatador.notifyDataSetChanged();
            MiTextView tv = null;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.START;
            params.setMargins(10, 10, 10, 10);

            tv = new MiTextView(new ContextThemeWrapper(ChatActivity.this, R.style.TextIzquierda), null, 0);

            if (tv != null) {
                tv.setText(this.text);
                layoutParaMensajes.addView(tv, params);
            }
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
            if (this.clienteOServer.equals("cliente")) {
                //this.mensajes.add(e + ":s");
                runOnUiThread(new actualizarListaThread(e, ":s"));
            } else {
                //this.mensajes.add(e + ":c");
                runOnUiThread(new actualizarListaThread(e, ":c"));
            }

        }
    }

    public void onClickEnviarMensaje(View v) {
        EditText et = (EditText) findViewById(R.id.editTextMensaje);

        MiTextView tv = null;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        params.setMargins(10, 10, 10, 10);

        if (clienteOServer.equals("server")) {
            tv = new MiTextView(new ContextThemeWrapper(this, R.style.TextDerecha), null, 0);
        }

        if (clienteOServer.equals("cliente")) {
            tv = new MiTextView(new ContextThemeWrapper(this, R.style.TextDerecha), null, 0);
        }

        tv.setText(et.getText().toString());
        layoutParaMensajes.addView(tv, params);


        enviarMensaje(et.getText().toString());
       // this.miAdapatador.notifyDataSetChanged();
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
                if (line != "" && line.length() != 0) {//Comprobamos que esa cadena tenga contenido
                    //AppenText("Recibido: "+line + "\n");//Procesamos la cadena recibida
                    //Aqui se escribe
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
                        ip += "IP de Servidor: " + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "¡Algo fue mal! " + e.toString() + "\n";
        }

        return ip;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DesconectarSockets();
    }
}
