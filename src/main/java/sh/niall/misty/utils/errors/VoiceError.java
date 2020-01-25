package sh.niall.misty.utils.errors;

import sh.niall.yui.exceptions.YuiException;

public class VoiceError extends YuiException {
    public VoiceError(String message) {
        super(message);
    }
}
