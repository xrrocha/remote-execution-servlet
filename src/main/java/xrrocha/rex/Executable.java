package xrrocha.rex;

import java.util.Map;

public interface Executable<C> {
    void execute(Map<String, Object> parameters, C context) throws Exception;
}
