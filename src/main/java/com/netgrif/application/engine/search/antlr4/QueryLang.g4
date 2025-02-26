// todo NAE-1997: generate this with plugin
grammar QueryLang;

query: resource=(PROCESS | PROCESSES) delimeter processConditions (paging)? (processSorting)? EOF # processQuery
     | resource=(CASE | CASES) delimeter caseConditions (paging)? (caseSorting)? EOF # caseQuery
     | resource=(TASK | TASKS) delimeter taskConditions (paging)? (taskSorting)? EOF # taskQuery
     | resource=(USER | USERS) delimeter userConditions (paging)? (userSorting)? EOF # userQuery
     ;

processConditions: processOrExpression ;
processOrExpression: processAndExpression (SPACE OR SPACE processAndExpression)* ;
processAndExpression: processConditionGroup (SPACE AND SPACE processConditionGroup)* ;
processConditionGroup: processCondition # processConditionGroupBasic
                     | (NOT SPACE?)? '(' SPACE? processConditions SPACE? ')' SPACE? # processConditionGroupParenthesis
                     ;
processCondition: processComparisons SPACE? ;

caseConditions: caseOrExpression ;
caseOrExpression: caseAndExpression (SPACE OR SPACE caseAndExpression)* ;
caseAndExpression: caseConditionGroup (SPACE AND SPACE caseConditionGroup)* ;
caseConditionGroup: caseCondition # caseConditionGroupBasic
                  | (NOT SPACE?)? '(' SPACE? caseConditions SPACE? ')' SPACE? # caseConditionGroupParenthesis
                  ;
caseCondition: caseComparisons SPACE? ;

taskConditions: taskOrExpression ;
taskOrExpression: taskAndExpression (SPACE OR SPACE taskAndExpression)* ;
taskAndExpression: taskConditionGroup (SPACE AND SPACE taskConditionGroup)* ;
taskConditionGroup: taskCondition # taskConditionGroupBasic
                  | (NOT SPACE?)? '(' SPACE? taskConditions SPACE? ')' SPACE? # taskConditionGroupParenthesis
                  ;
taskCondition: taskComparisons SPACE? ;

userConditions: userOrExpression ;
userOrExpression: userAndExpression (SPACE OR SPACE userAndExpression)* ;
userAndExpression: userConditionGroup (SPACE AND SPACE userConditionGroup)* ;
userConditionGroup: userCondition # userConditionGroupBasic
                  | (NOT SPACE?)? '(' SPACE? userConditions SPACE? ')' SPACE? # userConditionGroupParenthesis
                  ;
userCondition: userComparisons SPACE? ;

// delimeter
delimeter: WHERE_DELIMETER | COLON_DELIMETER ;
WHERE_DELIMETER: SPACE W H E R E SPACE ;
COLON_DELIMETER: SPACE? ':' SPACE ;

// paging
paging: PAGE SPACE pageNum=INT (SPACE SIZE SPACE pageSize=INT)? SPACE?;

// sorting
processSorting: SORT_BY SPACE processAttributeOrdering (',' SPACE? processAttributeOrdering)* SPACE?;
processAttributeOrdering: processAttribute (SPACE ordering=(ASC | DESC))? ;
processAttribute: ID
                | IDENTIFIER
                | VERSION
                | TITLE
                | CREATION_DATE
                ;

caseSorting: SORT_BY SPACE caseAttributeOrdering (',' SPACE? caseAttributeOrdering)* SPACE?;
caseAttributeOrdering: caseAttribute (SPACE ordering=(ASC | DESC))? ;
caseAttribute: ID
             | PROCESS_ID
             | PROCESS_IDENTIFIER
             | TITLE
             | CREATION_DATE
             | AUTHOR
             | places
             | tasksUserId
             | tasksState
             | dataValue
             | dataOptions
             ;

taskSorting: SORT_BY SPACE taskAttributeOrdering (',' SPACE? taskAttributeOrdering)* SPACE?;
taskAttributeOrdering: taskAttribute (SPACE ordering=(ASC | DESC))? ;
taskAttribute: ID
             | TRANSITION_ID
             | TITLE
             | STATE
             | USER_ID
             | CASE_ID
             | PROCESS_ID
             | LAST_ASSIGN
             | LAST_FINISH
             ;

userSorting: SORT_BY SPACE userAttributeOrdering (',' SPACE? userAttributeOrdering)* SPACE?;
userAttributeOrdering: userAttribute (SPACE ordering=(ASC | DESC))? ;
userAttribute: ID
             | NAME
             | SURNAME
             | EMAIL
             ;

// resource comparisons
processComparisons: idComparison
                  | identifierComparison
                  | versionComparison
                  | titleComparison
                  | creationDateComparison
                  ;

caseComparisons: idComparison
               | processIdComparison
               | processIdentifierComparison
               | titleComparison
               | creationDateComparison
               | authorComparison
               | placesComparison
               | tasksStateComparison
               | tasksUserIdComparison
               | dataValueComparison
               | dataOptionsComparison
               ;

