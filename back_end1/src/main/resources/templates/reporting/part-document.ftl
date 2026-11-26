<!DOCTYPE html>
<html lang="uk">
<head>
    <meta charset="UTF-8"/>
    <title>${doc.documentTitle()}</title>
    <style>
        <#assign generatedAt = doc.generatedAtFormatted() />
        <#include "styles.ftl" />
    </style>
</head>
<body>
    <div class="doc-header">
        <div class="doc-title">${doc.documentTitle()}</div>
        <div>Номер документа: <strong>${doc.documentNumber()}</strong></div>
    </div>

    <table class="meta-table">
        <tr>
            <td><strong>Дата та час формування:</strong> ${doc.generatedAtFormatted()}</td>
            <td><strong>Часова зона:</strong> ${doc.zoneId()}</td>
        </tr>
        <tr>
            <td><strong>Підстава / примітка:</strong> ${doc.customerReference()}</td>
            <td><strong>Автор запису:</strong> ${doc.createdBy()}</td>
        </tr>
    </table>

    <table class="report-table">
        <thead>
            <tr>
                <th>#</th>
                <th>SKU</th>
                <th>Найменування</th>
                <th class="num">К-сть</th>
                <th class="num">Ціна</th>
                <th class="num">Знижка</th>
                <th class="num">Сума з ПДВ / рядку</th>
            </tr>
        </thead>
        <tbody>
            <#list doc.lines() as line>
                <tr>
                    <td>${line.lineNumber()}</td>
                    <td>${line.sku()!""}</td>
                    <td>${line.name()!""}</td>
                    <td class="num">${line.quantity()}</td>
                    <td class="num">${line.unitPrice()}</td>
                    <td class="num">${line.discountPercent()}% (${line.discountAmount()})</td>
                    <td class="num">${line.lineTotalAfterDiscount()}</td>
                </tr>
            </#list>
        </tbody>
    </table>

    <table class="summary-box">
        <tr>
            <td>Сума до знижки:</td>
            <td class="num">${doc.subtotalBeforeDiscount()}</td>
        </tr>
        <tr>
            <td>Знижка:</td>
            <td class="num">-${doc.totalDiscount()}</td>
        </tr>
        <tr>
            <td>Сума після знижки:</td>
            <td class="num">${doc.subtotalAfterDiscount()}</td>
        </tr>
        <tr>
            <td>ПДВ (${doc.vatRatePercent()}%):</td>
            <td class="num">${doc.vatAmount()}</td>
        </tr>
        <tr class="summary-total">
            <td>Разом до сплати:</td>
            <td class="num">${doc.grandTotal()}</td>
        </tr>
    </table>
</body>
</html>