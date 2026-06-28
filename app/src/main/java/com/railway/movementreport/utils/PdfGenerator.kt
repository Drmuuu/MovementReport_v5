package com.railway.movementreport.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import com.railway.movementreport.data.entity.MovementEntry
import java.io.File
import java.io.FileOutputStream

class PdfGenerator(private val context: Context) {

    // A4 in PDF points
    private val PAGE_W = 595f
    private val PAGE_H = 842f
    private val MARGIN = 30f

    // Compact sizing — fits 31 rows + header + info block + summary + footer on one page
    private val ROW_H       = 16.5f
    private val TABLE_HDR_H = 18f
    private val INFO_H      = 52f   // info block height
    private val TITLE_H     = 44f   // title block height
    private val SUMMARY_H   = 44f
    private val FOOTER_H    = 36f
    private val GAP         = 6f

    fun generateReport(
        entries: List<MovementEntry>,
        monthYear: String,
        userPrefs: UserPreferences,
        place: String,
        exportDate: String
    ): File {
        val doc = PdfDocument()

        // Calculate if everything fits on one page
        val tableBodyH = entries.size * ROW_H
        val totalH = MARGIN + TITLE_H + GAP + INFO_H + GAP +
                     TABLE_HDR_H + tableBodyH + GAP + SUMMARY_H + GAP + FOOTER_H + MARGIN

        // Use one page; if truly too many entries, allow a second
        val needsPages = if (totalH <= PAGE_H) 1 else 2

        if (needsPages == 1) {
            val page = doc.startPage(PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), 1).create())
            drawSinglePage(page.canvas, entries, monthYear, userPrefs, place, exportDate)
            doc.finishPage(page)
        } else {
            // Split across 2 pages
            val firstPageRows = ((PAGE_H - MARGIN - TITLE_H - GAP - INFO_H - GAP -
                                  TABLE_HDR_H - GAP - SUMMARY_H - GAP - FOOTER_H - MARGIN) / ROW_H).toInt()
            val page1 = doc.startPage(PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), 1).create())
            drawPageContent(page1.canvas, entries.take(firstPageRows), monthYear, userPrefs,
                place, exportDate, isFirst = true, isLast = false, allEntries = entries, pageNum = 1, totalPages = 2)
            doc.finishPage(page1)

            val page2 = doc.startPage(PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), 2).create())
            drawPageContent(page2.canvas, entries.drop(firstPageRows), monthYear, userPrefs,
                place, exportDate, isFirst = false, isLast = true, allEntries = entries, pageNum = 2, totalPages = 2)
            doc.finishPage(page2)
        }

        val fileName = "MovementReport_${monthYear.replace("-", "_")}_$exportDate.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // Single-page optimised layout
    private fun drawSinglePage(
        canvas: Canvas,
        entries: List<MovementEntry>,
        monthYear: String,
        userPrefs: UserPreferences,
        place: String,
        exportDate: String
    ) {
        drawPageContent(canvas, entries, monthYear, userPrefs, place, exportDate,
            isFirst = true, isLast = true, allEntries = entries, pageNum = 1, totalPages = 1)
    }

    private fun drawPageContent(
        canvas: Canvas,
        pageEntries: List<MovementEntry>,
        monthYear: String,
        userPrefs: UserPreferences,
        place: String,
        exportDate: String,
        isFirst: Boolean,
        isLast: Boolean,
        allEntries: List<MovementEntry>,
        pageNum: Int,
        totalPages: Int
    ) {
        val cw = PAGE_W - 2 * MARGIN   // content width
        val cx = PAGE_W / 2f

        // ── Paints ──────────────────────────────────────────────
        fun textPaint(size: Float, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT, color: Int = Color.BLACK) =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = size
                typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                textAlign = align
                this.color = color
            }

        val thickLine = Paint().apply { strokeWidth = 2f; style = Paint.Style.STROKE; color = Color.BLACK }
        val thinLine  = Paint().apply { strokeWidth = 0.7f; style = Paint.Style.STROKE; color = Color.BLACK }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        var y = MARGIN

        // ── TITLE BLOCK ─────────────────────────────────────────
        if (isFirst) {
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thickLine); y += 2.5f
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thinLine);  y += 14f

            canvas.drawText("MOVEMENT REPORT",
                cx, y, textPaint(13f, bold = true, align = Paint.Align.CENTER))
            y += 12f
            canvas.drawText(DateUtils.getDisplayMonthYear(monthYear).uppercase(),
                cx, y, textPaint(10f, bold = true, align = Paint.Align.CENTER))
            y += 6f

            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thinLine);  y += 2.5f
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thickLine); y += GAP

            // ── INFO BLOCK ──────────────────────────────────────
            val infoTop = y
            val col1x = MARGIN + 6f
            val col2x = MARGIN + cw / 2f + 6f
            val lbl  = textPaint(7.5f, bold = true)
            val val_ = textPaint(7.5f)

            fun infoRow(label: String, value: String, x: Float, yPos: Float) {
                canvas.drawText("$label: ", x, yPos, lbl)
                canvas.drawText(value, x + lbl.measureText("$label:  "), yPos, val_)
            }

            y += 11f
            infoRow("Name",        userPrefs.name,        col1x, y)
            infoRow("Designation", userPrefs.designation, col2x, y); y += 13f
            infoRow("PF No",       userPrefs.pfNumber,    col1x, y)
            infoRow("Pay",         "₹ ${userPrefs.pay}",  col2x, y); y += 13f
            infoRow("Level",       userPrefs.level,       col1x, y); y += 8f

            // border around info block
            fillPaint.color = Color.TRANSPARENT
            canvas.drawRoundRect(MARGIN, infoTop, PAGE_W - MARGIN, y, 3f, 3f, thinLine)
            y += GAP
        } else {
            // Continuation header
            canvas.drawText(
                "MOVEMENT REPORT — ${DateUtils.getDisplayMonthYear(monthYear).uppercase()}  (Page $pageNum / $totalPages)",
                cx, y + 10f, textPaint(9f, bold = true, align = Paint.Align.CENTER, color = Color.parseColor("#1a237e"))
            )
            y += 18f
        }

        // ── TABLE ────────────────────────────────────────────────
        // Column widths (must sum to cw = 535)
        val cols = floatArrayOf(52f, 62f, 130f, 130f, 54f)   // date|train|from|to|remarks — tight
        val hdrs = arrayOf("Date", "Train No", "From Station", "To Station", "Remarks")

        val tableTop = y

        // Header row background
        fillPaint.color = Color.parseColor("#1a237e")
        canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + TABLE_HDR_H, fillPaint)

        val hdrPaint = textPaint(8f, bold = true, align = Paint.Align.CENTER, color = Color.WHITE)
        var colX = MARGIN
        for (i in hdrs.indices) {
            canvas.drawText(hdrs[i], colX + cols[i] / 2f, y + TABLE_HDR_H / 2f + 3f, hdrPaint)
            colX += cols[i]
        }
        y += TABLE_HDR_H

        // Data rows
        val altBg = Paint().apply { color = Color.parseColor("#f7f7f7"); style = Paint.Style.FILL }
        val cellPaint   = textPaint(7.5f)
        val centerPaint = textPaint(7.5f, align = Paint.Align.CENTER)

        for ((idx, entry) in pageEntries.withIndex()) {
            val rowTop = y; val rowBot = y + ROW_H

            if (idx % 2 == 1) canvas.drawRect(MARGIN, rowTop, PAGE_W - MARGIN, rowBot, altBg)

            val remarkColor = when (entry.remarks) {
                "Duty"   -> Color.parseColor("#1b5e20")
                "Rest"   -> Color.parseColor("#0d47a1")
                "C-Rest" -> Color.parseColor("#4a148c")
                else     -> Color.DKGRAY
            }

            val cells = arrayOf(
                DateUtils.dbToDisplay(entry.date),
                entry.trainNumber,
                entry.stationFrom,
                entry.stationTo,
                entry.remarks
            )

            colX = MARGIN
            for (i in cells.indices) {
                val textY = y + ROW_H / 2f + 3f
                if (i == 4) {
                    // Remarks — centred + coloured bold
                    val rp = textPaint(7.5f, bold = true, align = Paint.Align.CENTER, color = remarkColor)
                    canvas.drawText(cells[i], colX + cols[i] / 2f, textY, rp)
                } else if (i == 0) {
                    // Date — centred
                    canvas.drawText(cells[i], colX + cols[i] / 2f, textY, centerPaint)
                } else if (i == 1) {
                    canvas.drawText(cells[i].ifEmpty { "—" }, colX + cols[i] / 2f, textY, centerPaint)
                } else {
                    // Station names — left aligned, truncated
                    val maxLen = (cols[i] / 5f).toInt()
                    val txt = if (cells[i].isEmpty()) "—"
                              else if (cells[i].length > maxLen) cells[i].take(maxLen - 1) + "…"
                              else cells[i]
                    canvas.drawText(txt, colX + 4f, textY, cellPaint)
                }
                colX += cols[i]
            }

            canvas.drawLine(MARGIN, rowBot, PAGE_W - MARGIN, rowBot, thinLine)
            y = rowBot
        }

        // Vertical lines
        colX = MARGIN
        canvas.drawLine(colX, tableTop, colX, y, thinLine)
        for (w in cols) { colX += w; canvas.drawLine(colX, tableTop, colX, y, thinLine) }
        canvas.drawLine(MARGIN, tableTop, PAGE_W - MARGIN, tableTop, thinLine)

        y += GAP

        // ── SUMMARY ──────────────────────────────────────────────
        if (isLast) {
            // Serialized summary: Duty, Rest, C-Rest, CL, LAP, SICK — always shown in order
            val dutyCount  = allEntries.count { it.remarks == "Duty" }
            val restCount  = allEntries.count { it.remarks == "Rest" }
            val cRestCount = allEntries.count { it.remarks == "C-Rest" }
            val clCount    = allEntries.count { it.remarks == "CL" }
            val lapCount   = allEntries.count { it.remarks == "LAP" }
            val sickCount  = allEntries.count { it.remarks == "SICK" }
            val sclCount   = allEntries.count { it.remarks == "SCL" }
            val odCount    = allEntries.count { it.remarks == "OD" }

            // All 6 always shown — serial order
            val summaryItems = listOf(
                "Duty"   to dutyCount,
                "Rest"   to restCount,
                "C-Rest" to cRestCount,
                "CL"     to clCount,
                "LAP"    to lapCount,
                "SICK"   to sickCount,
                "SCL"    to sclCount,
                "OD"     to odCount
            )
            val colCount = summaryItems.size  // always 6
            val colW     = cw / colCount.toFloat()

            // Summary box — slightly taller to fit 6 items
            val sumBoxH = SUMMARY_H + 2f
            fillPaint.color = Color.parseColor("#e8eaf6")
            canvas.drawRoundRect(MARGIN, y, PAGE_W - MARGIN, y + sumBoxH, 4f, 4f, fillPaint)
            canvas.drawRoundRect(MARGIN, y, PAGE_W - MARGIN, y + sumBoxH, 4f, 4f, thinLine)

            canvas.drawText("SUMMARY",
                cx, y + 11f, textPaint(7.5f, bold = true, align = Paint.Align.CENTER, color = Color.parseColor("#1a237e")))

            var sx = MARGIN
            for ((lbl, count) in summaryItems) {
                val scx = sx + colW / 2f
                canvas.drawText(lbl, scx, y + 25f,
                    textPaint(6.5f, bold = true, align = Paint.Align.CENTER, color = Color.parseColor("#1a237e")))
                canvas.drawText(count.toString(), scx, y + 38f,
                    textPaint(11f, bold = true, align = Paint.Align.CENTER))
                sx += colW
            }
            y += sumBoxH + GAP

            // ── FOOTER ───────────────────────────────────────────
            val footerY = PAGE_H - MARGIN - 20f
            val fp = textPaint(8f)
            canvas.drawText("Place : $place", MARGIN, footerY, fp)
            canvas.drawText("Date  : $exportDate", MARGIN + 150f, footerY, fp)

            // Signature
            // Name + designation — right-aligned, designation centered under name
            val namePaint  = textPaint(7.5f, bold = true, align = Paint.Align.RIGHT)
            val desgPaint  = textPaint(7f, align = Paint.Align.CENTER)
            val nameStr    = "(${userPrefs.name})"
            val nameWidth  = namePaint.measureText(nameStr)
            val nameX      = PAGE_W - MARGIN
            val nameY      = footerY + 10f
            canvas.drawText(nameStr, nameX, nameY, namePaint)
            // Center designation under name: start of name block = nameX - nameWidth
            val desgCx = nameX - nameWidth / 2f
            canvas.drawText(userPrefs.designation, desgCx, footerY + 22f, desgPaint)
        }

        // Page number
        canvas.drawText("Page $pageNum of $totalPages",
            cx, PAGE_H - 8f, textPaint(7f, align = Paint.Align.CENTER, color = Color.GRAY))
    }
}
