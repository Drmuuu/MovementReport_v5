package com.railway.movementreport.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import com.railway.movementreport.data.entity.NightDutyEntry
import java.io.File
import java.io.FileOutputStream

class NightDutyPdfGenerator(private val context: Context) {

    private val PAGE_W    = 595f
    private val PAGE_H    = 842f
    private val MARGIN    = 30f
    private val ROW_H     = 18f
    private val HDR_H     = 36f   // double-row table header
    private val TITLE_H   = 44f
    private val INFO_H    = 52f
    private val SUMMARY_H = 44f
    private val FOOTER_H  = 36f
    private val GAP       = 6f
    private val CELL_FS   = 7.5f
    private val HDR_FS    = 8f

    fun generate(
        entries: List<NightDutyEntry>,
        monthYear: String,
        userPrefs: UserPreferences,
        place: String,
        exportDate: String
    ): File {
        val doc  = PdfDocument()
        val page = doc.startPage(PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), 1).create())
        draw(page.canvas, entries, monthYear, userPrefs, place, exportDate)
        doc.finishPage(page)
        val fileName = "NightDutyBill_${monthYear.replace("-","_")}_$exportDate.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private fun draw(
        canvas: Canvas,
        entries: List<NightDutyEntry>,
        monthYear: String,
        prefs: UserPreferences,
        place: String,
        exportDate: String
    ) {
        val cx        = PAGE_W / 2f
        val thickLine = strokePaint(2f)
        val thinLine  = strokePaint(0.7f)
        var y = MARGIN

        // ── TITLE BLOCK (same as Movement Report) ───────────────
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thickLine); y += 2.5f
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thinLine);  y += 14f

        canvas.drawText("NIGHT DUTY ALLOWANCE BILL", cx, y,
            makePaint(13f, bold = true, align = Paint.Align.CENTER))
        y += 12f
        canvas.drawText(DateUtils.getDisplayMonthYear(monthYear).uppercase(), cx, y,
            makePaint(9.5f, bold = true, align = Paint.Align.CENTER))
        y += 6f

        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thinLine);  y += 2.5f
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thickLine); y += GAP

        // ── INFO BLOCK (same style as Movement Report) ───────────
        val infoTop = y
        val c1 = MARGIN + 6f
        val c2 = MARGIN + (PAGE_W - 2 * MARGIN) / 2f + 6f
        y += 11f
        infoRow(canvas, "Name",        prefs.name,         c1, y)
        infoRow(canvas, "Designation", prefs.designation,  c2, y); y += 13f
        infoRow(canvas, "PF No",       prefs.pfNumber,     c1, y)
        infoRow(canvas, "Pay",         "Rs. ${prefs.pay}", c2, y); y += 13f
        infoRow(canvas, "Level",       prefs.level,        c1, y); y += 8f
        canvas.drawRoundRect(MARGIN, infoTop, PAGE_W - MARGIN, y, 3f, 3f, thinLine)
        y += GAP

        // ── TABLE ────────────────────────────────────────────────
        // 9 columns — same proportions as before but with blue header
        val cw   = PAGE_W - 2 * MARGIN
        val cols = floatArrayOf(
            cw * 0.10f,  // From Date
            cw * 0.09f,  // Train No
            cw * 0.13f,  // From Station
            cw * 0.13f,  // To Station
            cw * 0.09f,  // Night Duty From
            cw * 0.09f,  // Night Duty To
            cw * 0.13f,  // To Date
            cw * 0.12f,  // Total N Hrs
            cw * 0.12f   // Remark
        )
        val tableTop = y

        // ── HEADER ROW 1 — deep blue background (same as MR) ────
        canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + HDR_H,
            fillPaint(Color.parseColor("#1a237e")))

        val hdrP   = makePaint(HDR_FS, bold = true, align = Paint.Align.CENTER, color = Color.WHITE)
        val row1   = arrayOf("From","Train","From","To","Night Duty Hrs","","To Date","Total N","Remark")
        val col4x  = MARGIN + cols[0]+cols[1]+cols[2]+cols[3]
        val col5x  = col4x + cols[4]

        var hx = MARGIN
        for (i in cols.indices) {
            when (i) {
                4 -> {
                    // "Night Duty Hrs" spans cols 4 + 5
                    val spanCx = col4x + (cols[4] + cols[5]) / 2f
                    canvas.drawText("Night Duty Hrs", spanCx, y + 11f, hdrP)
                }
                5 -> { /* skip — covered by span above */ }
                else -> if (row1[i].isNotEmpty()) {
                    canvas.drawText(row1[i], hx + cols[i] / 2f, y + 11f, hdrP)
                }
            }
            hx += cols[i]
        }

        // Mid divider (white, half-opacity) separating row1 / row2
        val midY = y + HDR_H / 2f
        canvas.drawLine(MARGIN, midY, PAGE_W - MARGIN, midY,
            strokePaint(0.5f, Color.parseColor("#7986cb")))
        // Erase mid-divider between col 4 and 5 (they share "Night Duty Hrs" label)
        canvas.drawLine(col5x, midY - 1f, col5x, midY + 1f,
            strokePaint(2f, Color.parseColor("#1a237e")))

        // Row 2 labels
        val row2 = arrayOf("Date","No","","","From","To","","Hrs","")
        hx = MARGIN
        for (i in cols.indices) {
            if (row2[i].isNotEmpty()) {
                canvas.drawText(row2[i], hx + cols[i] / 2f, y + HDR_H - 6f, hdrP)
            }
            hx += cols[i]
        }

        // Vertical header lines (white, subtle)
        val vHdrLine = strokePaint(0.5f, Color.parseColor("#7986cb"))
        hx = MARGIN
        for (i in cols.indices) {
            canvas.drawLine(hx, y, hx, y + HDR_H, vHdrLine)
            hx += cols[i]
        }
        canvas.drawLine(hx, y, hx, y + HDR_H, vHdrLine)
        // Remove the vertical between col 4 and 5 in the top half
        canvas.drawLine(col5x, y + 1f, col5x, midY - 1f,
            strokePaint(2f, Color.parseColor("#1a237e")))

        // Top + bottom border of header
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thickLine)
        canvas.drawLine(MARGIN, y + HDR_H, PAGE_W - MARGIN, y + HDR_H, thinLine)

        y += HDR_H

        // ── DATA ROWS ────────────────────────────────────────────
        val altBg = fillPaint(Color.parseColor("#f7f7f7"))
        val cellP = makePaint(CELL_FS, align = Paint.Align.CENTER)

		for ((idx, entry) in entries.withIndex()) {

		    val remarkPaint = makePaint(
		        CELL_FS,
		        bold = true,
		        align = Paint.Align.CENTER,
		        color = Color.parseColor("#4a148c")
		    )

		    val remarkLines =
		        wrapText(
		            entry.remark,
		            remarkPaint,
		            cols[8] - 8f
		        )

		    val lineHeight = 10f

		    val rowHeight =
		        maxOf(
		            ROW_H,
		            remarkLines.size * lineHeight + 8f
		        )

		    val rowBot = y + rowHeight

		    if (idx % 2 == 1) {
		        canvas.drawRect(
		            MARGIN,
		            y,
		            PAGE_W - MARGIN,
		            rowBot,
		            altBg
		        )
		    }

		    val cells = arrayOf(
		        DateUtils.dbToDisplayShort(entry.fromDate),
		        entry.trainNumber,
		        entry.stationFrom,
		        entry.stationTo,
		        entry.nightDutyFrom,
		        entry.nightDutyTo,
		        DateUtils.dbToDisplayShort(entry.toDate),
		        entry.totalNightHrs,
		        entry.remark
		    )

		    var colX = MARGIN

		    for (i in cells.indices) {

		        if (i != 8) {

		            canvas.drawText(
		                cells[i],
		                colX + cols[i] / 2f,
		                y + rowHeight / 2f + 3f,
		                cellP
		            )

		        } else {

		            val startY =
		                y +
		                rowHeight / 2f -
		                ((remarkLines.size - 1) * lineHeight / 2f)

		            for ((lineIndex, line) in remarkLines.withIndex()) {

		                canvas.drawText(
		                    line,
		                    colX + cols[i] / 2f,
		                    startY + lineIndex * lineHeight,
		                    remarkPaint
		                )

		            }
		        }

		        colX += cols[i]
		    }

		    canvas.drawLine(
		        MARGIN,
		        rowBot,
		        PAGE_W - MARGIN,
		        rowBot,
		        thinLine
		    )

		    y = rowBot
		}

        // Vertical data column lines
        hx = MARGIN
        canvas.drawLine(hx, tableTop, hx, y, thinLine)
        for (w in cols) { hx += w; canvas.drawLine(hx, tableTop, hx, y, thinLine) }
        canvas.drawLine(MARGIN, tableTop, PAGE_W - MARGIN, tableTop, thinLine)
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, thinLine)

        y += GAP

        // ── SUMMARY BOX (same blue style as MR) ──────────────────
        // Calculate total night hours from entries
        val totalMins = entries.sumOf { parseMinsOrZero(it.totalNightHrs) }
        val totalHrs  = totalMins / 60
        val totalMin  = totalMins % 60

        canvas.drawRoundRect(MARGIN, y, PAGE_W - MARGIN, y + SUMMARY_H, 4f, 4f,
            fillPaint(Color.parseColor("#e8eaf6")))
        canvas.drawRoundRect(MARGIN, y, PAGE_W - MARGIN, y + SUMMARY_H, 4f, 4f, thinLine)

        canvas.drawText("SUMMARY", cx, y + 11f,
            makePaint(7.5f, bold = true, align = Paint.Align.CENTER,
                color = Color.parseColor("#1a237e")))

        val cw3 = (PAGE_W - 2 * MARGIN)
        // Only Total Night Hours in summary — always shown (never zero if there are entries)
        val summaryItems = listOf(
            "Total Night Hours" to String.format("%02d:%02d", totalHrs, totalMin)
        ).filter { it.second != "00:00" || entries.isNotEmpty() }
        var sx = MARGIN
        for ((lbl, val_) in summaryItems) {
            val scx = sx + cw3 / 2f
            canvas.drawText(lbl, scx, y + 25f,
                makePaint(7.5f, bold = true, align = Paint.Align.CENTER,
                    color = Color.parseColor("#1a237e")))
            canvas.drawText(val_, scx, y + 38f,
                makePaint(12f, bold = true, align = Paint.Align.CENTER))
            sx += cw3
        }
        y += SUMMARY_H + GAP

        // ── FOOTER (same layout as MR) ────────────────────────────
        val fy = PAGE_H - MARGIN - 20f
        canvas.drawText("Place : $place",      MARGIN,        fy, makePaint(8f))
        canvas.drawText("Date  : $exportDate", MARGIN + 150f, fy, makePaint(8f))
        // canvas.drawLine(PAGE_W - MARGIN - 150f, fy, PAGE_W - MARGIN, fy, thinLine)
        canvas.drawText("(${prefs.name})",
            PAGE_W - MARGIN, fy + 10f,
            makePaint(7.5f, bold = true, align = Paint.Align.RIGHT))
        canvas.drawText(prefs.designation,
            PAGE_W - MARGIN, fy + 20f,
            makePaint(7f, align = Paint.Align.RIGHT))
    }


	private fun wrapText(
	    text: String,
	    paint: Paint,
	    maxWidth: Float
	): List<String> {

	    if (text.isBlank()) return listOf("")

	    val words = text.split(" ")

	    val lines = mutableListOf<String>()

	    var current = ""

	    for (word in words) {

	        val test =
	            if (current.isEmpty())
	                word
	            else
	                "$current $word"

	        if (paint.measureText(test) <= maxWidth) {

	            current = test

	        } else {

	            if (current.isNotEmpty())
	                lines.add(current)

	            current = word

	        }
	    }

	    if (current.isNotEmpty())
	        lines.add(current)

	    return lines
	}

    /** Parse "HH:MM" → total minutes, returns 0 if "--" or invalid */
    private fun parseMinsOrZero(time: String): Int {
        if (time == "--" || time.isBlank()) return 0
        val parts = time.split(":")
        if (parts.size != 2) return 0
        val h = parts[0].toIntOrNull() ?: return 0
        val m = parts[1].toIntOrNull() ?: return 0
        return h * 60 + m
    }

    private fun makePaint(
        size: Float, bold: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT,
        color: Int = Color.BLACK
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize  = size
        typeface  = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        textAlign = align
        this.color = color
    }

    private fun strokePaint(w: Float, color: Int = Color.BLACK) =
        Paint().apply { strokeWidth = w; style = Paint.Style.STROKE; this.color = color }

    private fun fillPaint(color: Int) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; this.color = color }

    private fun infoRow(canvas: Canvas, label: String, value: String, x: Float, y: Float) {
        val lbl = makePaint(7.5f, bold = true)
        canvas.drawText("$label: ", x, y, lbl)
        canvas.drawText(value, x + lbl.measureText("$label:  "), y, makePaint(7.5f))
    }
}
