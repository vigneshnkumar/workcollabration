package com.WorkCollabration.App;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import com.WorkCollabration.App.net.ApiClient;
import com.WorkCollabration.App.net.ApiConfig;
import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Verification extends Activity{
	EditText verificationId;
	String userEmailId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userEmailId=getIntent().getStringExtra("emailid");
		if (userEmailId==null){
			// Opened without coming from sign up; nothing to verify.
			startActivity(new Intent(this, Signup.class));
			finish();
			return;
		}
		setContentView(R.layout.activity_verification);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
	}

	public void submitVerificaitonId(View v){
		verificationId=(EditText)findViewById(R.id.editVerificationId);
		String verificationCode=verificationId.getText().toString().trim();
		if (!verificationCode.matches("\\d+")){
			verificationId.setError("Enter the numeric code from your email");
			return;
		}
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("VerificationCode",verificationCode);
		params.put("EmailId",userEmailId);
		try {
			JSONObject jsonObject=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("Verification.php"), params));
			if (jsonObject.getInt("Result")==1){
				Toast.makeText(getApplicationContext(), "Successfully Registered", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(Verification.this,Loginpage.class));
			}
			else{
				Toast.makeText(getApplicationContext(), "Invalid Verification Code, Try Again", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.w("Verification", "Verification failed", e);
			Toast.makeText(getApplicationContext(), "Could not reach the server, try again later", Toast.LENGTH_LONG).show();
		}
	}
}
