package com.example.yfsl.socketcommunication_demo;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
//    private ServerSocket serverSocket;
//    private Socket socket;
    private ExecutorService threadPool;
    private TextView message;
    private Observable<String> rxBus;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn);
        message = findViewById(R.id.message);
        button.setOnClickListener(this);
        threadPool = Executors.newCachedThreadPool();
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                socketServer();
            }
        });
        rxBus = RxBus.get().register("111",String.class);
        rxBus.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                message.setText(s);
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    });
}

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        count ++;
        switch (viewId){
            case R.id.btn:
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        socketClient(count);
                    }
                });
                break;
        }
    }

    /**
     * 客户端
     */
    private void socketClient(int count) {
        OutputStream outputStream = null;
        Socket socket = null;
        try {
            socket = new Socket("192.168.200.2",34324);
            String data = "呼叫服务器 呼叫服务器 over"+count;
            outputStream = socket.getOutputStream();
            outputStream.write(data.getBytes());
            outputStream.flush();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 服务端
     */
    @SuppressLint("SetTextI18n")
    private void socketServer() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(34324);
            serverSocket.bind(inetSocketAddress);
            while (true) {
                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024*4];
                int temp = 0;
                while ((temp = inputStream.read(buffer)) != -1) {
                    Log.e("TAG","消息："+new String(buffer,0,temp,"UTF-8"));
                    log("========================================");
                    RxBus.get().post("111",new String(buffer,0,temp,"UTF-8"));
//                    message.setText(new String(buffer,0,temp,"UTF-8")+"\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        try {
//            serverSocket.close();
//            socket.close();
//            serverSocket = null;
//            socket = null;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void log(String msg){
        Log.e("TAG",msg);
    }
}
