package com.example.dell.imeidemo;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    EditText inputEt;
    TextView responseTxv;
    Button sendBt,queryIMEI,getStatus;

    String mDid="";//设备号
    String mUid="";//唯一ID
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        inputEt = findViewById(R.id.inputDid);
        responseTxv = findViewById(R.id.responseMsg);
        sendBt = findViewById(R.id.sendRequestBt);
        queryIMEI = findViewById(R.id.startQueryActivity);
        getStatus = findViewById(R.id.startGetStatusActivity);

        sendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputEt.getText().equals("")){
                    Toast.makeText(MainActivity.this,"请输入设备号",Toast.LENGTH_SHORT).show();
                }else {
                    String did = inputEt.getText().toString().trim();
                    mDid = coverDid(did);//记录填入的did(20位)
                    sendGet(coverDid(did));
                }
            }
        });

        queryIMEI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQueryIMEI();
            }
        });

        getStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGetStatus();
            }
        });
    }

    private void sendGet(String did){
        final String requestUrl = "http://www.duoyue.net/weixin/api/writeImei?did=" + did + "&dev=DYV56&uid=20171116002013&reboot=true";//请求地址
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
                        responseTxv.setText(failMsg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseMsg = response.body().string();
                //获取UID
                try {
                    JSONObject object = new JSONObject(responseMsg);
                    int errCode = object.getInt("errcode");
                    if (errCode == 0){
                        String content = object.getString("content");
                        Log.i("content内容",content);
                        JSONObject contentObj = new JSONObject(content);
                        mUid = contentObj.getString("uid");//获取到uid
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        responseTxv.setText(responseMsg);
                    }
                });
            }

        });
    }

    //00000362523450038487
    //在设备号前面补0，补到20位
    private String coverDid(String did){
        int coverNum = 20 - did.length();
        String coverStr = "";
        for (int i=0;i<coverNum;i++){
            coverStr += "0";
        }
        return coverStr + did;
    }

    //打开查询IMEI界面
    private void startQueryIMEI(){
        Intent intent = new Intent(MainActivity.this,QueryIMEI.class);
        if (mUid.equals("")){
            mUid = "20180322000001";
        }
        intent.putExtra("uid",mUid);
        startActivity(intent);
    }

    //打开获取设备状态界面
    private void startGetStatus(){
        Intent intent = new Intent(MainActivity.this,GetStatus.class);
        intent.putExtra("did",mDid);
        startActivity(intent);
    }
}
