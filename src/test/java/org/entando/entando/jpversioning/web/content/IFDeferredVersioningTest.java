package org.entando.entando.jpversioning.web.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agiletec.plugins.jpversioning.aps.system.services.versioning.IFDeferredVersioning;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

    @Test
    void possiblyDeferred_exception_enabled() throws Exception {
        Executor executorMock = mock(Executor.class);
        Supplier<String> actionMock = mock(Supplier.class);
        String methodName = "saveContent";
        RuntimeException exception = new RuntimeException("Test Exception");

        when(actionMock.get()).thenThrow(exception);

        // Simulo l'esecuzione immediata nel thread corrente quando viene chiamato l'executor
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executorMock).execute(any(Runnable.class));

        try (MockedStatic<IFDeferredVersioning> mock = mockStatic(IFDeferredVersioning.class)) {
            mock.when(IFDeferredVersioning::checkEnabled).thenReturn(true);
            mock.when(() -> IFDeferredVersioning.possiblyDeferred(
                    any(Executor.class),
                    any(Supplier.class),
                    anyString()
            )).thenCallRealMethod();

            CompletableFuture<Void> result = IFDeferredVersioning.possiblyDeferred(executorMock, actionMock, methodName);

            assertNotNull(result);
            assertTrue(result.isCompletedExceptionally());

            ExecutionException ex = assertThrows(ExecutionException.class, result::get);
            assertEquals(exception, ex.getCause());
        }
    }

    @Test
    void possiblyDeferred_exception_disabled() throws Exception {
        Executor executorMock = mock(Executor.class);
        Supplier<String> actionMock = mock(Supplier.class);
        String methodName = "saveContent";
        RuntimeException exception = new RuntimeException("Test Exception");

        when(actionMock.get()).thenThrow(exception);

        try (MockedStatic<IFDeferredVersioning> mocked = mockStatic(IFDeferredVersioning.class)) {
            mocked.when(IFDeferredVersioning::checkEnabled).thenReturn(false);
            mocked.when(() -> IFDeferredVersioning.possiblyDeferred(
                    any(Executor.class),
                    any(Supplier.class),
                    anyString())
            ).thenCallRealMethod();

            CompletableFuture<Void> result = IFDeferredVersioning.possiblyDeferred(executorMock, actionMock, methodName);

            assertNotNull(result);
            assertTrue(result.isCompletedExceptionally());

            ExecutionException ex = assertThrows(ExecutionException.class, result::get);
            assertEquals(exception, ex.getCause());
        }
    }

}
