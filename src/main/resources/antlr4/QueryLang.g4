grammar QueryLang;

query:  resource delimeter conditions EOF ;

resource: CASE
        | TASK
        | USER
        | PROCESS ;

// resource types
CASE: C A S E | C A S E S ;
TASK: T A S K | T A S K S ;
USER: U S E R | U S E R S ;
PROCESS: P R O C E S S | P R O C E S S E S ;

// delimeter
delimeter: WHERE_DELIMETER | COLON_DELIMETER ;
WHERE_DELIMETER: SPACE W H E R E SPACE ;
COLON_DELIMETER: SPACE? ':' SPACE ;

conditions: orExpression ;

orExpression: andExpression (SPACE OR SPACE andExpression)*;

andExpression: conditionGroup (SPACE AND SPACE conditionGroup)*;

conditionGroup: condition | '(' SPACE? orExpression SPACE? ')' SPACE? ;

condition: (NOT SPACE)? attribute SPACE operator SPACE value SPACE?;

AND: A N D | '&' ;
OR: O R | '|' ;
NOT: N O T | '!' ;

attribute: ID | TITLE;

ID: I D ;
TITLE: T I T L E ;

operator: EQ
        | LT
        | GT
        | LTE
        | GTE
        | CONTAINS
        ;

// operators
EQ: E Q | '==' ;
LT: L T | '<' ;
GT: G T | '>' ;
LTE: L T E | '<=' ;
GTE: G T E | '>=' ;
CONTAINS: C O N T A I N S | '~';

value: STRING
     | NUMBER
     | DATE
     ;

// basic types
STRING: '\'' (~('\'' | '\r' | '\n'))* '\'' ;
NUMBER: DIGIT+ ('.' DIGIT+)? ;
DATE: DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT ; // 2020-03-03, todo NAE-1997 better recognition


// fragments
fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];
fragment DIGIT: [0-9];

SPACE: [ ]+ ;
ANY: . ;