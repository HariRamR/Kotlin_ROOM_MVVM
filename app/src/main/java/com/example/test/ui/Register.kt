package com.example.test.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R
import com.example.test.model.UserModel
import com.example.test.ui.utils.CommonMethods
import com.example.test.view_model.UserViewModel
import com.google.android.material.snackbar.Snackbar

class Register: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val linearRoot = findViewById<LinearLayout>(R.id.linear_register)
        val nameEt = findViewById<EditText>(R.id.user_name_et_register)
        val mailEt = findViewById<EditText>(R.id.mail_id_et_register)
        val passwordEt = findViewById<EditText>(R.id.password_et_register)
        val progressBar = findViewById<ProgressBar>(R.id.progress_register)
        val doneBtn = findViewById<Button>(R.id.done_btn_register)

        val userViewModel = UserViewModel(application)
        val userList: List<UserModel>? = userViewModel.getUserData().value

        doneBtn.setOnClickListener {
            val msg = CommonMethods.verifyUser(
                userList,
                mailEt.text.toString(),
                passwordEt.text.toString(),
                true
            )
            if (msg.equals("")
            ) {
                progressBar.visibility = View.VISIBLE
                userViewModel.insertUser(UserModel(name = nameEt.text.toString(), email = mailEt.text.toString(), password = passwordEt.text.toString(), isCurrentUser = 1))

                startActivity(Intent(this, Dashboard()::class.java))
                progressBar.visibility = View.GONE
                finish()
        }else Snackbar.make(linearRoot, msg, Snackbar.LENGTH_SHORT).show()
        }
    }
}