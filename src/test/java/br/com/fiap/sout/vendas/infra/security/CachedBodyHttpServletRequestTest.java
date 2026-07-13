package br.com.fiap.sout.vendas.infra.security;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CachedBodyHttpServletRequestTest {

    @Test
    void shouldCacheBodySuccessfully() throws IOException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        byte[] body = "test body content".getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(body);

        ServletInputStream sis = new ServletInputStream() {
            @Override
            public boolean isFinished() { return false; }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setReadListener(jakarta.servlet.ReadListener readListener) {}
            @Override
            public int read() { return bais.read(); }
        };

        when(mockRequest.getInputStream()).thenReturn(sis);

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(mockRequest);

        assertArrayEquals(body, cachedRequest.getCachedBody());

        ServletInputStream cachedInputStream = cachedRequest.getInputStream();
        assertNotNull(cachedInputStream);
        assertTrue(cachedInputStream.isReady());

        assertThrows(UnsupportedOperationException.class, () -> cachedInputStream.setReadListener(null));

        // Read and check if finished
        assertFalse(cachedInputStream.isFinished());
        byte[] readBody = cachedInputStream.readAllBytes();
        assertArrayEquals(body, readBody);
        assertTrue(cachedInputStream.isFinished());
    }
}
