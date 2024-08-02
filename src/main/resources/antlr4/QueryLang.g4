grammar QueryLang;

query:  resource delimeter conditions EOF ;

resource: CASE
        | CASES
        | TASK
        | TASKS
        | USER
        | USERS
        | PROCESS
        | PROCESSES
        ;

// delimeter
delimeter: WHERE_DELIMETER | COLON_DELIMETER ;
WHERE_DELIMETER: SPACE W H E R E SPACE ;
COLON_DELIMETER: SPACE? ':' SPACE ;

// conditions
conditions: orExpression ;
orExpression: andExpression (SPACE OR SPACE andExpression)* ;
andExpression: conditionGroup (SPACE AND SPACE conditionGroup)* ;
conditionGroup: condition | '(' SPACE? orExpression SPACE? ')' SPACE? ;
condition: (NOT SPACE)? attribute SPACE operator SPACE value SPACE? ;

attribute: ID
         | TITLE
         | IDENTIFIER
         | VERSION
         | CREATION_DATE
         | PROCESS_ID
         | AUTHOR
         | TRANSITION_ID
         | STATE
         | USER_ID
         | CASE_ID
         | LAST_ASSIGN
         | LAST_FINISH
         | NAME
         | SURNAME
         | EMAIL
         | data
         | places
         | tasks
         ;

operator: EQ
        | LT
        | GT
        | LTE
        | GTE
        | CONTAINS
        ;

// special attribute rules
data: DATA '.' field_id=JAVA_ID '.' (VALUE | OPTIONS) ;
places: PLACES '.' place_id=JAVA_ID '.' MARKING ;
tasks: TASKS '.' task_id=JAVA_ID '.' (STATE | USER_ID) ;

value: STRING
     | NUMBER
     | DATE
     | DATETIME
     | VERSION_NUMBER
     | LIST
     | BOOLEAN
     ;

// operators
AND: A N D | '&' ;
OR: O R | '|' ;
NOT: N O T | '!' ;
EQ: E Q | '==' ;
LT: L T | '<' ;
GT: G T | '>' ;
LTE: L T E | '<=' ;
GTE: G T E | '>=' ;
CONTAINS: C O N T A I N S | '~';

// resurces
CASE: C A S E ;
CASES: C A S E S ;
TASK: T A S K ;
TASKS: T A S K S ;
USER: U S E R ;
USERS: U S E R S ;
PROCESS: P R O C E S S ;
PROCESSES: P R O C E S S E S ;

// attributes
ID: I D ;
TITLE: T I T L E ;
IDENTIFIER: I D E N T I F I E R ;
VERSION: V E R S I O N ;
CREATION_DATE: C R E A T I O N D A T E ;
PROCESS_ID: P R O C E S S I D ;
AUTHOR: A U T H O R ;
PLACES: P L A C E S ;
TRANSITION_ID: T R A N S I T I O N I D ;
STATE: S T A T E ;
USER_ID: U S E R I D ;
CASE_ID: C A S E I D ;
LAST_ASSIGN: L A S T A S S I G N ;
LAST_FINISH: L A S T F I N I S H ;
NAME: N A M E ;
SURNAME: S U R N A M E ;
EMAIL: E M A I L ;

DATA: D A T A ;
VALUE: V A L U E ;
OPTIONS: O P T I O N S ;
MARKING: M A R K I N G ;

// basic types
LIST: '[' SPACE? ((STRING | NUMBER) SPACE? (',' SPACE? (STRING | NUMBER) SPACE? )* )? SPACE? ']' ;
STRING: '\'' (~('\'' | '\r' | '\n'))* '\'' ;
NUMBER: DIGIT+ ('.' DIGIT+)? ;
DATETIME: DATE SPACE DIGIT DIGIT ':' DIGIT DIGIT ':' DIGIT DIGIT ; // 2020-03-03 20:00:00 todo NAE-1997 better recognition
DATE: DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT ; // 2020-03-03, todo NAE-1997 better recognition
BOOLEAN: T R U E | F A L S E ;
VERSION_NUMBER: DIGIT+ '.' DIGIT+ '.' DIGIT+ ;
JAVA_ID: [a-zA-Z$_] [a-zA-Z0-9$_]* ;
SPACE: [ ]+ ;
ANY: . ;

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
