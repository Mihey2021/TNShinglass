package ru.tn.shinglass.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import ru.tn.shinglass.databinding.OptionsGroupBinding
import ru.tn.shinglass.databinding.OptionsItemBinding
import ru.tn.shinglass.models.Option

class OptionsMenuExpListAdapter(
    private val context: Context,
    private val groups: ArrayList<Map<Option, ArrayList<Option>>>,
    private val onInteractionListener: OnOptionsInteractionListener,
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return groups.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return groups[groupPosition][getGroup(groupPosition)]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Option? {
        return groups[groupPosition].keys.elementAtOrNull(0)
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Option? {
        val subOptionsArrayList = groups[groupPosition][getGroup(groupPosition)]
        return subOptionsArrayList?.get(childPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        val bindingGroup = OptionsGroupBinding.inflate(
            LayoutInflater.from(parent?.context ?: context),
            parent,
            false
        )

//        if (convertView == null) {
//            //val inflater = LayoutInflater.from(parent?.context ?: context)
//           bindingGroup = OptionsGroupBinding.inflate(LayoutInflater.from(parent?.context ?: context), parent, false)
//        }

        if (isExpanded) {
            //Изменяем что-нибудь, если текущая Group раскрыта
        } else {
            //Изменяем что-нибудь, если текущая Group скрыта
        }

        val group = getGroup(groupPosition)

        bindingGroup.optionGroupLayout.elevation = 0.1f
        bindingGroup.optionsItemTextView.elevation = 1.0f
        bindingGroup.optionsItemTextView.text = group?.title
        bindingGroup.optionsItemTextView.setTextColor(Color.BLACK)

        return bindingGroup.root

    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        val bindingChild = OptionsItemBinding.inflate(
            LayoutInflater.from(parent?.context ?: context),
            parent,
            false
        )

        val childOptionItem = getChild(groupPosition, childPosition)

        if (childOptionItem != null)
            bindingChild.optionItemLayout.setOnClickListener {
                onInteractionListener.selectOption(childOptionItem)
            }

        bindingChild.optionsItemTextView.text = childOptionItem?.title
        bindingChild.optionsItemTextView.setTextColor(Color.BLACK)
        bindingChild.descriptionOptionTextView.text = childOptionItem?.description
        bindingChild.optionItemLayout.elevation = 0.1f
        bindingChild.optionsItemTextView.elevation = 1.0f
        return bindingChild.root

    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    fun getKeyGroup(group: HashMap<Option, ArrayList<Option>>): Option? {
        return group.keys.elementAtOrNull(0)
    }
}