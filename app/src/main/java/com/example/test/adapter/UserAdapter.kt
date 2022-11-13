package com.example.test.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.UserModel
import java.util.*

class UserAdapter: RecyclerView.Adapter<UserAdapter.MainViewHolder>() {

    private var userList: List<UserModel>? = listOf()
    private val random: Random = Random()
    private var context: Context?= null

    fun setUserData(userList: List<UserModel>?, context: Context){
        this.userList = userList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {

        return MainViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_dashboard, parent, false))
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {

        holder.userNameId.text = userList!![position].name.substring(0,1)
        val color: Int = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        val drawable = DrawableCompat.wrap(ResourcesCompat.getDrawable(context!!.resources, R.drawable.user_name_bg, context!!.theme)!!)
        DrawableCompat.setTint(drawable, color)
        holder.userNameId.background = drawable
        holder.userName.text = userList!![position].name
        holder.userEMail.text = userList!![position].email
    }

    override fun getItemCount(): Int {
        return if(userList == null) 0 else userList!!.size
    }

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameId: TextView = itemView.findViewById(R.id.id_list_item_dashboard)
        val userName: TextView = itemView.findViewById(R.id.user_name_list_item_dashboard)
        val userEMail: TextView = itemView.findViewById(R.id.mail_list_item_dashboard)
    }
}