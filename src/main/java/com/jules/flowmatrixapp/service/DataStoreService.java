package com.jules.flowmatrixapp.service;

import com.jules.flowmatrixapp.model.FlowRecord;
import com.jules.flowmatrixapp.model.diff.DiffRow;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@SessionScope // Each user session gets its own data store
@Getter
public class DataStoreService implements Serializable {

    private List<FlowRecord> flowRecords = Collections.emptyList();
    private List<DiffRow> diffRows = Collections.emptyList();

    public void setFlowRecords(List<FlowRecord> records) {
        // Use thread-safe list for potential concurrent reads/writes
        this.flowRecords = new CopyOnWriteArrayList<>(records);
    }

    public void setDiffRows(List<DiffRow> diffs) {
        this.diffRows = new CopyOnWriteArrayList<>(diffs);
    }

    public void clearFlows() {
        this.flowRecords = Collections.emptyList();
    }

    public void clearDiffs() {
        this.diffRows = Collections.emptyList();
    }

    public boolean hasData() {
        return this.flowRecords != null && !this.flowRecords.isEmpty();
    }

    public boolean hasDiffData() {
        return this.diffRows != null && !this.diffRows.isEmpty();
    }
}
