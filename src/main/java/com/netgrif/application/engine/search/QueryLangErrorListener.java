package com.netgrif.application.engine.search;

import lombok.Getter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.List;

@Getter
public class QueryLangErrorListener extends BaseErrorListener {
    List<String> errorMessages = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            throws ParseCancellationException {
        errorMessages.add(underlineError(recognizer, (Token) offendingSymbol, line, charPositionInLine, msg));
    }

    protected String underlineError(Recognizer<?, ?> recognizer, Token offendingToken, int line, int charPositionInLine, String msg) {
        String underlineErrorMsg = msg + "\n";
        int start = offendingToken.getStartIndex();
        int stop = offendingToken.getStopIndex();
        if (start > stop) {
            return underlineErrorMsg;
        }
        CommonTokenStream tokens = (CommonTokenStream) recognizer.getInputStream();
        String input = tokens.getTokenSource().getInputStream().toString();
        String[] lines = input.split("\n");
        String errorLine = lines[line - 1];
        underlineErrorMsg += errorLine + "\n";
        underlineErrorMsg += " ".repeat(charPositionInLine) + "^".repeat(stop - start + 1) + "\n";

        return underlineErrorMsg;
    }
}
