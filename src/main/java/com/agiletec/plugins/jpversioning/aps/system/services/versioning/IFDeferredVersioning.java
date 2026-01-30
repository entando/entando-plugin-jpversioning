package com.agiletec.plugins.jpversioning.aps.system.services.versioning;

import com.agiletec.aps.system.ApsSystemUtils.ApsDeepDebug;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.entando.entando.aps.system.services.IFeatureFlag;

public interface IFDeferredVersioning extends IFeatureFlag {
    boolean DEFERRED_VERSIONING_ENABLED = checkEnabled();

    default boolean isEnabled() {
        return DEFERRED_VERSIONING_ENABLED;
    }

    static boolean checkEnabled() {
        return IFeatureFlag.readEnablementStatus("DEFERRED_VERSIONING");
    }

    static void possiblyDeferred(Executor executor, Supplier<String> action, String method) {
        if (checkEnabled()) {
            ApsDeepDebug.print("deferred-versioning", "running versioning task in a separate thread: " + method);
            CompletableFuture.runAsync(action::get, executor);
        } else {
            action.get();
        }
    }

}
