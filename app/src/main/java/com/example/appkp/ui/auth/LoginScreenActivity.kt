package com.example.appkp.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.appkp.R
import com.example.appkp.api.RetrofitBuilder
import com.example.appkp.model.UserResponse
import com.example.appkp.ui.PhotoScreenActivity
import com.example.appkp.ui.auth.presenter.LoginPresenter
import com.example.appkp.ui.auth.view.IResult
import com.example.appkp.util.Constant
import com.example.appkp.util.Preferences
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.edt_name
import kotlinx.android.synthetic.main.activity_login.edt_password
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback

class LoginScreenActivity : AppCompatActivity(), IResult {


    lateinit var loginPresenter: LoginPresenter
    lateinit var preference: Preferences
    private val TAG = "LoginTag"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        loginPresenter = LoginPresenter(this@LoginScreenActivity)
        preference = Preferences(this)
        preference.setValue("firstLaunch", "first")



        btn_login.setOnClickListener {
            val email = edt_name.text.toString()
            val password = edt_password.text.toString()

            val login = loginPresenter.onLogin(email, password)

            if (login) {
                RetrofitBuilder.api.onLogin(email, password)
                    .enqueue(object : Callback<UserResponse> {

                        override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                            onError("Login Failed")
                        }

                        override fun onResponse(
                            call: Call<UserResponse>,
                            response: retrofit2.Response<UserResponse>
                        ) {
                            try {
                                val success = response.body()?.success
                                if (success!!) {
                                    val name = response.body()?.user!!.name
                                    val token = response.body()?.token

                                    preference = Preferences(this@LoginScreenActivity).apply {
                                        setValue("token", token!!)
                                        setValue("name", name)
                                        setValue("isLoggedIn", "true")

                                        onSuccess("Login Success")

                                        startActivity(
                                            Intent(
                                                this@LoginScreenActivity.applicationContext,
                                                PhotoScreenActivity::class.java
                                            )
                                        )
                                        finishAffinity()
                                    }
                                }

                            } catch (e: JSONException) {
                                Log.d(TAG, e.printStackTrace().toString())
                            }
                        }
                    })
            }
        }


        tv_daftar.setOnClickListener{
            startActivity(Intent(this, RegisterScreenActivity::class.java))
        }
    }


    override fun onSuccess(message: String) {
        Toasty.success(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        Toasty.error(this, message, Toast.LENGTH_SHORT).show()
    }

}

