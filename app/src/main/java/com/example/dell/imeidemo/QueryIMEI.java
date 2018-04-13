package com.example.dell.imeidemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QueryIMEI extends AppCompatActivity {
    EditText inputEt;
    Button queryBt;
    TextView responseTv;

    String uid="";//唯一ID
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_imei);
        uid = getIntent().getStringExtra("uid");

        initView();
    }

    private void initView(){
        inputEt = findViewById(R.id.inputEt);
        queryBt = findViewById(R.id.queryBt);
        responseTv = findViewById(R.id.responseTxv);

        inputEt.setText(uid);

        queryBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGet(inputEt.getText().toString());//将输入框的内容放入请求函数
            }
        });
    }

    private void sendGet(String uid){
        final String requestUrl = "http://cloud.duoyue.net/weixin/api/imeiGenerator?cmd=query&dev=DYV56&uid=" + uid;//请求地址
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
                        Toast.makeText(QueryIMEI.this,failMsg,Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseMsg = response.body().string();
                final StringBuffer sb = new StringBuffer();
                try {
                    JSONObject jsonObject = new JSONObject(responseMsg);
                    int errcode = jsonObject.getInt("errcode");
                    String errmsg = jsonObject.getString("errmsg");
                    sb.append("返回码：" + errcode + "\n");
                    sb.append("返回状态：" + errmsg + "\n");
                    if (0 == errcode){
                        JSONObject object = new JSONObject(jsonObject.getString("content"));
                        String command = object.getString("command");
                        String datetime = object.getString("datetime");
                        String uid = object.getString("uid");
                        sb.append("动作：" + command + "\n");
                        sb.append("写入日期：" + datetime + "\n");
                        sb.append("唯一ID：" + uid + "\n");
                        if (object.has("imei")){
                            //如果有IMEI字段
                            String imei = object.getString("imei");
                            sb.append("设备号：" + imei);
                        }else {
                            //否则是IMEI集合
                            JSONArray array = object.getJSONArray("imei_list");
                            for (int i=1;i<=array.length();i++){
                                sb.append("设备号" + i + "：" + array.get(i-1) + "\n");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        responseTv.setText(sb.toString());
                    }
                });
            }

        });
    }
}
