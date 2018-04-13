package com.example.dell.imeidemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.dell.imeidemo.Util.SomeUtils.coverDid;

public class GetStatus extends AppCompatActivity {

    EditText inputEt;
    Button getStatusBt;
    TextView responseTxv;

    String did = "";//设备号
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_status);

        did = getIntent().getStringExtra("did");

        initView();
    }

    private void initView(){
        inputEt = findViewById(R.id.inputEt);
        getStatusBt = findViewById(R.id.getStatusBt);
        responseTxv = findViewById(R.id.responseTxv);

        inputEt.setText(did);

        getStatusBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //将输入框的数字作为设备号向服务器发送请求，并补上适当数量的0
                sendGet(coverDid(inputEt.getText().toString()));
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
                final StringBuffer sb = new StringBuffer();
                try {
                    JSONObject jsonObject = new JSONObject(responseMsg);
                    JSONObject object = new JSONObject(jsonObject.getString("content"));
                    sb.append("返回码：" + jsonObject.getInt("errcode") + "\n");
                    sb.append("返回状态：" + jsonObject.getString("errmsg") + "\n");
                    sb.append("设备号：" + object.getString("did") + "\n");
                    sb.append("是否在线：" + object.getString("online") + "\n");
                    sb.append("更新时间：" + object.getString("updatetime") + "\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        responseTxv.setText(sb.toString());
                    }
                });
            }

        });
    }
}
