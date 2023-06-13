package com.ircclouds.irc.api.ctcp;

public class DCCSendException {
    private final DCCSendResult result;
    private final Exception readerExc;
    private final Exception writerExc;

    public DCCSendException(DCCSendResult aResult, Exception aReaderException, Exception aWriterException) {
        result = aResult;
        readerExc = aReaderException;
        writerExc = aWriterException;
    }

    public DCCSendResult getDCCSendResult() {
        return result;
    }

    public Exception getReaderException() {
        return readerExc;
    }

    public Exception getWriterException() {
        return writerExc;
    }
}
