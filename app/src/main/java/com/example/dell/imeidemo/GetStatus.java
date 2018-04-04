package com.example.dell.imeidemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetStatus extends AppCompatActivity {

    EditText inputEt;
    Button getStatusBt;
    TextView responseTxv;

    String did="";//设备号
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_status);

        did = getIntent().getStringExtra("did");
        Log.i("did内容",did);
        initView();
    }

    private void initView(){
        inputEt = findViewById(R.id.inputEt);
        getStatusBt = findViewById(R.id.getStatusBt);
        responseTxv = findViewById(R.id.responseTxv);

        if (did.equals("")){
            //无内容，不做操作
        }else {
            inputEt.setText(did);
        }

        getStatusBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGet(inputEt.getText().toString());
            }
        });
    }

    private void sendGet(String did){
        final String requestUrl = "http://www.duoyue.net/weixin/api/devStatus?did=" + did;//请求地址
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(requestUrl)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String failMsg = e.toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GetStatus.this,failMsg,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseMsg = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        responseTxv.setText(responseMsg);
                    }
                });
            }

        });
    }
}
