package ru.tn.shinglass.adapters

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.view.View
import android.graphics.RectF

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

import android.graphics.pdf.PdfDocument
import java.io.FileOutputStream
import java.io.IOException


class MyPrintDocumentAdapter(
    private val mContext: Context,
    private val mView: View
) : PrintDocumentAdapter() {

    private var mDocument: PrintedPdfDocument? = null


    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        //Создаем новый PdfDocument с запрошенными атрибутами страницы
        mDocument = PrintedPdfDocument(mContext, newAttributes)

        //Ответ на запрос отмены
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val totalPages = 1
        if (totalPages > 0) {
            PrintDocumentInfo.Builder("print_output.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(totalPages)
                .build()
                .also { info ->
                    //Перекомпоновка макета контента завершена
                    callback.onLayoutFinished(info, true)
                }
        } else {
            //В противном случае сообщить об ошибке в инфраструктуру печати
            callback.onLayoutFailed("Ошибка вычисления количества страниц.")
        }
    }

    override fun onWrite(
        pageRanges: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        // Start the page
        // Start the page
        if(mDocument == null) return
        val page: PdfDocument.Page = mDocument!!.startPage(0)
        // Create a bitmap and put it a canvas for the view to draw to. Make it the size of the view
        // Create a bitmap and put it a canvas for the view to draw to. Make it the size of the view
        val bitmap = Bitmap.createBitmap(
            mView.width, mView.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        mView.draw(canvas)
        // create a Rect with the view's dimensions.
        // create a Rect with the view's dimensions.
        val src = Rect(0, 0, mView.width, mView.height)
        // get the page canvas and measure it.
        // get the page canvas and measure it.
        val pageCanvas: Canvas = page.canvas
        val pageWidth: Int = pageCanvas.width
        val pageHeight: Int = pageCanvas.height
        // how can we fit the Rect src onto this page while maintaining aspect ratio?
        // how can we fit the Rect src onto this page while maintaining aspect ratio?
        val scale: Int = Math.min(pageWidth / src.width(), pageHeight / src.height())
        val left: Float = (pageWidth / 2 - src.width() * scale / 2).toFloat()
        val top: Float = (pageHeight / 2 - src.height() * scale / 2).toFloat()
        val right: Float = (pageWidth / 2 + src.width() * scale / 2).toFloat()
        val bottom: Float = (pageHeight / 2 + src.height() * scale / 2).toFloat()
        val dst = RectF(left, top, right, bottom)

        pageCanvas.drawBitmap(bitmap, src, dst, null)
        mDocument!!.finishPage(page)

        try {
            mDocument!!.writeTo(
                FileOutputStream(
                    destination!!.fileDescriptor
                )
            )
        } catch (e: IOException) {
            callback!!.onWriteFailed(e.toString())
            return
        } finally {
            mDocument!!.close()
            mDocument = null
        }
        callback!!.onWriteFinished(arrayOf(PageRange(0, 0)))
    }
}