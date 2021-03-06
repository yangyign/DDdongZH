package com.example.androidzonghe1.activity.xtWork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.androidzonghe1.ConfigUtil;
import com.example.androidzonghe1.R;
import com.example.androidzonghe1.activity.lpyWork.MyTheActivity;
import com.example.androidzonghe1.activity.yjWork.ActivityLoginPage;
import com.example.androidzonghe1.activity.yjWork.LoginActivity;
import com.example.androidzonghe1.activity.yjWork.PolicyActivity;
import com.example.androidzonghe1.activity.yjWork.ServiceAgreementActivity;
import com.example.androidzonghe1.activity.yjWork.UserAgreementActivity;

public class AcitivySetting extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    LinearLayout lGoodReputation;
    LinearLayout lUserAgreement;
    LinearLayout lServiceAgreement;
    LinearLayout lPersonMsg;
    LinearLayout lAboutDingDong;
    Button btnLogOff;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_setting);

        toolbar = findViewById(R.id.toolBar);
        btnLogOff = findViewById(R.id.btn_log_off);
        lGoodReputation = findViewById(R.id.ll_good_reputation);
        lUserAgreement = findViewById(R.id.ll_user_agreement);
        lServiceAgreement = findViewById(R.id.ll_service_agreement);
        lPersonMsg = findViewById(R.id.ll_person_msg);
        lAboutDingDong = findViewById(R.id.ll_about_dingdong);

        btnLogOff.setOnClickListener(this::onClick);
        lGoodReputation.setOnClickListener(this::onClick);
        lUserAgreement.setOnClickListener(this::onClick);
        lServiceAgreement.setOnClickListener(this::onClick);
        lPersonMsg.setOnClickListener(this::onClick);
        lAboutDingDong.setOnClickListener(this::onClick);

        if (getActionBar() != null){
            getActionBar().hide();
        }
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ActivityQuestion", "toolbar back onClicked");
                finish();
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_log_off://????????????
                Log.e("ActivitySetting", "btnLogOff onClicked");
                if(ConfigUtil.isLogin){
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    ConfigUtil.isLogin = false;
                    editor.putBoolean("isLogin", ConfigUtil.isLogin);
                    editor.apply();
                    //?????????????????????
                    Intent intent = new Intent(AcitivySetting.this, MyTheActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
//                    finish();
                }else {
                    Toast.makeText(this,"????????????",Toast.LENGTH_SHORT);
                }
                break;
            case R.id.ll_good_reputation://???????????????
                Log.e("ActivitySetting", "lGoodReputation onClicked");
                break;
            case R.id.ll_user_agreement:
                Log.e("ActivitySetting", "lUserAgreement onClicked");
                Intent userIntent = new Intent(getApplicationContext(), UserAgreementActivity.class);
                startActivity(userIntent);
                break;
            case R.id.ll_service_agreement:
                Log.e("ActivitySetting", "lServiceAgreement onClicked");
                Intent serviceAgreementIntent = new Intent(getApplicationContext(), ServiceAgreementActivity.class);
                startActivity(serviceAgreementIntent);
                break;
            case R.id.ll_person_msg:
                Log.e("ActivitySetting", "lPersonMsg onClicked");
                Intent msgIntent = new Intent(getApplicationContext(), PolicyActivity.class);
                startActivity(msgIntent);
                break;
            case R.id.ll_about_dingdong:
                Log.e("ActivitySetting", "lAboutDingDong onClicked");
                Intent aboutIntent = new Intent(getApplicationContext(), ActivityAboutDingDong.class);
                startActivity(aboutIntent);
                break;
        }
    }
}
