@page {
    size: A4;
    margin: 22mm 15mm 22mm 15mm;
    @bottom-left {
        content: "Згенеровано: ${generatedAt!""}";
        font-family: "DejaVu Sans", sans-serif;
        font-size: 8pt;
        color: #555555;
    }
    @bottom-right {
        content: "Сторінка " counter(page) " з " counter(pages);
        font-family: "DejaVu Sans", sans-serif;
        font-size: 8pt;
        color: #333333;
    }
}

body {
    font-family: "DejaVu Sans", sans-serif;
    font-size: 10pt;
    color: #1f2937;
    margin: 0;
    padding: 0;
}

.doc-header {
    border-bottom: 2px solid #1e3a8a;
    padding-bottom: 10px;
    margin-bottom: 16px;
}

.doc-title {
    font-size: 16pt;
    font-weight: bold;
    color: #1e3a8a;
    margin: 0 0 6px 0;
}

.meta-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 14px;
    font-size: 9pt;
}

.meta-table td {
    padding: 4px 6px;
    vertical-align: top;
}

table.report-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 8px;
    margin-bottom: 16px;
    font-size: 9pt;
}

table.report-table thead {
    display: table-header-group;
}

table.report-table th {
    background-color: #1e3a8a;
    color: #ffffff;
    font-weight: bold;
    padding: 7px 6px;
    border: 1px solid #1e3a8a;
    text-align: left;
}

table.report-table td {
    padding: 6px;
    border: 1px solid #d1d5db;
    vertical-align: top;
}

table.report-table tr {
    page-break-inside: avoid;
}

.num {
    text-align: right;
    white-space: nowrap;
}

.summary-box {
    width: 55%;
    margin-left: auto;
    border-collapse: collapse;
    font-size: 9.5pt;
}

.summary-box td {
    padding: 5px 8px;
    border-bottom: 1px solid #e5e7eb;
}

.summary-total td {
    font-weight: bold;
    font-size: 11pt;
    color: #1e3a8a;
    border-top: 2px solid #1e3a8a;
    border-bottom: 2px solid #1e3a8a;
    padding-top: 7px;
    padding-bottom: 7px;
}