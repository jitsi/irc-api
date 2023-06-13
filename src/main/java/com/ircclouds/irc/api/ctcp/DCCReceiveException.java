package com.ircclouds.irc.api.ctcp;

import java.io.*;

public class DCCReceiveException {
    private final DCCReceiveResult result;

    private final IOException exc;

    public DCCReceiveException(DCCReceiveResult aResult, IOException aException) {
        result = aResult;
        exc = aException;
    }

    public IOException getException() {
        return exc;
    }

    public DCCReceiveResult getDCCReceiveResult() {
        return result;
    }
}
