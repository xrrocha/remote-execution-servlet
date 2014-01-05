package org.plenix.rex;

import java.util.Map;

public interface Executor {
    void execute(String executableName, Map<String, Object> parameters, ExecutionContext context) throws Exception;
}
