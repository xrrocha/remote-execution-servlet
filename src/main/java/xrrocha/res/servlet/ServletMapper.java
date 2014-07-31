package xrrocha.res.servlet;

public interface ServletMapper {
    void addMapping(String path, RequestHandler handler);

    void removeMapping(String path);
}
