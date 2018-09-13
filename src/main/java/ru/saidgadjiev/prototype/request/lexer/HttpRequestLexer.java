package ru.saidgadjiev.prototype.request.lexer;

import ru.saidgadjiev.prototype.request.parser.HttpRequestParser;

/**
 * Created by said on 12.09.2018.
 */
public class HttpRequestLexer {

    private final String input;

    private static final char EOF = (char) -1;

    private int p = 0;

    private char ch;

    public HttpRequestLexer(String input) {
        this.input = input;

        ch = input.charAt(p);
    }

    public Token nextToken() {
        while (ch != EOF) {
            switch (ch) {
                case ' ':
                    consume();

                    return new Token(" ", Token.TokenType.SPACE);
                case '\r':
                    consume();

                    return new Token("\r", Token.TokenType.CARRIAGE_RETURN);
                case '\n':
                    consume();

                    return new Token("\n", Token.TokenType.NEW_LINE);
                case ':':
                    consume();

                    return new Token(":", Token.TokenType.COLON);
                case '?':
                    consume();

                    return new Token("?", Token.TokenType.QUESTION_MARK);
                case '&':
                    consume();

                    return new Token("&", Token.TokenType.AMPERSAND);
                case '=':
                    consume();

                    return new Token("=", Token.TokenType.EQ);
                default:
                    return word();
            }
        }

        return new Token("</EOF>", Token.TokenType.EOF);
    }

    private Token word() {
        StringBuilder builder = new StringBuilder();

        do {
            builder.append(ch);
            consume();
        } while (!isDelimiter() && ch != EOF);

        return new Token(builder.toString(), Token.TokenType.WORD);
    }

    private void consume() {
        ++p;

        if (p >= input.length()) {
            ch = EOF;
        } else {
            ch = input.charAt(p);
        }
    }

    private boolean isDelimiter() {
        return ch == ' ' || ch == '\r' || ch == '\n' || ch == ':' || ch == '?' || ch == '&' || ch == '=';
    }

    public static void main(String[] args) {
        String request = "GET /api/user?name=said&test=1 HTTP/1.1\r\n" +
                "User name: said\r\n" +
                "Web framework: Prototype\r\n" +
                "\r\n" +
                "test {a:5}";

        HttpRequestLexer lexer = new HttpRequestLexer(request);

        HttpRequest request1 = new HttpRequestParser(lexer).parse();

        System.out.println(request1);
    }
}
