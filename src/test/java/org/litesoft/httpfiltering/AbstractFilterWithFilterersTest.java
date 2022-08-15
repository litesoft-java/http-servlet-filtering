package org.litesoft.httpfiltering;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AbstractFilterWithFilterersTest {
    private static final String A_OK = "A OK!";
    private static final String B_BAD = "B NOT OK!";

    private final List<Character> filtersCalled = new ArrayList<>();

    private final Filterer filtererA = request -> {
        filtersCalled.add( 'A' );
        return !"/A".equals( request.getServletPath() ) ? null :
               Filterer.FilteredResponse.ofOK( A_OK );
    };

    private final Filterer filtererB = request -> {
        filtersCalled.add( 'B' );
        return !"/B".equals( request.getServletPath() ) ? null :
               Filterer.FilteredResponse.ofError( 400, B_BAD );
    };

    private ServletRequest chainCalledRequest = null;
    private ServletResponse chainCalledResponse = null;

    private final FilterChain chain = ( request, response ) -> {
        chainCalledRequest = request;
        chainCalledResponse = response;
    };

    private final HttpServletRequest request = Mockito.mock( HttpServletRequest.class );
    private final MyHttpServletResponse response = new MyHttpServletResponse();

    @Test
    void noFilters()
            throws ServletException, IOException {
        new Filter().doFilter( request, response, chain );
        assertChainCalled();
        assertFiltersCalled( "" );
    }

    @Test
    void filtersAB_noMatch()
            throws ServletException, IOException {
        when( request.getServletPath() ).thenReturn( "/NotAorB" );
        new Filter( filtererA, filtererB ).doFilter( request, response, chain );
        assertChainCalled();
        assertFiltersCalled( "AB" );
    }

    @Test
    void filtersAB_matchA()
            throws ServletException, IOException {
        setupMatchingCall( "/A", 200, A_OK );
        assertFiltersCalled( "A" );
    }

    @Test
    void filtersAB_matchB()
            throws ServletException, IOException {
        setupMatchingCall( "/B", 400, B_BAD );
        assertFiltersCalled( "AB" );
    }

    private void setupMatchingCall( String servletPath, int expectedStatus, String expectedBody )
            throws IOException, ServletException {
        when( request.getServletPath() ).thenReturn( servletPath );
        new Filter( filtererA, filtererB ).doFilter( request, response, chain );
        assertChainNotCalled();
        assertEquals( expectedStatus, response.status );
        assertEquals( expectedBody, response.msg );
    }

    private void assertChainCalled() {
        assertSame( request, chainCalledRequest );
        assertSame( response, chainCalledResponse );
    }

    private void assertChainNotCalled() {
        assertNull( chainCalledRequest );
        assertNull( chainCalledResponse );
    }

    private void assertFiltersCalled( String calledFilterChars ) {
        StringBuilder sb = new StringBuilder();
        for ( Character c : filtersCalled ) {
            sb.append( c );
        }
        assertEquals( calledFilterChars, sb.toString() );
    }

    private static class Filter extends AbstractFilterWithFilterers {
        public Filter( Filterer... filterers ) {
            super( filterers );
        }
    }

    private static class MyHttpServletResponse extends HttpServletResponseWrapper {
        private Integer status;
        private String msg;

        public MyHttpServletResponse() {
            super( mockResponse );
        }

        @Override
        public void sendError( int status, String msg ) {
            this.status = status;
            this.msg = msg;
        }

        @Override
        public void setStatus( int status ) {
            this.status = status;
        }

        @Override
        public PrintWriter getWriter() {
            return new PrintWriter( new MyStringWriter() );
        }

        class MyStringWriter extends StringWriter {
            @Override
            public void close()
                    throws IOException {
                super.close();
                msg = toString();
            }
        }
    }

    private static final HttpServletResponse mockResponse = Mockito.mock( HttpServletResponse.class );
}