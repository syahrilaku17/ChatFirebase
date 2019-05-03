package com.example.myapplication.AdapterC

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.Add_Data.Users
import com.example.myapplication.R
import com.example.myapplication.ShowData
import com.google.firebase.database.FirebaseDatabase

class Adapter (val mCtx: Context, val layotResId : Int, val list: List<Users>): ArrayAdapter<Users>(mCtx, layotResId, list){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(layotResId, null)

        val textNama = view.findViewById<TextView>(R.id.showName)
        val textStatus = view.findViewById<TextView>(R.id.showStatus)

        val buttonUpdate = view.findViewById<TextView>(R.id.btn_update)
        val buttonDelete = view.findViewById<TextView>(R.id.btn_delete)

        val user = list[position]

        textNama.text = user.nama
        textStatus.text = user.status

        buttonUpdate.setOnClickListener {
            showUpdateDialog(user)
        }
        buttonDelete.setOnClickListener {
            Deleteinfo(user)
        }
        return view
    }

    private fun showUpdateDialog( user: Users){
        val builder = AlertDialog.Builder(mCtx)
        builder.setTitle("Update")
        val inflater = LayoutInflater.from(mCtx)
        val view = inflater.inflate(R.layout.update_data, null)
        val textNama = view.findViewById<EditText>(R.id.inputName)
        val textStatus = view.findViewById<EditText>(R.id.inputStatus)

        textNama.setText(user.nama)
        textStatus.setText(user.status)
        builder.setView(view)
        builder.setPositiveButton("Update"){
            dialog, which ->
            val dbUsers = FirebaseDatabase.getInstance().getReference("USERS")
            val nama = textNama.text.toString().trim()
            val status = textStatus.text.toString().trim()
            if (nama.isEmpty()){
                textNama.error = "please enter name"
                textNama.requestFocus()
                return@setPositiveButton
            }
            if (status.isEmpty()){
                textStatus.error = "please enter status"
                textStatus.requestFocus()
                return@setPositiveButton
            }
            
            val user = Users(user.id, nama, status)
            
            dbUsers.child(user.id).setValue(user).addOnCompleteListener { 
                Toast.makeText(mCtx, "Updated", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("No"){
            dialog, which ->
        }
        val alert = builder.create()
        alert.show()
    }
    private fun Deleteinfo( user: Users){
        val progresDialog = ProgressDialog(context, R.style.AppTheme)
        progresDialog.isIndeterminate = true
        progresDialog.setMessage("Deleting...")
        progresDialog.show()
        val mydatabase = FirebaseDatabase.getInstance().getReference("USERS")
        mydatabase.child(user.id).removeValue()
        Toast.makeText(mCtx, "Deleted", Toast.LENGTH_SHORT).show()
        val intent = Intent(context, ShowData::class.java)
        context.startActivity(intent)
        progresDialog.dismiss()
    }
}