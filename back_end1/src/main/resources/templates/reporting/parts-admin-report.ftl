<!DOCTYPE html>
<html lang="uk">
<head>
    <meta charset="UTF-8"/>
    <title>${report.reportTitle()}</title>
    <style>
        <#assign generatedAt = report.generatedAtFormatted() />
        <#include "styles.ftl" />
    </style>
</head>
<body>
    <div class="doc-header">
        <div class="doc-title">${report.reportTitle()}</div>
        <div>Сформовано: <strong>${report.generatedAtFormatted()}</strong> | Часова зона: <strong>${report.zoneId()}</strong></div>
    </div>

    <table class="meta-table">
        <tr>
            <td><strong>Кількість позицій у звіті:</strong> ${report.totalItems()}</td>
            <td><strong>Ліміт конфігурації:</strong> ${report.maxAllowedItems()} позицій</td>
        </tr>
    </table>

    <table class="report-table">
        <thead>
            <tr>
                <th>#</th>
                <th>ID</th>
                <th>SKU</th>
                <th>Найменування позиції</th>
                <th class="num">К-сть</th>
                <th class="num">Ціна</th>
                <th class="num">Знижка %</th>
                <th class="num">Сума нетто</th>
            </tr>
        </thead>
        <tbody>
            <#list report.rows() as r>
                <tr>
                    <td>${r.index()}</td>
                    <td>${r.id()}</td>
                    <td>${r.sku()!""}</td>
                    <td>${r.name()!""}</td>
                    <td class="num">${r.quantity()}</td>
                    <td class="num">${r.unitPrice()}</td>
                    <td class="num">${r.discountPercent()}%</td>
                    <td class="num">${r.netTotal()}</td>
                </tr>
            </#list>
        </tbody>
    </table>

    <table class="summary-box">
        <tr>
            <td>Загальна кількість одиниць:</td>
            <td class="num">${report.totalQuantity()}</td>
        </tr>
        <tr>
            <td>Разом нетто:</td>
            <td class="num">${report.totalNetAmount()}</td>
        </tr>
        <tr>
            <td>ПДВ:</td>
            <td class="num">${report.totalVatAmount()}</td>
        </tr>
        <tr class="summary-total">
            <td>Разом брутто:</td>
            <td class="num">${report.totalGrossAmount()}</td>
        </tr>
    </table>
</body>
</html>