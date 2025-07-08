package com.ceo.ticketissuance.core.printing

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import com.ceo.ticketissuance.domain.usecase.TicketGenerationResult
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

class TicketPrintDocumentAdapter(
    private val context: Context,
    private val ticketResult: TicketGenerationResult,
    private val onPrintStarted: () -> Unit,
    private val onPrintCompleted: () -> Unit,
    private val onPrintFailed: (String) -> Unit
) : PrintDocumentAdapter() {
    
    private var pdfDocument: PrintedPdfDocument? = null
    
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        try {
            pdfDocument = PrintedPdfDocument(context, newAttributes)
            
            val info = PrintDocumentInfo.Builder("ticket_${ticketResult.ticket.ticketNumber}.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build()
            
            callback.onLayoutFinished(info, true)
        } catch (e: Exception) {
            onPrintFailed(e.message ?: "Layout failed")
            callback.onLayoutFailed(e.message)
        }
    }
    
    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        try {
            onPrintStarted()
            
            val page = pdfDocument?.startPage(0)
            if (page != null) {
                drawTicketContent(page.canvas)
                pdfDocument?.finishPage(page)
            }
            
            pdfDocument?.writeTo(FileOutputStream(destination.fileDescriptor))
            
            onPrintCompleted()
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            onPrintFailed(e.message ?: "Write failed")
            callback.onWriteFailed(e.message)
        } finally {
            pdfDocument?.close()
            pdfDocument = null
        }
    }
    
    override fun onFinish() {
        super.onFinish()
        pdfDocument?.close()
        pdfDocument = null
    }
    
    private fun drawTicketContent(canvas: Canvas) {
        val ticket = ticketResult.ticket
        val margin = 50f
        var yPosition = margin + 50f
        
        // Paint styles
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        
        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        }
        
        // Header
        canvas.drawText("PENALTY CHARGE NOTICE", margin, yPosition, titlePaint)
        yPosition += 40f
        canvas.drawText("Traffic Management Act 2004", margin, yPosition, bodyPaint)
        yPosition += 60f
        
        // Ticket number box
        val ticketNumberRect = Rect(margin.toInt(), yPosition.toInt(), (canvas.width - margin).toInt(), (yPosition + 60).toInt())
        canvas.drawRect(ticketNumberRect, linePaint)
        canvas.drawText("Ticket Number: ${ticket.ticketNumber}", margin + 20f, yPosition + 40f, headerPaint)
        yPosition += 100f
        
        // Vehicle details
        canvas.drawText("VEHICLE DETAILS", margin, yPosition, headerPaint)
        yPosition += 30f
        canvas.drawText("Registration: ${ticket.vrm}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("Make: ${ticketResult.vehicleMake?.name ?: "Unknown"}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("Model: ${ticketResult.vehicleModel?.name ?: "Unknown"}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("Colour: ${ticket.color}", margin + 20f, yPosition, bodyPaint)
        yPosition += 50f
        
        // Location details
        canvas.drawText("LOCATION DETAILS", margin, yPosition, headerPaint)
        yPosition += 30f
        canvas.drawText("Street: ${ticketResult.street.name} (${ticketResult.street.areaCode})", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("Date: ${ticket.issueTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("Time: ${ticket.issueTime.format(DateTimeFormatter.ofPattern("HH:mm"))}", margin + 20f, yPosition, bodyPaint)
        yPosition += 50f
        
        // Contravention details
        canvas.drawText("CONTRAVENTION DETAILS", margin, yPosition, headerPaint)
        yPosition += 30f
        canvas.drawText("Code: ${ticketResult.contravention.code}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("Description: ${ticketResult.contravention.description}", margin + 20f, yPosition, bodyPaint)
        yPosition += 50f
        
        // Penalty details
        canvas.drawText("PENALTY DETAILS", margin, yPosition, headerPaint)
        yPosition += 30f
        canvas.drawText("Penalty Amount: £${ticket.penaltyAmount}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("Early Payment Discount: £${ticket.discountAmount}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("Discounted Amount: £${ticket.discountAmount}", margin + 20f, yPosition, bodyPaint)
        yPosition += 50f
        
        // Officer details
        canvas.drawText("OFFICER DETAILS", margin, yPosition, headerPaint)
        yPosition += 30f
        canvas.drawText("Officer: ${ticketResult.officerName}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("Badge: ${ticketResult.badgeNumber}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        if (ticketResult.officerSignature != null) {
            canvas.drawText("Signature: ✓ Captured", margin + 20f, yPosition, bodyPaint)
        }
        yPosition += 50f
        
        // Footer
        canvas.drawLine(margin, yPosition, canvas.width - margin, yPosition, linePaint)
        yPosition += 30f
        canvas.drawText("This notice is served under the Traffic Management Act 2004.", margin, yPosition, bodyPaint)
        yPosition += 25f
        canvas.drawText("You have the right to make representations or appeal this penalty charge notice.", margin, yPosition, bodyPaint)
    }
}