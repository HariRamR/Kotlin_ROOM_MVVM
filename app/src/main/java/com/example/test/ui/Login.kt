package com.example.test.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.test.R
import com.example.test.ui.utils.CommonMethods
import com.example.test.view_model.UserViewModel
import com.google.android.material.snackbar.Snackbar

class Login: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val linearRoot = findViewById<LinearLayout>(R.id.linear_login)
        val mailEt = findViewById<EditText>(R.id.mail_id_et_login)
        val passwordEt = findViewById<EditText>(R.id.password_et_login)
        val registerBtn = findViewById<TextView>(R.id.register_txt_login)
        val progressBar = findViewById<ProgressBar>(R.id.progress_login)
        val loginBtn = findViewById<Button>(R.id.login_btn_login)

        registerBtn.setOnClickListener{
            startActivity(Intent(this, Register::class.java))
        }

        val userViewModel = UserViewModel(application)

        loginBtn.setOnClickListener {

            userViewModel.getUserData().observe(this, Observer {
                if (it != null){
                    progressBar.visibility = View.VISIBLE
                    val msg = CommonMethods.verifyUser(
                        it,
                        mailEt.text.toString(),
                        passwordEt.text.toString(),
                        false
                    )
                    if (msg.equals("")
                    ) {
                        userViewModel.updateUser(mailEt.text.toString())
                        startActivity(Intent(this, Dashboard()::class.java))
                        finish()
                    }else Snackbar.make(linearRoot, msg, Snackbar.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }else Snackbar.make(linearRoot, "User not exist, Please register to continue", Snackbar.LENGTH_SHORT).show()
            })
        }
    }
}