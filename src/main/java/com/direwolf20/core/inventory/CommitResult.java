package com.direwolf20.core.inventory;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

public final class CommitResult {
    public static final CommitResult NO_ACTION = new CommitResult(false, ImmutableMultiset.of(), ImmutableMultiset.of());
    private boolean activeCommit;
    private Multiset<IndexKey> missing;
    private Multiset<IndexKey> notInserted;

    public CommitResult(boolean activeCommit, Multiset<IndexKey> missing, Multiset<IndexKey> notInserted) {
        this.activeCommit = activeCommit;
        this.missing = missing;
        this.notInserted = notInserted;
    }

    public boolean isActiveCommit() {
        return activeCommit;
    }

    public boolean isSuccessfulCommit() {
        return isActiveCommit() && getMissing().isEmpty() && getNotInserted().isEmpty();
    }

    public boolean isFailedCommit() {
        return isActiveCommit() && (! getMissing().isEmpty() || ! getNotInserted().isEmpty());
    }

    public Multiset<IndexKey> getMissing() {
        return missing;
    }

    public Multiset<IndexKey> getNotInserted() {
        return notInserted;
    }
}
