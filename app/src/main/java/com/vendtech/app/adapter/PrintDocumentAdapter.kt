package com.vendtech.app.adapter

import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import androidx.annotation.RequiresApi
import com.vendtech.app.ui.Print.PrintScreenActivity
import java.io.FileOutputStream
import java.io.IOException


@RequiresApi(Build.VERSION_CODES.KITKAT)
class MyPrintDocumentAdapter() : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder("my_file.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<PageRange?>?,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        // Here you write your content to the output stream associated with the destination ParcelFileDescriptor.
        // For example, you can write text or graphics data here.
        // Be sure to close the output stream when you're done.
        try {
            FileOutputStream(destination.fileDescriptor).use({ output ->
                // Write your content to the output stream
                // For example:
                // output.write("Hello, World!".getBytes());
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            })
        } catch (e: IOException) {
            // Handle any exceptions here
        }
    }
}