package com.aboni.nmea.router.n2k;

import javax.validation.constraints.NotNull;

public interface N2KFastCache {
    void setCallback(N2KMessageCallback callback);

    void onMessage(@NotNull N2KMessage msg);

    void cleanUp();
}
