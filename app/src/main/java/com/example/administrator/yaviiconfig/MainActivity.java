package com.example.administrator.yaviiconfig;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends Activity implements View.OnClickListener{
    EditText etReceiver,servicePort,serviceIp,wifiEt,wifiPassEt;
    TextView etIp;
    Button btConn,btSend;
    ImageView freshIv;
    OutputStream os;
    InputStream ips;
    Writer writer;
    Reader reader;
    public boolean isConnected = false;
    private MyHandler myHandler;
    DatagramSocket socket;
    String ip;
    int port;
    InetAddress addr ;
    private AutoCompleteTextView etSend;
    private ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_ui);

        String [] arr={"@YAV: D298,yavii123,192.168.0.255,808",
                "@AP:D298,yavii123",
                "@IP:192.168.0.255,808",
                "@ID:00001",
                "@MODE:0",
                "@SERVER:0",
                "@SERVER:1",
                "@DT:1",
                "@BAUDR：2400",
                "@BAUDR：4800",
                "@BAUDR：9600",
                "@BAUDR：115200",
                "@HB:1",
                "@CLEAR",
                "@CH:0",
                "@CH:1",
                "@CH:2",
                "@ALL",
                "@END",
                "MODE=2_11_2_2AD00001",
                "DT=2_2AD00001",
                "CC=0_05_2AD00001",
                "COUNT=0_2AD00001",
                "DO=1_1000_500_2AD00001"
        };




        etIp= (TextView) findViewById(R.id.et_ip);
        serviceIp=(EditText) findViewById(R.id.service_et_ip);
        servicePort= (EditText) findViewById(R.id.service_et_port);
        wifiEt= (EditText) findViewById(R.id.wifi_et);
        wifiPassEt= (EditText) findViewById(R.id.wifi_pass_et);
        btConn=(Button) findViewById(R.id.bt_connect);
        btSend=(Button) findViewById(R.id.bt_send);
        freshIv= (ImageView) findViewById(R.id.fresh_iv);
        freshIv.setOnClickListener(this);
        btConn.setOnClickListener(this);
        btSend.setOnClickListener(this);
        etSend= (AutoCompleteTextView) findViewById(R.id.et_send);
        etReceiver=(EditText) findViewById(R.id.et_recevier);
        myHandler = new MyHandler();
        GetIp getIp=new GetIp();
        etIp.setText(getIp.getIp(this));
        ip=etIp.getText().toString().trim();
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.auto_complete_adapter,arr);
        etSend.setAdapter(arrayAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(etIp.getText().equals("0.0.0.0")){
            Toast.makeText(MainActivity.this,"请连接wifi后刷新",Toast.LENGTH_SHORT).show();
        }else{
            if(isConnected==false){
                btConn.performClick();
            }
        }


    }

    private void sendData(final String context){
       if(addr==null||addr.getHostAddress().equals(ip)){
           //Toast.makeText(MainActivity.this,"暂无发送目标，无法发出指令",Toast.LENGTH_SHORT).show();
           String temp[]=ip.split("\\.");
           String ip2=temp[0]+"."+temp[1]+"."+temp[2]+".255";
           try {
               addr=InetAddress.getByName(ip2);
           } catch (UnknownHostException e) {
               e.printStackTrace();
           }
           port=1256;
       }
           new Thread(){
               public void run() {
                   byte[] sendBuf;
                   sendBuf=context.getBytes();

                   DatagramPacket sendPacket
                           = new DatagramPacket(sendBuf , sendBuf.length , addr , port );
                   try {
                       socket.send(sendPacket);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               };
           }.start();







    }

    private void connectThread() {
        if(!isConnected){
            new Thread(){
                public void run() {
                    connectServer( etIp.getText().toString(),"1256");
                };
            }.start();
        }else{
            if(socket!=null){
                socket.close();

            }

            btConn.setText("连接");
            isConnected=false;
        }

    }

    protected void connectServer(String ip,String port) {
        try {
            InetAddress ia = InetAddress.getByName(ip);
            socket = new DatagramSocket(Integer.parseInt(port));

            myHandler.sendEmptyMessage(2);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInfo(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            receiverData(msg.what);
            if(msg.what==1){
                String result = msg.getData().get("msg").toString();
                etReceiver.setText(result);
            }
        }
    }

    public void receiverData(int flag) {
        if(flag==2){
            btConn.setText("断开");
            showInfo("连接成功");
            isConnected=true;
            new Thread(){
                public void run() {
                    while(true){
                        if(isConnected){

                            byte[] buf = new byte[1024];
                            String result = null;

                            DatagramPacket packet = new DatagramPacket(buf,
                                    buf.length);


                            try {
                                socket.receive(packet);
                                port = packet.getPort();
                                int a=port;
                                addr = packet.getAddress();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            result = new String(buf, 0, packet.getLength());
                            if (!result.equals("")){

                                Message msg = new Message();
                                msg.what = 1;
                                Bundle data = new Bundle();
                                data.putString("msg", result);
                                msg.setData(data);
                                myHandler.sendMessage(msg);
                            }

                        }
                    }



                };
            }.start();
        }

    }





    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bt_connect:
                connectThread();

                break;
            case R.id.bt_send:
                String context=etSend.getText().toString().trim();
                sendData(context);
                break;
            case R.id.fresh_iv:
                GetIp getIp=new GetIp();
                String ip = getIp.getIp(this);
                etIp.setText(ip);
                break;
        }

    }

    public void send1(View v){
        String wifi=wifiEt.getText().toString().trim();
        String pass=wifiPassEt.getText().toString().trim();
        String context="@AP:"+wifi+","+pass;
        sendData(context);
    }
    public void send2(View v){
        String ip=serviceIp.getText().toString().trim();
        String port=servicePort.getText().toString().trim();
        String context="@IP:"+ip+","+port;
        sendData(context);
    }



    @Override
    protected void onStop() {
        super.onStop();
        if(isConnected==true){
            btConn.performClick();
        }
    }
}
