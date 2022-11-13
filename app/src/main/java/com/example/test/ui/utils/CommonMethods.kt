package com.example.test.ui.utils

import com.example.test.model.UserModel

class CommonMethods {

    companion object {

        fun verifyUser(userList: List<UserModel>?, mailId:String, password: String, isFromRegister: Boolean): String{

            var result = ""
            if (mailId.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mailId).matches()){
                result = "Provide a valid mail address"
            }else if(password.isEmpty() || password.length < 6){
                result = "Password should not be less than 6 characters"
            }else if (isFromRegister){

                if (userList != null){
                    userList.forEach {
                        if (it.email.equals(mailId, true)){
                            result = "User already exist, Please login to continue"
                        }
                    }
                }
            }else {

                var isContainsLogin = false
                var isWrongPass = false
                if(userList != null){
                    userList.forEach {
                        if (it.email.equals(mailId, true)){

                            if (!password.equals(it.password, true)){
                                isWrongPass = true
                            }
                            isContainsLogin = true
                        }
                    }
                    if(!isContainsLogin){
                        result = "User not exist, Please register to continue"
                    }else if(isWrongPass){
                        result = "Incorrect password!!"
                    }
                }else result = "User not exist, Please register to continue"
            }

            return result
        }
    }
}