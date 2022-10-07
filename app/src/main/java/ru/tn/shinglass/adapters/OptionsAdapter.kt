package ru.tn.shinglass.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.tn.shinglass.databinding.OptionsItemBinding
import ru.tn.shinglass.models.Option

//class OptionsAdapter(private val onOptionsInteractionListener: OnOptionsInteractionListener) : ListAdapter<Option, OptionsAdapter.OptionHolder>(OptionDiffCallback()) {
//
//    class OptionHolder(
//        private val binding: OptionsItemBinding,
//        private val onOptionsInteractionListener: OnOptionsInteractionListener
//        ) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(option: Option) {
//            binding.apply {
//                optionTitle.text = option.title
//                postCardView.setOnClickListener {
//                    onOptionsInteractionListener.selectOption(option)
//                }
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionHolder {
//        val binding = OptionsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return OptionHolder(binding, onOptionsInteractionListener)
//    }
//
//    override fun onBindViewHolder(holder: OptionHolder, position: Int) {
//        val option = getItem(position)
//        holder.bind(option)
//    }
//
//    class OptionDiffCallback : DiffUtil.ItemCallback<Option>() {
//        override fun areItemsTheSame(oldItem: Option, newItem: Option): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: Option, newItem: Option): Boolean {
//            return oldItem == newItem
//        }
//
//        //Еще один способ не применять анимацию (убрать "мерцание")
//        override fun getChangePayload(oldItem: Option, newItem: Option): Any = Unit
//    }
//
//}