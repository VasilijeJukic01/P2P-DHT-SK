package com.kids.file;

import com.kids.app.ChordState;
import com.kids.app.servent.ServentIdentity;

import java.io.Serial;
import java.io.Serializable;


public record FileData(
        String path,
        ServentIdentity serventIdentity
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 782345119237880561L;

    @Override
    public int hashCode() {
        return ChordState.chordHash(path());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FileData) return o.hashCode() == this.hashCode();
        return false;
    }

}
