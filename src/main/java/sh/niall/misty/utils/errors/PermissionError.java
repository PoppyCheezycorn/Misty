package sh.niall.misty.utils.errors;

import sh.niall.yui.exceptions.CommandException;

public class PermissionError extends CommandException {
    public PermissionError(String message) {
        super(message);
    }
}
