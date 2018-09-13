package ru.saidgadjiev.prototype.request.lexer;

/**
 * Created by said on 12.09.2018.
 */
public enum  State {

    METHOD,

    URI,

    QUERY_PARAMETER,

    HTTP_VERSION,

    HEADER,

    BODY

}
