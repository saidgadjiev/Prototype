package ru.saidgadjiev.prototype.http.parser;

import ru.saidgadjiev.prototype.http.HttpRequest;
import ru.saidgadjiev.prototype.http.lexer.HttpRequestLexer;
import ru.saidgadjiev.prototype.http.lexer.Token;

import java.util.Arrays;

/**
 * Created by said on 12.09.2018.
 */
public class HttpRequestParser {

    private Token lookahead;

    private final HttpRequestLexer lexer1;

    private final HttpRequest request = new HttpRequest();

    public HttpRequestParser(HttpRequestLexer lexer1) {
        this.lexer1 = lexer1;

        consume();
    }

    public HttpRequest parse() {
        requestPart();

        match(Token.TokenType.CARRIAGE_RETURN);
        match(Token.TokenType.NEW_LINE);

        header();

        while (check(Token.TokenType.CARRIAGE_RETURN) && check(Token.TokenType.NEW_LINE) && lookahead.getTokenType() == Token.TokenType.WORD) {
            header();
        }

        match(Token.TokenType.CARRIAGE_RETURN);
        match(Token.TokenType.NEW_LINE);

        body();

        return request;
    }

    private void requestPart() {
        method();

        match(Token.TokenType.SPACE);

        uri();

        match(Token.TokenType.SPACE);

        httpVersion();
    }

    private void header() {
        if (lookahead.getTokenType() == Token.TokenType.WORD) {
            StringBuilder headerName = new StringBuilder();

            do {
                headerName.append(lookahead.getValue());
                consume();
            } while (lookahead.getTokenType() != Token.TokenType.COLON && lookahead.getTokenType() != Token.TokenType.EQ && lookahead.getTokenType() != Token.TokenType.EOF);

            match(Token.TokenType.COLON);
            match(Token.TokenType.SPACE);

            StringBuilder headerValue = new StringBuilder();

            do {
                headerValue.append(lookahead.getValue());
                consume();
            } while (lookahead.getTokenType() != Token.TokenType.CARRIAGE_RETURN && lookahead.getTokenType() != Token.TokenType.EOF);

            request.addHeader(headerName.toString(), headerValue.toString());
        }
    }

    private void body() {
        if (lookahead.getTokenType() == Token.TokenType.WORD) {
            String contentType = request.getHeader("Content-Type");

            if (contentType.equals("application/x-www-form-urlencoded")) {
                queryParameters();
            } else if (contentType.contains("multipart/")) {
                String boundary = contentType.substring(contentType.indexOf("boundary"));

                System.out.println(boundary);
            } else {
                StringBuilder body = new StringBuilder();

                do {
                    body.append(lookahead.getValue());
                    consume();
                } while (lookahead.getTokenType() != Token.TokenType.EOF);

                request.setBody(body.toString());
            }
        }
    }

    private void httpVersion() {
        if (lookahead.getTokenType() == Token.TokenType.WORD) {
            request.setHttpVersion(lookahead.getValue());
            consume();
        } else {
            throw new Error("Expected word but found " + lookahead.getValue());
        }
    }

    private void method() {
        if (check(Token.TokenType.WORD, "GET")) {
            request.setMethod(HttpRequest.Method.GET);
        } else if (check(Token.TokenType.WORD, "POST")) {
            request.setMethod(HttpRequest.Method.POST);
        } else {
            throw new Error("Expected " + Arrays.toString(HttpRequest.Method.values()) + " but found " + lookahead.getValue());
        }
    }

    private void uri() {
        if (lookahead.getTokenType() == Token.TokenType.WORD) {
            request.setUri(lookahead.getValue());

            consume();

            if (check(Token.TokenType.QUESTION_MARK)) {
                queryParameters();
            }
        } else {
            throw new Error("Expected word but found " + lookahead.getValue());
        }
    }

    private void queryParameters() {
        queryParameter();

        while (check(Token.TokenType.AMPERSAND)) {
            queryParameter();
        }
    }

    private void queryParameter() {
        if (lookahead.getTokenType() == Token.TokenType.WORD) {
            String queryParamName = lookahead.getValue();
            String queryParamValue = null;

            consume();
            if (check(Token.TokenType.EQ)) {
                queryParamValue = lookahead.getValue();
                consume();
            }

            request.addParam(queryParamName, queryParamValue);
        }
    }

    private void match(Token.TokenType tokenType, String value) {
        if (lookahead.getTokenType() == tokenType && lookahead.getValue().equals(value)) {
            consume();
        } else {
            throw new Error("expecting " + tokenType +
                    "; found " + lookahead);
        }
    }

    private void match(Token.TokenType tokenType) {
        if (lookahead.getTokenType() == tokenType) {
            consume();
        } else {
            throw new Error("expecting " + tokenType +
                    "; found " + lookahead);
        }
    }

    private boolean check(Token.TokenType tokenType, String value) {
        if (lookahead.getTokenType() == tokenType && lookahead.getValue().equals(value)) {
            consume();

            return true;
        }

        return false;
    }

    private boolean check(Token.TokenType tokenType) {
        if (lookahead.getTokenType() == tokenType) {
            consume();

            return true;
        }

        return false;
    }

    private void consume() {
        lookahead = lexer1.nextToken();
    }
}
