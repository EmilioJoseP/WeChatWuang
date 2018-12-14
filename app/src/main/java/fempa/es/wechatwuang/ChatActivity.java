package fempa.es.wechatwuang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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
    String nombre;
    public String ip;
    public int puerto;

    //Hilos
    EsperaClienteThread HiloEspera;
    RecibirMensajeThread HiloEscucha;

    //Sockets
    Socket socket;
    ServerSocket serverSocket;
    boolean ConectionEstablished;

    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    ListView lista;
    ArrayList<String> mensajes;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intentDeDatos = getIntent();
        this.clienteOServer = intentDeDatos.getStringExtra("clienteOServer");
        this.nombre = intentDeDatos.getStringExtra("nombre");
        String ipPuerto = intentDeDatos.getStringExtra("ipPuerto");
        this.ip = (ipPuerto.split(":"))[0];
        this.puerto = Integer.parseInt((ipPuerto.split(":"))[1]);

        this.mensajes = new ArrayList<>();
        this.mensajes.add("okokoko");
        this.lista = findViewById(R.id.lista);
        this.arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, this.mensajes);
        this.lista.setAdapter(arrayAdapter);

        if (this.clienteOServer.equals("cliente")) {
            iniciarCliente();
        } else if (this.clienteOServer.equals("server")) {
            iniciarServer();
        }
    }

    public void onClickEnviarMensaje(View v) {
        EditText et = (EditText)findViewById(R.id.editTextMensaje);
        this.mensajes.add(et.getText().toString());
        enviarMensaje(et.getText().toString());
        this.arrayAdapter.notifyDataSetChanged();
    }

    private void enviarMensaje(String txt)
    {
        new EnviarMensajeThread(txt).start();
    }

    public void iniciarServer() {
        (HiloEspera=new EsperaClienteThread()).start();
    }

    public void iniciarCliente() {
        if(this.ip.length()>5) {
            (new ClienteConectarAServerThread(this.ip)).start();
        }
    }

    private class EsperaClienteThread extends Thread {
        public void run() {
            try {
                //Abrimos el socket
                serverSocket = new ServerSocket(puerto);

                //Mostramos un mensaje para indicar que estamos esperando en la direccion ip y el puerto...
                //AppenText("Creado el servidor\n Dirección: "+getIpAddress()+" Puerto: "+serverSocket.getLocalPort());

                //Creamos un socket que esta a la espera de una conexion de cliente
                socket = serverSocket.accept();

                //Una vez hay conexion con un cliente, creamos los streams de salida/entrada
                try {
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                }catch(Exception e){ e.printStackTrace();}

                ConectionEstablished=true;

                //Iniciamos el hilo para la escucha y procesado de mensajes
                (HiloEscucha=new RecibirMensajeThread()).start();

                HiloEspera=null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class RecibirMensajeThread extends Thread {

        public boolean executing;
        private String line;

        public void run() {
            executing=true;

            while(executing) {
                line="";
                line=ObtenerCadena();//Obtenemos la cadena del buffer
                if(line!="" && line.length()!=0) {//Comprobamos que esa cadena tenga contenido
                    //AppenText("Recibido: "+line + "\n");//Procesamos la cadena recibida
                    //Aqui se escribe
                }
            }
        }

        public void setExecuting (boolean execute) {
            executing=execute;
        }

        private String ObtenerCadena() {
            String cadena="";

            try {
                cadena=dataInputStream.readUTF();//Leemos del datainputStream una cadena UTF
                Log.d("ObtenerCadena", "Cadena reibida: "+cadena);

            } catch(Exception e) {
                e.printStackTrace();
                executing=false;
            }
            return cadena;
        }
    }



    private class ClienteConectarAServerThread extends Thread {
        String mIp;

        public ClienteConectarAServerThread(String ip) {
            mIp = ip;
        }

        public void run()
        {
            try {
                socket = new Socket(mIp, puerto);//Creamos el socket

                try {
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ConectionEstablished=true;

                //Iniciamos el hilo para la escucha y procesado de mensajes
                (HiloEscucha = new RecibirMensajeThread()).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class EnviarMensajeThread extends Thread {
        private String msg;

        public EnviarMensajeThread (String message) {
            msg = message;
        }

        @Override
        public void run(){
            try {
                dataOutputStream.writeUTF(msg);//Enviamos el mensaje
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void DesconectarSockets() {
        if(ConectionEstablished) {
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
}
