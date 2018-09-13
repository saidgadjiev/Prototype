package ru.saidgadjiev.prototype.request.lexer;

/**
 * Created by said on 13.09.2018.
 */
public class Token {

    private final String value;

    private final TokenType tokenType;

    public Token(String value, TokenType tokenType) {
        this.value = value;
        this.tokenType = tokenType;
    }

    public String getValue() {
        return value;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + value + '\'' +
                ", tokenType=" + tokenType +
                '}';
    }

    public enum TokenType {

        METHOD,

        URI,

        HTTP_VERSION,

        HEADER,

        BODY,

        EOF
    }
}
