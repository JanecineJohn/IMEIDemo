package com.example.dell.imeidemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.imeidemo.Beans.AppAuthBean;
import com.example.dell.imeidemo.Util.CheckPermissionsActivity;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.dell.imeidemo.Util.SomeUtils.coverDid;

public class MainActivity extends CheckPermissionsActivity {

    EditText inputEt;
    TextView responseTxv;
    Button sendBt, queryIMEI, getStatus;

    String mDid = "";//设备号
    String mUid = "";//唯一ID
    String textIMEI = "";/**测试用IMEI*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        inputEt = findViewById(R.id.inputDid);
        responseTxv = findViewById(R.id.responseMsg);
        sendBt = findViewById(R.id.sendRequestBt);
        queryIMEI = findViewById(R.id.startQueryActivity);
        getStatus = findViewById(R.id.startGetStatusActivity);

        /**测试用*/
        textIMEI = getIMEI();
        if (textIMEI == null || textIMEI.equals("000000000000000")){
            textIMEI = "357710061152818";
        }
        inputEt.setText(textIMEI);
        //responseTxv.setText(textIMEI);
        sendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputEt.getText().equals("")) {
                    Toast.makeText(MainActivity.this, "请输入设备号", Toast.LENGTH_SHORT).show();
                } else {
                    String did = inputEt.getText().toString().trim();
                    mDid = did;//记录填入的did(20位)
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

        appAuth(inputEt.getText().toString().trim());
    }

    private void sendGet(String did) {
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
                final StringBuffer sb = new StringBuffer();//存储解析的返回信息
                //获取UID
                try {
                    final JSONObject object = new JSONObject(responseMsg);
                    final int errCode = object.getInt("errcode");
                    final String errmsg = object.getString("errmsg");
                    if (errCode == 0) {
                        String content = object.getString("content");
                        Log.i("content内容", content);
                        JSONObject contentObj = new JSONObject(content);
                        //解析嵌套JsonObject信息
                        mUid = contentObj.getString("uid");//获取到uid
                        String datetime = contentObj.getString("datatime");
                        String uid = contentObj.getString("uid");
                        //将解析到的返回信息存入sb
                        sb.append("返回码：" + errCode + "\n");
                        sb.append("返回状态：" + errmsg + "\n");
                        sb.append("写入日期：" + datetime + "\n");
                        sb.append("唯一ID：" + uid + "\n");
                    } else {
                        sb.append("返回码：" + errCode + "\n");
                        sb.append("返回状态：" + errmsg + "\n");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            responseTxv.setText(sb.toString());
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**获取设备IMEI码*/
    private String getIMEI() {
        //实例化TelephonyManager对象
        TelephonyManager telephonyManager = (TelephonyManager)
                MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
        String imei= "";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_PHONE_STATE},1);
        }else {
            imei = telephonyManager.getDeviceId();
        }
        return imei;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , String[] permissions, int[] paramArrayOfInt) {
        if (1 == requestCode){
            if (paramArrayOfInt.length > 0 &&
                    paramArrayOfInt[0] == PackageManager.PERMISSION_GRANTED){
                getIMEI();
            }
        }
    }

    /**打开查询IMEI界面
     * 需要传过去uid
     * */
    private void startQueryIMEI(){
        Intent intent = new Intent(MainActivity.this,QueryIMEI.class);
        if (mUid.equals("")){
            mUid = "20180322000001";
        }
        intent.putExtra("uid",mUid);
        startActivity(intent);
    }

    /**打开获取设备状态界面
     * 需要传过去did
     * */
    private void startGetStatus(){
        Intent intent = new Intent(MainActivity.this,GetStatus.class);
        intent.putExtra("did",mDid);
        startActivity(intent);
    }

    /**打开页面首先鉴权
     * 需要传过去did
     * */
    private void appAuth(String did){
        final String requestUrl = "http://www.duoyue.net/weixin/api/appAuth?did=" + did;
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
                        Toast.makeText(MainActivity.this,
                                "无法连接服务器",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseMsg = response.body().string();
                final StringBuffer sb = new StringBuffer();
                try {
                    JSONObject jsonObject = new JSONObject(responseMsg);
                    int errCode = jsonObject.getInt("errcode");
                    if (0 == errCode){
                        final AppAuthBean appAuthBean =
                                new Gson().fromJson(responseMsg,AppAuthBean.class);
                        Log.i("runOnUiThread方法体外",appAuthBean.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("runOnUiThread方法体内",appAuthBean.toString());
                                Toast.makeText(MainActivity.this,
                                        "失效日期：" + appAuthBean.getExpire_data(),Toast.LENGTH_SHORT).show();//失效日期
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "该设备号已失效",Toast.LENGTH_SHORT).show();
                                MainActivity.this.finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
