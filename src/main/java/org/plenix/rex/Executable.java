package org.plenix.rex;

import java.util.Map;

public interface Executable<C extends ExecutionContext> {
    void execute(Map<String, Object> parameters, C context) throws Exception;
}
