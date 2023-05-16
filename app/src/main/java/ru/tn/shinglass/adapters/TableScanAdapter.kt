package ru.tn.shinglass.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.tn.shinglass.R
import ru.tn.shinglass.databinding.TableScanGroupItemLayoutBinding
import ru.tn.shinglass.models.TableScan

class TableScanAdapter(
    private val onTableScanItemInteractionListener: OnTableScanItemInteractionListener,
    private val isExternalDocument: Boolean = false,
    private val isExternalDocumentDetail: Boolean = false,
    private val emptyCellText: String = "",
) :
    ListAdapter<TableScan, TableScanAdapter.TableScanHolder>(TableScanDiffCallback()) {

    class TableScanHolder(
        private val binding: TableScanGroupItemLayoutBinding,
        private val onTableScanItemInteractionListener: OnTableScanItemInteractionListener,
        private val isExternalDocument: Boolean,
        private val isExternalDocumentDetail: Boolean,
        private val emptyCellText: String = "",
    ) :
        RecyclerView.ViewHolder(binding.root) {

        val itemCard = binding.root

        @SuppressLint("SetTextI18n")
        fun bind(tableScan: TableScan) {
            binding.apply {

//                if (binding.titleTextView.height >= binding.itemCountTextView.height) {
//                    val lpTitle = binding.titleTextView.layoutParams
//                    lpTitle.height = ViewGroup.LayoutParams.WRAP_CONTENT
//                    binding.titleTextView.layoutParams = lpTitle
//                    val lpTotalCount = binding.itemCountTextView.layoutParams
//                    lpTotalCount.height = 0
//                    binding.itemCountTextView.layoutParams = lpTotalCount
//                } else {
//                    val lpTitle = binding.titleTextView.layoutParams
//                    lpTitle.height = 0
//                    binding.titleTextView.layoutParams = lpTitle
//                    val lpTotalCount = binding.itemCountTextView.layoutParams
//                    lpTotalCount.height = ViewGroup.LayoutParams.WRAP_CONTENT
//                    binding.itemCountTextView.layoutParams = lpTotalCount
//                }

//                if (tableScan.totalCount > 0.0 || tableScan.Count == 0.0)
                //if (tableScan.isGroup || tableScan.Count == 0.0)
                if (tableScan.isGroup) {
                    binding.totalCountGroup.visibility = View.VISIBLE
                    binding.detailItemGroup.visibility = View.GONE
                } else {
                    binding.totalCountGroup.visibility = View.GONE
                    binding.detailItemGroup.visibility = View.VISIBLE
                }

                val transparent = "#80FFFFFF"
                totalCountGroup.setBackgroundColor(Color.parseColor(transparent))
                detailItemGroup.setBackgroundColor(Color.parseColor(transparent))


//                binding.detailItemGroup.visibility =
//                    if (isExternalDocumentDetail ||) View.GONE else View.VISIBLE

                //Окрашивание по принципу светофор применяем только для строк, где есть документ-основание (кол-во по документу-основанию больше 0)
                //if (tableScan.totalCount > 0.0 && tableScan.docCount > 0.0) {
                if (tableScan.docCount > 0.0) {
                    applyColorsToRecord(tableScan, binding)
                }
//                if (tableScan.Count > 0.0 && tableScan.docCount > 0.0) {
//                    if (tableScan.Count == tableScan.docCount)
//                        tableItemScanLayout.setBackgroundColor(Color.parseColor("#90EE90")) //Green
//                    if (tableScan.Count < tableScan.docCount)
//                        tableItemScanLayout.setBackgroundColor(Color.parseColor("#F0E68C")) //Yellow
//                    if (tableScan.Count > tableScan.docCount)
//                        tableItemScanLayout.setBackgroundColor(Color.parseColor("#FA8072")) //Red
//                }

                titleTextView.text = "${tableScan.ItemTitle}, ${tableScan.ItemMeasureOfUnitTitle}"
                titleTextView.setTextColor(Color.BLACK)
                //if (tableScan.docGuid == "")
                if (isExternalDocument)
                    itemCountTextView.text =
                        "${tableScan.totalCount} / ${tableScan.docCount}"
                else
                    itemCountTextView.text =
                        "${tableScan.totalCount} ${tableScan.ItemMeasureOfUnitTitle}"

                countTextView.text = tableScan.Count.toString()
//                if (isExternalDocumentDetail)
//                    countTextView.text = "${tableScan.Count} / ${tableScan.docCount}"
//                else
//                    countTextView.text = tableScan.Count.toString()

                countTextView.setTextColor(Color.BLACK)
                unitOfMeasureTextView.text = tableScan.ItemMeasureOfUnitTitle
                unitOfMeasureTextView.setTextColor(Color.BLACK)

                if (tableScan.cellGuid != "") {
                    if (tableScan.cellReceiverGuid != "")
                        cellTextView.text = tableScan.cellReceiverTitle
                    else
                        cellTextView.text = tableScan.cellTitle
                }
                else {
                    cellTextView.text = emptyCellText
                }

                cellTextView.setTextColor(Color.BLACK)
//                titleTextView.setOnClickListener {
//                    onTableScanItemInteractionListener.selectItem(tableScan)
//                }
                tableItemScanLayout.setOnClickListener {
                    onTableScanItemInteractionListener.selectItem(tableScan)
                }
            }
        }

        private fun applyColorsToRecord(
            record: TableScan,
            binding: TableScanGroupItemLayoutBinding
        ) {
            val colorGreen = "#ccffcc" //"#90EE90"
            val colorYellow = "#ffffcc" //"#F0E68C"
            val colorRed = "#ff9999" //"#FA8072"
            val transparent = "#80FFFFFF"
            with(binding) {
                if (record.totalCount == record.docCount) {
                    totalCountGroup.setBackgroundColor(Color.parseColor(colorGreen))
                    detailItemGroup.setBackgroundColor(Color.parseColor(colorGreen))
                }
                if (record.totalCount < record.docCount) {
                    totalCountGroup.setBackgroundColor(Color.parseColor(colorYellow))
                    detailItemGroup.setBackgroundColor(Color.parseColor(colorYellow))
                }
                if (record.totalCount > record.docCount) {
                    totalCountGroup.setBackgroundColor(Color.parseColor(colorRed))
                    detailItemGroup.setBackgroundColor(Color.parseColor(colorRed))
                }
                if (record.totalCount == 0.0) {
                    totalCountGroup.setBackgroundColor(Color.parseColor(transparent))
                    detailItemGroup.setBackgroundColor(Color.parseColor(transparent))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableScanHolder {
        val binding =
            TableScanGroupItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return TableScanHolder(
            binding,
            onTableScanItemInteractionListener,
            isExternalDocument,
            isExternalDocumentDetail,
            emptyCellText,
        )
    }

    override fun onBindViewHolder(holder: TableScanHolder, position: Int) {
        val item = getItem(position)
        println("LoggingDiff Bind: ${item.ItemTitle}, id: ${item.id}, position: $position")
        holder.bind(item)
        holder.itemCard.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_table_scan)
    }

    class TableScanDiffCallback : DiffUtil.ItemCallback<TableScan>() {
        override fun areItemsTheSame(oldItem: TableScan, newItem: TableScan): Boolean {
            println("LoggingDiff areItemsTheSame: ${newItem.ItemTitle}, id: ${newItem.id}, result: ${oldItem.id == newItem.id}")
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: TableScan, newItem: TableScan): Boolean {
            println("LoggingDiff areContentsTheSame: ${newItem.ItemTitle}, id: ${newItem.id}, result: ${oldItem.isGroup == newItem.isGroup
                    && oldItem.cellGuid == newItem.cellGuid
                    && oldItem.Count == newItem.Count
                    && oldItem.totalCount == newItem.totalCount
                    && oldItem.refreshing == newItem.refreshing}")

            return oldItem.isGroup == newItem.isGroup
                && oldItem.cellGuid == newItem.cellGuid
                && oldItem.Count == newItem.Count
                && oldItem.totalCount == newItem.totalCount
                && oldItem.refreshing == newItem.refreshing
        //return true //oldItem == newItem
        }

        //Еще один способ не применять анимацию (убрать "мерцание")
        override fun getChangePayload(oldItem: TableScan, newItem: TableScan): Any = Unit
    }

}