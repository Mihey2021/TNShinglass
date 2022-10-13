package ru.tn.shinglass.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.tn.shinglass.databinding.TableScanItemLayoutBinding
import ru.tn.shinglass.models.TableScan


class TableScanAdapter(private val onTableScanItemInteractionListener: OnTableScanItemInteractionListener) :
    ListAdapter<TableScan, TableScanAdapter.TableScanHolder>(TableScanDiffCallback()) {

    class TableScanHolder(
        private val binding: TableScanItemLayoutBinding,
        private val onTableScanItemInteractionListener: OnTableScanItemInteractionListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tableScan: TableScan) {
            binding.apply {
                titleTextView.text = tableScan.ItemTitle
                titleTextView.setTextColor(Color.BLACK)
                countTextView.text = tableScan.Count.toString()
                countTextView.setTextColor(Color.BLACK)
                unitOfMeasureTextView.text = tableScan.ItemMeasureOfUnitTitle
                unitOfMeasureTextView.setTextColor(Color.BLACK)
                cellTextView.text = tableScan.cellTitle
                cellTextView.setTextColor(Color.BLACK)
                titleTextView.setOnClickListener {
                    onTableScanItemInteractionListener.selectItem(tableScan)
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

    class TableScanDiffCallback : DiffUtil.ItemCallback<TableScan>() {
        override fun areItemsTheSame(oldItem: TableScan, newItem: TableScan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TableScan, newItem: TableScan): Boolean {
            return true //oldItem == newItem
        }

        //Еще один способ не применять анимацию (убрать "мерцание")
        override fun getChangePayload(oldItem: TableScan, newItem: TableScan): Any = Unit
    }

}