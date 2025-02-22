package com.ghostchu.quickshop.util.paste.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HTMLTable {
    private static final String TEMPLATE = """           
            <table style="table-layout: fixed;">
              {col}
              {thead}
              {tbody}
            </table>
            """;
    private final int columns;
    private final List<String[]> data = new LinkedList<>();

    private final boolean firstColumnBold;
    private String[] title;

    /**
     * Creates an HTML Table
     *
     * @param maxColumns Table max columns, any more columns will be ignored
     */
    public HTMLTable(int maxColumns) {
        this.columns = maxColumns;
        this.firstColumnBold = false;
    }

    /**
     * Creates an HTML Table
     *
     * @param maxColumns      Table max columns, any more columns will be ignored
     * @param firstColumnBold Whether the first column should be bolded
     */
    public HTMLTable(int maxColumns, boolean firstColumnBold) {
        this.columns = maxColumns;
        this.firstColumnBold = firstColumnBold;
    }

    /**
     * Insert new row to the table
     *
     * @param data The data to insert, any more columns which overflowed than maxColumns will be ignored
     */
    public void insert(@NotNull Object... data) {
        String[] f = new String[columns];
        Arrays.fill(f, "");
        for (int i = 0; i < data.length; i++) {
            Object obj = data[i];
            if (obj == null) {
                obj = "null";
            }
            f[i] = obj.toString();
        }
        if (firstColumnBold) {
            if (!StringUtils.isEmpty(f[0])) {
                f[0] = "<b>" + f[0] + "</b>";
            }
        }
        this.data.add(f);
    }

    /**
     * Render this table to HTML sources
     *
     * @return The HTML sources
     */
    @NotNull
    public String render() {
        String thead = renderHead();
        String tbody = renderBody();
        return TEMPLATE
                .replace("{col}", renderColAttributes())
                .replace("{thead}", thead)
                .replace("{tbody}", tbody);
    }

    @NotNull
    private String renderHead() {
        StringBuilder tdBuilder = new StringBuilder();
        if (title == null || title.length == 0) {
            return "";
        }
        for (String headTitle : title) {
            tdBuilder.append("<th>").append(headTitle).append("</th>");
        }
        return """
                <thead>
                <tr>
                 {th}
                </tr>
                </thead>
                """.replace("{th}", tdBuilder.toString());
    }

    private String renderBody() {
        StringBuilder tdBuilder = new StringBuilder();
        for (String[] line : data) {
            tdBuilder.append("<tr>");
            for (String recordEntry : line) {
                tdBuilder.append("<td>").append(recordEntry).append("</td>");
            }
            tdBuilder.append("</tr>");
        }
        return "<tbody>" + tdBuilder + "</tbody>";
    }

    private String renderColAttributes() {
        return String.format("<col style\"width: %s%%;\">\n", 100 / columns).repeat(columns);
    }

    /**
     * Sets the table title
     *
     * @param title The title, null to clear if already set.
     */
    public void setTableTitle(@Nullable String... title) {
        this.title = title;
    }
}
