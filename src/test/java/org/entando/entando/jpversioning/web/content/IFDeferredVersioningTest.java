package org.entando.entando.jpversioning.web.content;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.agiletec.plugins.jpversioning.aps.system.services.versioning.IFDeferredVersioning;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.entando.entando.web.AbstractControllerIntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class IFDeferredVersioningTest extends AbstractControllerIntegrationTest {


    @Test
    void possiblyDeferred_enabled() throws Exception {

        Executor executorMock = mock(Executor.class);
        Supplier<String> actionMock = mock(Supplier.class);
        String methodName = "saveContent";

        try (MockedStatic<IFDeferredVersioning> mock = mockStatic(IFDeferredVersioning.class)) {
            mock.when(IFDeferredVersioning::checkEnabled).thenReturn(true);
            mock.when(() -> IFDeferredVersioning.possiblyDeferred(
                    any(Executor.class),
                    any(Supplier.class),
                    anyString()
            )).thenCallRealMethod();

            IFDeferredVersioning.possiblyDeferred(executorMock, actionMock, methodName);
            // the executor gets called!!!
            verify(executorMock, times(1)).execute(any(Runnable.class));
        }
    }

    @Test
    void possiblyDeferred_disabled() {
        Executor executorMock = mock(Executor.class);
        Supplier<String> actionMock = mock(Supplier.class);
        String methodName = "saveContent";

        try (MockedStatic<IFDeferredVersioning> mocked = mockStatic(IFDeferredVersioning.class)) {
            // Feature flag disabled
            mocked.when(IFDeferredVersioning::checkEnabled).thenReturn(false);

            mocked.when(() -> IFDeferredVersioning.possiblyDeferred(
                    any(Executor.class),
                    any(Supplier.class),
                    anyString())
            ).thenCallRealMethod();

            IFDeferredVersioning.possiblyDeferred(executorMock, actionMock, methodName);

            verify(actionMock, times(1)).get();
            // Executor never gets called!!!
            verify(executorMock, never()).execute(any());
        }
    }

}