taskComparisons: idComparison
               | transitionIdComparison
               | titleComparison
               | stateComparison
               | userIdComparison
               | caseIdComparison
               | processIdComparison
               | lastAssignComparison
               | lastFinishComparison
               ;

userComparisons: idComparison
               | nameComparison
               | surnameComparison
               | emailComparison
               ;

// attribute comparisons
idComparison: ID SPACE objectIdComparison ;
titleComparison: TITLE SPACE stringComparison ;
identifierComparison: IDENTIFIER SPACE stringComparison ;
versionComparison: VERSION SPACE (NOT SPACE?)? op=(EQ | LT | GT | LTE | GTE) SPACE VERSION_NUMBER ;
creationDateComparison: CREATION_DATE SPACE dateComparison # cdDate
                      | CREATION_DATE SPACE dateTimeComparison # cdDateTime
                      ;
processIdComparison: PROCESS_ID SPACE stringComparison ;
processIdentifierComparison: PROCESS_IDENTIFIER SPACE stringComparison ;
authorComparison: AUTHOR SPACE stringComparison ;
transitionIdComparison: TRANSITION_ID SPACE stringComparison ;
stateComparison: STATE SPACE EQ SPACE state=(ENABLED | DISABLED) ;
userIdComparison: USER_ID SPACE stringComparison ;
caseIdComparison: CASE_ID SPACE stringComparison ;
lastAssignComparison: LAST_ASSIGN SPACE dateComparison # laDate
                    | LAST_ASSIGN SPACE dateTimeComparison # laDateTime
                    ;
lastFinishComparison: LAST_FINISH SPACE dateComparison # lfDate
                    | LAST_FINISH SPACE dateTimeComparison # lfDateTime
                    ;
nameComparison: NAME SPACE stringComparison ;
surnameComparison: SURNAME SPACE stringComparison ;
emailComparison: EMAIL SPACE stringComparison ;
dataValueComparison: dataValue SPACE stringComparison # dataString
              | dataValue SPACE numberComparison # dataNumber
              | dataValue SPACE dateComparison # dataDate
              | dataValue SPACE dateTimeComparison # dataDatetime
              | dataValue SPACE booleanComparison # dataBoolean
              ;
dataOptionsComparison: dataOptions SPACE stringComparison ;
placesComparison: places SPACE numberComparison ;
tasksStateComparison: tasksState SPACE (NOT SPACE?)? op=EQ SPACE state=(ENABLED | DISABLED) ;
tasksUserIdComparison: tasksUserId SPACE stringComparison ;

// basic comparisons
objectIdComparison: (NOT SPACE?)? op=EQ SPACE STRING ;
stringComparison: (NOT SPACE?)? op=(EQ | CONTAINS) SPACE STRING ;
numberComparison: (NOT SPACE?)? op=(EQ | LT | GT | LTE | GTE) SPACE number=(INT | DOUBLE) ;
dateComparison: (NOT SPACE?)? op=(EQ | LT | GT | LTE | GTE) SPACE DATE ;
dateTimeComparison: (NOT SPACE?)? op=(EQ | LT | GT | LTE | GTE) SPACE DATETIME ;
booleanComparison: (NOT SPACE?)? op=EQ SPACE BOOLEAN ;

// special attribute rules
dataValue: DATA '.' fieldId=JAVA_ID '.' VALUE ;
dataOptions: DATA '.' fieldId=JAVA_ID '.' OPTIONS ;
places: PLACES '.' placeId=JAVA_ID '.' MARKING ;
tasksState: TASKS '.' taskId=JAVA_ID '.' STATE ;
tasksUserId: TASKS '.' taskId=JAVA_ID '.' USER_ID ;

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
PROCESS_IDENTIFIER: P R O C E S S I D E N T I F I E R ;
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
ENABLED: E N A B L E D ;
DISABLED: D I S A B L E D ;

// paging
PAGE: P A G E ;
SIZE: S I Z E ;

// sorting
SORT_BY: S O R T SPACE B Y ;
ASC: A S C ;
DESC: D E S C ;

// basic types
LIST: '[' SPACE? ((STRING | NUMBER) SPACE? (',' SPACE? (STRING | NUMBER) SPACE? )* )? SPACE? ']' ;
STRING: '\'' (~('\'' | '\r' | '\n'))* '\'' ; // todo NAE-1997: escape???
INT: DIGIT+ ;
DOUBLE: DIGIT+ '.' DIGIT+ ;
DATETIME: DATE 'T' ([01] DIGIT | '2' [0-3]) ':' [0-5] DIGIT ':' [0-5] DIGIT ('.' DIGIT+)? ; // 2020-03-03T20:00:00.055 // todo NAE-1997: format
DATE: DIGIT DIGIT DIGIT DIGIT '-' ('0' [1-9] | '1' [0-2]) '-' ('0' [1-9] | [12] DIGIT | '3' [01]) ; // 2020-03-03 // todo NAE-1997: format
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
