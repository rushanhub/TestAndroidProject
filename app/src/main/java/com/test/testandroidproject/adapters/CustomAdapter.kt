package com.test.testandroidproject.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.test.testandroidproject.R
import com.test.testandroidproject.data.PostInfo
import com.test.testandroidproject.data.UserInfo

// Адаптер для ExpandableListView

class CustomAdapter internal constructor(
    private val context: Context,
    private var titleList: List<UserInfo>,
    private var expandList: LinkedHashMap<UserInfo, List<PostInfo>>,
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return this.titleList.size
    }

    // Метод принимает данные в адаптер
    fun setData(titleList1: List<UserInfo>, expandList1: LinkedHashMap<UserInfo, List<PostInfo>>) {
        this.titleList = titleList1
        this.expandList = expandList1
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.expandList[this.titleList[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleList[listPosition]
    }

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.expandList[this.titleList[listPosition]]!![expandedListPosition]
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        listPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?,
    ): View {
        var convertView1 = convertView
        val dataItemList = getGroup(listPosition) as UserInfo
        if (convertView1 == null) {
            val layoutInflater =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView1 = layoutInflater.inflate(R.layout.list_item, null)
        }

        val tvItemName = convertView1?.findViewById<TextView>(R.id.tv_item_name)
        val imgArrow = convertView1?.findViewById<ImageView>(R.id.img_arrow)
        val tvItemEmail = convertView1?.findViewById<TextView>(R.id.tv_item_email)
        val tvItemPhone = convertView1?.findViewById<TextView>(R.id.tv_item_phone)
        val tvItemWebsite = convertView1?.findViewById<TextView>(R.id.tv_item_website)
        tvItemName?.text = dataItemList.name
        if (isExpanded) {
            imgArrow?.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.up_arrow))
        } else {
            imgArrow?.setImageDrawable(ContextCompat.getDrawable(this.context,
                R.drawable.down_arrow))
        }

        tvItemWebsite?.text = context.resources.getString(R.string.web_site)
        tvItemWebsite?.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://" + dataItemList.website))
            context.startActivity(intent)
        }

        tvItemPhone?.text = dataItemList.phone
        tvItemEmail?.text = dataItemList.email
        return convertView1!!
    }

    override fun getChildView(
        listPosition: Int,
        expandedListPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?,
    ): View {
        var convertView1 = convertView
        val expandedListData = getChild(listPosition, expandedListPosition) as PostInfo
        if (convertView1 == null) {
            val layoutInflater =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView1 = layoutInflater.inflate(R.layout.list_item_expanded, null)
        }
        val tvItemExpTitle = convertView1?.findViewById<TextView>(R.id.tv_item_exp_title)
        val tvItemExpDescription =
            convertView1?.findViewById<TextView>(R.id.tv_item_exp_description)
        tvItemExpTitle?.text = expandedListData.title
        tvItemExpDescription?.text = expandedListData.body
        return convertView1!!
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }

}