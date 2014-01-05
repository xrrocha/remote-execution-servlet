package org.plenix.rex.servlet;

import org.plenix.rex.ExecutionContext;

import javax.servlet.ServletContext;
import java.io.PrintWriter;

public class ServletExecutionContext implements ExecutionContext {
    private ServletContext servletContext;
    private ServletMapper servletMapper;
    private PrintWriter out;

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
