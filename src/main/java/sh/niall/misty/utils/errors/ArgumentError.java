package sh.niall.misty.utils.errors;

import sh.niall.yui.exceptions.CommandException;

public class ArgumentError extends CommandException {
    public ArgumentError(String message) {
        super(message);
    }
}
