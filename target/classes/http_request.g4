grammar http_request;

http_request
    : request_line header (header)*
    ;

request_line
    : METHOD ' ' WORD ' ' HTTP_VERSION
    ;

header
    : WORD ':' ' ' WORD
    ;

METHOD
    : 'GET'
    | 'POST'
    ;

WORD
    : '*'
    ;

HTTP_VERSION
    : 'HTTP/1.1'
    ;