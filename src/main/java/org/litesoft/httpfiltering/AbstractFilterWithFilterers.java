package org.litesoft.httpfiltering;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.litesoft.httpfiltering.Filterer.FilteredResponse;

public abstract class AbstractFilterWithFilterers implements Filter {
    private final Filterer[] filterers;

    public AbstractFilterWithFilterers( Filterer... filterers ) {
        this.filterers = (filterers != null) ? filterers : new Filterer[0];
    }

    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse response, FilterChain chain )
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)servletRequest;
        for ( Filterer filterer : filterers ) {
            FilteredResponse fr = filterer.processRequest( request );
            if ( fr != null ) {
                if ( fr.hasError() ) {
                    ((HttpServletResponse)response).sendError( fr.getErrorStatus(), fr.getBody() );
                } else {
                    sendSimpleAnswer( (HttpServletResponse)response, fr.getBody() );
                }
                return;
            }
        }
        chain.doFilter( servletRequest, response );
    }

    protected void sendSimpleAnswer( HttpServletResponse response, String answer )
            throws IOException {
        response.setStatus( 200 );
        PrintWriter writer = response.getWriter();
        writer.print( answer );
        writer.close();
    }
}
