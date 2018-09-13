package ru.saidgadjiev.prototype.request.lexer;

/**
 * Created by said on 12.09.2018.
 */
public class HttpRequestLexer {

    private final String input;

    private static final char EOF = (char) -1;

    private int p = 0;

    private char ch;

    private State state = State.METHOD;

    public HttpRequestLexer(String input) {
        this.input = input;

        ch = input.charAt(p);
    }

    public Token nextToken() {
        while (ch != EOF) {
            switch (state) {
                case METHOD: {
                    state = State.URI;

                    Token result = method();

                    //Поглащаем ' '
                    consume();

                    return result;
                }
                case URI: {
                    state = State.HTTP_VERSION;

                    Token result = uri();

                    //Поглащаем ' '
                    consume();

                    return result;
                }
                case HTTP_VERSION: {
                    Token result = httpVersion();

                    //Поглащаем \n
                    consume();
                    //Следующий символ
                    consume();

                    //Если следующий символ возврат коретки значит мы дошли до body. Иначе header.
                    if (ch == '\r') {
                        //Поглащаем \n
                        //Следующий символ
                        consume();
                        consume();

                        state = State.BODY;
                    } else {
                        state = State.HEADER;
                    }

                    return result;
                }
                case HEADER: {
                    Token result = header();

                    //Поглащаем \n
                    consume();
                    //Следующий символ
                    consume();

                    //Если следующий символ возврат коретки значит мы дошли до body. Иначе остаемся на header.
                    if (ch == '\r') {
                        //Поглащаем \n
                        consume();
                        //Следующий символ
                        consume();

                        state = State.BODY;
                    }

                    return result;
                }
                case BODY: {
                    return body();
                }
            }
        }

        return new Token("</EOF>", Token.TokenType.EOF);
    }

    private Token method() {
        StringBuilder builder = new StringBuilder();

        do {
            builder.append(ch);
            consume();
        } while (ch != ' ' && ch != EOF);

        return new Token(builder.toString(), Token.TokenType.METHOD);
    }

    private Token uri() {
        StringBuilder builder = new StringBuilder();

        do {
            builder.append(ch);
            consume();
        } while (ch != ' ' && ch != EOF);

        return new Token(builder.toString(), Token.TokenType.URI);
    }

    private Token httpVersion() {
        StringBuilder builder = new StringBuilder();

        do {
            builder.append(ch);
            consume();
        } while (ch != '\r' && ch != EOF);

        return new Token(builder.toString(), Token.TokenType.HTTP_VERSION);
    }

    private Token header() {
        StringBuilder builder = new StringBuilder();

        do {
            builder.append(ch);
            consume();
        } while (ch != '\r' && ch != EOF);

        return new Token(builder.toString(), Token.TokenType.HEADER);
    }

    private Token body() {
        StringBuilder builder = new StringBuilder();

        do {
            builder.append(ch);
            consume();
        } while (ch != EOF);

        return new Token(builder.toString(), Token.TokenType.BODY);
    }

    private void consume() {
        ++p;

        if (p >= input.length()) {
            ch = EOF;
        } else {
            ch = input.charAt(p);
        }
    }

    public static void main(String[] args) {
        String request = "GET /api/user HTTP/1.1\r\n" +
                "User: said\r\n" +
                "Framework: Prototype\r\n" +
                "\r\n" +
                "test";

        HttpRequestLexer lexer = new HttpRequestLexer(request);

        Token token = lexer.nextToken();

        while (token.getTokenType() != Token.TokenType.EOF) {
            System.out.println(token);

            token = lexer.nextToken();
        }

        System.out.println(token);
    }
}
