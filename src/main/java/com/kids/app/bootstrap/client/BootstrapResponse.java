package com.kids.app.bootstrap.client;

import com.kids.app.servent.ServentIdentity;

import java.util.List;

public record BootstrapResponse(
        ServentIdentity someServentInfo,
        List<ServentIdentity> nodes
) {
}
