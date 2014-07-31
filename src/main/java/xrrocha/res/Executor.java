package xrrocha.res;

import java.util.Map;

public interface Executor<C> {
    void execute(String executableName, Map<String, Object> parameters, C context) throws Exception;
}
