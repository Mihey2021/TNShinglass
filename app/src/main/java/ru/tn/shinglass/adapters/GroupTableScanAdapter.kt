package ru.tn.shinglass.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.tn.shinglass.databinding.TableScanItemLayoutBinding
import ru.tn.shinglass.models.TableScan
import ru.tn.shinglass.viewmodel.TableScanFragmentViewModel

class GroupTableScanAdapter(private val onTableScanItemInteractionListener: OnTableScanItemInteractionListener, private val isExternalDocumentDetail: Boolean = false) :
    ListAdapter<TableScan, GroupTableScanAdapter.TableScanHolder>(TableScanDiffCallback()) {

    class TableScanHolder(
        private val binding: TableScanItemLayoutBinding,
        private val onTableScanItemInteractionListener: OnTableScanItemInteractionListener,
        private val isExternalDocumentDetail: Boolean
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tableScan: TableScan) {
            binding.apply {

                //Окрашивание применяем только для строк, где есть документ-основание (кол-во по документу-основанию больше 0)
                if (tableScan.Count > 0.0 && tableScan.docCount > 0.0) {
                    if (tableScan.Count == tableScan.docCount)
                        tableItemScanLayout.setBackgroundColor(Color.parseColor("#90EE90")) //Green
                    if (tableScan.Count < tableScan.docCount)
                        tableItemScanLayout.setBackgroundColor(Color.parseColor("#F0E68C")) //Yellow
                    if (tableScan.Count > tableScan.docCount)
                        tableItemScanLayout.setBackgroundColor(Color.parseColor("#FA8072")) //Red
                }

                titleTextView.text = tableScan.ItemTitle
                titleTextView.setTextColor(Color.BLACK)
                //if (tableScan.docGuid == "")
                if (isExternalDocumentDetail)
                    countTextView.text = "${tableScan.Count} / ${tableScan.docCount}"
                else
                    countTextView.text = tableScan.Count.toString()

                countTextView.setTextColor(Color.BLACK)
                unitOfMeasureTextView.text = tableScan.ItemMeasureOfUnitTitle
                unitOfMeasureTextView.setTextColor(Color.BLACK)
                cellTextView.text = tableScan.cellTitle
                cellTextView.setTextColor(Color.BLACK)
//                titleTextView.setOnClickListener {
//                    onTableScanItemInteractionListener.selectItem(tableScan)
//                }
                tableItemScanLayout.setOnClickListener {
                    onTableScanItemInteractionListener.selectItem(tableScan)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableScanHolder {
        val binding =
            TableScanItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableScanHolder(binding, onTableScanItemInteractionListener, isExternalDocumentDetail)
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