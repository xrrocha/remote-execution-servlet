package xrrocha.rex.servlet;

import javax.servlet.ServletContext;
import java.io.PrintWriter;

public class ServletExecutionContext {
    private final ServletContext servletContext;
    private final ServletMapper servletMapper;
    private final PrintWriter out;

    public ServletExecutionContext(ServletContext servletContext, ServletMapper servletMapper, PrintWriter out) {
        this.servletContext = servletContext;
        this.servletMapper = servletMapper;
        this.out = out;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public ServletMapper getServletMapper() {
        return servletMapper;
    }

    public PrintWriter getOut() {
        return out;
    }
}
