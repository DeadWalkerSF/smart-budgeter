package com.suman.smartbudgeter.domain.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.suman.smartbudgeter.domain.MonthlyBudgetReport
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExportManager(
    private val context: Context,
) {

    fun exportCsv(report: MonthlyBudgetReport): android.net.Uri {
        val file = createFile("budget-summary-${report.monthLabel.slugify()}.csv")
        val csv = buildString {
            appendLine("Month,${report.monthLabel}")
            appendLine("Generated At,${report.generatedAt}")
            appendLine("Total Budget,${report.snapshot.totalBudget}")
            appendLine("Total Spent,${report.snapshot.totalSpent}")
            appendLine("Remaining Budget,${report.snapshot.remainingBudget}")
            appendLine("Daily Allowance,${report.snapshot.dailyAllowance}")
            appendLine()
            appendLine("Category,Amount,Note,Timestamp")
            report.transactions.forEach { transaction ->
                appendLine(
                    listOf(
                        transaction.categoryName.csvSafe(),
                        transaction.amount.toString(),
                        transaction.note.csvSafe(),
                        transaction.timestamp.toDisplayDateTime(),
                    ).joinToString(","),
                )
            }
        }
        file.writeText(csv)
        return file.toContentUri()
    }

    fun exportPdf(report: MonthlyBudgetReport): android.net.Uri {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 14f
        }
        val titlePaint = Paint(bodyPaint).apply {
            textSize = 20f
            isFakeBoldText = true
        }
        canvas.drawColor(android.graphics.Color.parseColor("#050505"))

        var y = 48f
        canvas.drawText("Smart Budgeter Summary", 32f, y, titlePaint)
        y += 28f
        canvas.drawText(report.monthLabel, 32f, y, bodyPaint)
        y += 24f
        canvas.drawText(
            "Generated ${
                report.generatedAt.format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault()),
                )
            }",
            32f,
            y,
            bodyPaint,
        )
        y += 32f

        listOf(
            "Total Budget: ${report.snapshot.totalBudget.asCurrency()}",
            "Total Spent: ${report.snapshot.totalSpent.asCurrency()}",
            "Remaining: ${report.snapshot.remainingBudget.asCurrency()}",
            "Daily Allowance: ${report.snapshot.dailyAllowance.asCurrency()}",
        ).forEach { line ->
            canvas.drawText(line, 32f, y, bodyPaint)
            y += 22f
        }

        y += 18f
        canvas.drawText("Transactions", 32f, y, titlePaint)
        y += 28f

        report.transactions.take(22).forEach { transaction ->
            val line = "${transaction.timestamp.toDisplayDateTime()}  ${transaction.categoryName}  ${transaction.amount.asCurrency()}"
            canvas.drawText(line.take(72), 32f, y, bodyPaint)
            y += 18f
            if (transaction.note.isNotBlank()) {
                canvas.drawText("Note: ${transaction.note}".take(78), 48f, y, bodyPaint)
                y += 18f
            }
        }

        pdfDocument.finishPage(page)

        val file = createFile("budget-summary-${report.monthLabel.slugify()}.pdf")
        FileOutputStream(file).use { output ->
            pdfDocument.writeTo(output)
        }
        pdfDocument.close()
        return file.toContentUri()
    }

    private fun createFile(name: String): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        return File(exportDir, name)
    }

    private fun File.toContentUri(): android.net.Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            this,
        )
    }

    private fun String.slugify(): String {
        return lowercase(Locale.getDefault())
            .replace(" ", "-")
            .replace(Regex("[^a-z0-9-]"), "")
    }

    private fun String.csvSafe(): String {
        val escaped = replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
