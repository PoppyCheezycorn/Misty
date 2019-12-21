package sh.niall.misty.utils.errors;

import sh.niall.yui.commands.errors.CommandError;

public class PermissionError extends CommandError {
    public PermissionError(String message) {
        super(message);
    }
}
