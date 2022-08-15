package org.litesoft.httpfiltering;

import javax.servlet.http.HttpServletRequest;

public interface Filterer {
    /**
     * Check the Request, and process appropriately.
     *
     * @param request to check
     * @return !null means this request is Filtered (and no subsequent processing is needed, except returning the <code>FilteredResponse</code>); null means continue with normal filtering / processing!.
     */
    FilteredResponse processRequest( HttpServletRequest request );

    @SuppressWarnings("unused")
    class FilteredResponse {
        private final Integer errorStatus;
        private final String body;

        private FilteredResponse( Integer errorStatus, String body ) {
            this.errorStatus = errorStatus;
            this.body = body;
        }

        public boolean hasError() {
            return (errorStatus != null);
        }

        public Integer getErrorStatus() {
            return errorStatus;
        }

        public String getBody() {
            return body;
        }

        @Override
        public String toString() {
            return "Response(" + ((errorStatus == null) ? 200 : errorStatus) + "): " + body;
        }

        public static FilteredResponse ofError( int errorStatus, String body ) {
            return new FilteredResponse( errorStatus, body );
        }

        public static FilteredResponse ofOK( String body ) {
            return new FilteredResponse( null, body );
        }
    }
}
