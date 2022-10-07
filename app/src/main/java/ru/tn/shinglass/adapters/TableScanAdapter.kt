package ru.tn.shinglass.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.tn.shinglass.databinding.TableScanItemLayoutBinding
import ru.tn.shinglass.models.CurrentScanData


class TableScanAdapter(private val onTableScanItemInteractionListener: OnTableScanItemInteractionListener) :
    ListAdapter<CurrentScanData, TableScanAdapter.TableScanHolder>(TableScanDiffCallback()) {

    class TableScanHolder(
        private val binding: TableScanItemLayoutBinding,
        private val onTableScanItemInteractionListener: OnTableScanItemInteractionListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentScanData: CurrentScanData) {
            binding.apply {
                titleTextView.text = currentScanData.title
                titleTextView.setTextColor(Color.BLACK)
                countTextView.text = "1.0"
                countTextView.setTextColor(Color.BLACK)
                unitOfMeasureTextView.text = currentScanData.unitOfMeasureTitle
                unitOfMeasureTextView.setTextColor(Color.BLACK)
                cellTextView.text = "SH1-007-1-B-3-0015"
                cellTextView.setTextColor(Color.BLACK)
                titleTextView.setOnClickListener {
                    onTableScanItemInteractionListener.selectItem(currentScanData)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableScanHolder {
        val binding = TableScanItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableScanHolder(binding, onTableScanItemInteractionListener)
    }

    override fun onBindViewHolder(holder: TableScanHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class TableScanDiffCallback : DiffUtil.ItemCallback<CurrentScanData>() {
        override fun areItemsTheSame(oldItem: CurrentScanData, newItem: CurrentScanData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CurrentScanData, newItem: CurrentScanData): Boolean {
            return true //oldItem == newItem
        }

        //Еще один способ не применять анимацию (убрать "мерцание")
        override fun getChangePayload(oldItem: CurrentScanData, newItem: CurrentScanData): Any = Unit
    }

}