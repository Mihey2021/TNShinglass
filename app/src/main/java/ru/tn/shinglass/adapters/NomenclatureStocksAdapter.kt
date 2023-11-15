package ru.tn.shinglass.adapters

import android.content.Context
import android.graphics.Color
import android.view.*
import android.view.View.OnLongClickListener
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.tn.shinglass.R
import ru.tn.shinglass.databinding.TableScanGroupItemLayoutBinding
import ru.tn.shinglass.models.NomenclatureStocks

const val IDM_PRINT_CELL = 1
const val IDM_PRINT_ITEM = 2

class NomenclatureStocksAdapter(
    private val onStocksItemInteractionListener: OnStocksItemInteractionListener,
    private val groupByCell: Boolean = false,
) : ListAdapter<NomenclatureStocks, NomenclatureStocksAdapter.NomenclatureStocksHolder>(
    NomenclatureStocksDiffCallback()
) {

    private var position: NomenclatureStocks? = null

    fun getPosition() = position

    fun setPosition(position: NomenclatureStocks) {
        this.position = position
    }

    class NomenclatureStocksHolder(
        private val binding: TableScanGroupItemLayoutBinding,
        private val groupByCell: Boolean,
        private val onStocksItemInteractionListener: OnStocksItemInteractionListener,
        private val context: Context,
    ) : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
        val itemCard = binding.root
        fun bind(nomenclatureStocks: NomenclatureStocks) {
            binding.apply {
                if (nomenclatureStocks.isGroup) {
                    totalCountGroup.visibility = View.VISIBLE
                    detailItemGroup.visibility = View.GONE
                } else {
                    totalCountGroup.visibility = View.GONE
                    detailItemGroup.visibility = View.VISIBLE
                    tableItemScanLayout.setOnCreateContextMenuListener(this@NomenclatureStocksHolder)
                    tableItemScanLayout.isLongClickable = true
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

        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu.setHeaderTitle(R.string.print_barcode_text)
            menu.setHeaderIcon(R.drawable.ic_baseline_print_24)
            menu.add(
                Menu.NONE,
                IDM_PRINT_CELL,
                Menu.NONE,
                context.getString(R.string.cell_text)
            )
            menu.add(
                Menu.NONE,
                IDM_PRINT_ITEM,
                Menu.NONE,
                context.getString(R.string.nomenclature_title)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NomenclatureStocksHolder {
        val binding = TableScanGroupItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NomenclatureStocksHolder(
            binding,
            groupByCell,
            onStocksItemInteractionListener,
            parent.context
        )
    }

    override fun onBindViewHolder(holder: NomenclatureStocksHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemCard.animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_table_scan)
        //Добавим прослушиватель по долгому тапу для контекстного меню
        val itemDetailView = holder.itemView.findViewById<Group>(R.id.detailItemGroup)
        itemDetailView.setOnLongClickListener {
            setPosition(item)
            false
        }
    }

    //Очистим прослушиватель по долгому тапу для контекстного меню
    override fun onViewRecycled(holder: NomenclatureStocksHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
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