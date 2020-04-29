package com.brins.locksmith.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brins.locksmith.R
import com.brins.locksmith.data.AppConfig.APPNAME
import com.brins.locksmith.data.AppConfig.USERNAME
import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.utils.AccountIconUtil
import com.chad.library.adapter.base.model.BaseData
import com.makeramen.roundedimageview.RoundedImageView

/**
 * @author lipeilin
 * @date 2020/4/29
 */
class AutofillAdapter(var context: Context, var list: MutableList<in BaseData>) :
    RecyclerView.Adapter<AutofillAdapter.ViewHolder>(), View.OnClickListener {

    var onItemClickListener: OnItemClickListener? = null


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mDataName: TextView = itemView.findViewById(R.id.tv_name)
        val mDataAccount: TextView = itemView.findViewById(R.id.tv_account)
        val mDataImage: RoundedImageView = itemView.findViewById(R.id.iv_password)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_password_recycler, parent, false)
        view.setOnClickListener(this)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = (list[position] as BaseMainData)
        holder.mDataName.text = data.generalItems[APPNAME]
        holder.mDataAccount.text = data.generalItems[USERNAME]
        val image = AccountIconUtil.generateBitmap(data, context)
        holder.mDataImage.setImageDrawable(image)
        //将position保存在itemView的Tag中，以便点击时进行获取
        holder.itemView.tag = position

    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    override fun onClick(v: View) {
        onItemClickListener?.onItemClick(v, v.tag as Int)
    }
}