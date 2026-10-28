package com.WorkCollabration.App;

/**
 * Created by Hari on 10/17/2015.
 */

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
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Signup extends Activity {

	EditText editName;
	EditText editEmail;
	EditText editPassword;
	String userName;
	String userEmailId;
	String userPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
	}

	public void openSignIn(View v){
		Intent openSignIn=new Intent(Signup.this,Loginpage.class);
		startActivity(openSignIn);
	}

	public void submitSignUp(View v){
		editName   = (EditText)findViewById(R.id.editname);
		editEmail   = (EditText)findViewById(R.id.editemail);
		editPassword   = (EditText)findViewById(R.id.editpassword);
		userName=editName.getText().toString().trim();
		userEmailId=editEmail.getText().toString().trim();
		userPassword=editPassword.getText().toString();
		if(userName.isEmpty()){
			editName.setError("Name is required");
			return;
		}
		if(!Patterns.EMAIL_ADDRESS.matcher(userEmailId).matches()){
			editEmail.setError("Enter a valid email address");
			return;
		}
		if(userPassword.length()<4){
			editPassword.setError("Password must be at least 4 characters");
			return;
		}
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("UserName",userName);
		params.put("Password",userPassword);
		params.put("EmailId",userEmailId);
		try {
			JSONObject jsonObject=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("SignUp.php"), params));
			if (jsonObject.getInt("Result")==0){
				Intent verification=new Intent(Signup.this,Verification.class);
				verification.putExtra("emailid", userEmailId);
				startActivity(verification);
			}
			else{
				Toast.makeText(getApplicationContext(), "User Already Exists, Enter another Email ID", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.w("Signup", "Sign up failed", e);
			Toast.makeText(getApplicationContext(), "Could not reach the server, try again later", Toast.LENGTH_LONG).show();
		}
	}
}
