grammar http_request;

http_request
    : request_line '\r\n' (header)* (body)?
    ;

request_line
    : WORD
    ;

header
    : WORD ':' WORD
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