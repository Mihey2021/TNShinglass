package ru.tn.shinglass.adapters

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
import ru.tn.shinglass.models.NomenclatureStocks

class NomenclatureStocksAdapter(
    private val onStocksItemInteractionListener: OnStocksItemInteractionListener,
    private val groupByCell: Boolean = false,
) : ListAdapter<NomenclatureStocks, NomenclatureStocksAdapter.NomenclatureStocksHolder>(
        NomenclatureStocksDiffCallback()
    ) {

    class NomenclatureStocksHolder(
        private val binding: TableScanGroupItemLayoutBinding,
        private val groupByCell: Boolean,
        private val onStocksItemInteractionListener: OnStocksItemInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        val itemCard = binding.root
        fun bind(nomenclatureStocks: NomenclatureStocks) {
            binding.apply {
                if (nomenclatureStocks.isGroup) {
                    totalCountGroup.visibility = View.VISIBLE
                    detailItemGroup.visibility = View.GONE
                } else {
                    totalCountGroup.visibility = View.GONE
                    detailItemGroup.visibility = View.VISIBLE
                }

                val transparent = "#80FFFFFF"
                totalCountGroup.setBackgroundColor(Color.parseColor(transparent))
                detailItemGroup.setBackgroundColor(Color.parseColor(transparent))
                titleTextView.setTextColor(Color.BLACK)
                countTextView.setTextColor(Color.BLACK)
                unitOfMeasureTextView.setTextColor(Color.BLACK)
                cellTextView.setTextColor(Color.BLACK)
                if (groupByCell) {
                    titleTextView.text =
                        nomenclatureStocks.cell.title
                    cellTextView.text = nomenclatureStocks.nomenclature.itemTitle
                } else {
                    titleTextView.text =
                        "${nomenclatureStocks.nomenclature.itemTitle}, ${nomenclatureStocks.nomenclature.unitOfMeasurementTitle}"
                    cellTextView.text = nomenclatureStocks.cell.title
                }
                itemCountTextView.text =
                    "${nomenclatureStocks.totalCount} ${nomenclatureStocks.nomenclature.unitOfMeasurementTitle}"
                countTextView.text = nomenclatureStocks.totalCount.toString()
                unitOfMeasureTextView.text = nomenclatureStocks.nomenclature.unitOfMeasurementTitle

                tableItemScanLayout.setOnClickListener {
                    onStocksItemInteractionListener.selectItem(nomenclatureStocks)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NomenclatureStocksHolder {
        val binding = TableScanGroupItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NomenclatureStocksHolder(binding, groupByCell, onStocksItemInteractionListener)
    }

    override fun onBindViewHolder(holder: NomenclatureStocksHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemCard.animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_table_scan)
    }

    class NomenclatureStocksDiffCallback : DiffUtil.ItemCallback<NomenclatureStocks>() {
        override fun areItemsTheSame(
            oldItem: NomenclatureStocks,
            newItem: NomenclatureStocks
        ): Boolean {
            return oldItem.nomenclature.itemGuid == newItem.nomenclature.itemGuid
        }

        override fun areContentsTheSame(
            oldItem: NomenclatureStocks,
            newItem: NomenclatureStocks
        ): Boolean {
            return oldItem.nomenclature.qualityGuid == newItem.nomenclature.qualityGuid
                    && oldItem.cell == newItem.cell
                    && oldItem.totalCount == newItem.totalCount
                    && oldItem.nomenclature.unitOfMeasurementGuid == newItem.nomenclature.unitOfMeasurementGuid
        }

        //Еще один способ не применять анимацию (убрать "мерцание")
        override fun getChangePayload(
            oldItem: NomenclatureStocks,
            newItem: NomenclatureStocks
        ): Any = Unit
    }

}