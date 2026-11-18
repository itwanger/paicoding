package com.github.paicoding.forum.api.model.vo.table;

import lombok.Data;
import java.util.List;

/** Snapshot of article rows already delivered to the reader; never loads gated content. */
@Data
public class TableLearningReq {
    private String title;
    private List<String> columns;
    private List<String> values;
    private String action;
    private String question;
    private List<List<String>> rows;
    private List<Integer> visibleRows;
    private List<Integer> visibleColumns;
}
