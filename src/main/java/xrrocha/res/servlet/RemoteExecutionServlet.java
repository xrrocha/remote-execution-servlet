package xrrocha.res.servlet;

import xrrocha.res.ReloadableClassExecutor;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
// TODO Restore named class execution after fixing filesystem-based class loader
public class RemoteExecutionServlet extends HttpServlet implements ServletMapper {
    private final Map<String, RequestHandler> handlers = new HashMap<>();

    private static final Set<String> reservedParameterNames =
            new HashSet<>(Arrays.asList(new String[]{"className", "refreshLibraries"}));

    private static final Logger logger = Logger.getLogger(RemoteExecutionServlet.class.getName());

    public RemoteExecutionServlet(/*File classDirectory, File jarDirectory, ClassLoader parentClassLoader*/) {
        //executor = new ReloadableClassExecutor<>(classDirectory, jarDirectory, parentClassLoader);
    }

    @Override
    public void init(ServletConfig servletConfig) {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Initializing servlet");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Servicing path: " + path);

        try {
            if (path == null || path.length() == 1) {
                logger.warning("Won't handle empty path");
                //execute(request, response);
            } else {
                handle(path.substring(1), request, response);
            }
        } catch (Exception e) {
            String errorMessage = "Error servicing request " + (path == null ? "" : path) + ": " + e;
            logger.warning(errorMessage);
            response.getWriter().println(errorMessage);
        }

        response.getWriter().flush();
    }

    private void handle(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RequestHandler handler = handlers.get(path);
        if (handler == null) {
            logger.warning("No suitable handler for " + path);
        } else {
            handler.handle(request, response);
        }
    }

    @Override
    public void addMapping(String path, RequestHandler handler) {
        handlers.put(path, handler);
    }

    @Override
    public void removeMapping(String path) {
        handlers.remove(path);
    }

    private static Map<String, Object> buildParameters(Map<String, String[]> parameterMap) {
        Map<String, Object> parameters = new HashMap<>();
        for (String parameterName : parameterMap.keySet()) {
            if (!reservedParameterNames.contains(parameterName)) {
                parameters.put(parameterName, parameterMap.get(parameterName)[0]);
            }
        }
        return parameters;
    }

    @SuppressWarnings("unused")
    private /*final*/ ReloadableClassExecutor<ServletExecutionContext> executor;

    @SuppressWarnings("unused")
    private void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (Boolean.valueOf(request.getParameter("scanLibraries"))) {
            executor.scanLibraries();
        }

        String className = request.getParameter("className");
        if (className != null) {
            Map<String, Object> parameters = buildParameters(request.getParameterMap());
            ServletExecutionContext context =
                    new ServletExecutionContext(request.getServletContext(), this, response.getWriter());
            executor.execute(className, parameters, context);
        }
    }
}
