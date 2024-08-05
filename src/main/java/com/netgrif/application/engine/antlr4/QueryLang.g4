grammar QueryLang;

query: (processQuery | caseQuery | taskQuery | userQuery) EOF ;

processQuery: (PROCESS | PROCESSES) delimeter processConditions ;
caseQuery: (CASE | CASES) delimeter caseConditions ;
taskQuery: (TASK | TASKS) delimeter taskConditions ;
userQuery: (USER | USERS) delimeter userConditions ;

processConditions: processOrExpression ;
processOrExpression: processAndExpression (SPACE OR SPACE processAndExpression)* ;
processAndExpression: processConditionGroup (SPACE AND SPACE processConditionGroup)* ;
processConditionGroup: processCondition | '(' SPACE? processConditions SPACE? ')' SPACE? ;
processCondition: (NOT SPACE)? processComparisons SPACE? ;

caseConditions: caseOrExpression ;
caseOrExpression: caseAndExpression (SPACE OR SPACE caseAndExpression)* ;
caseAndExpression: caseConditionGroup (SPACE AND SPACE caseConditionGroup)* ;
caseConditionGroup: caseCondition | '(' SPACE? caseConditions SPACE? ')' SPACE? ;
caseCondition: (NOT SPACE)? caseComparisons SPACE? ;

taskConditions: taskOrExpression ;
taskOrExpression: taskAndExpression (SPACE OR SPACE taskAndExpression)* ;
taskAndExpression: taskConditionGroup (SPACE AND SPACE taskConditionGroup)* ;
taskConditionGroup: taskCondition | '(' SPACE? taskConditions SPACE? ')' SPACE? ;
taskCondition: (NOT SPACE)? taskComparisons SPACE? ;

userConditions: userOrExpression ;
userOrExpression: userAndExpression (SPACE OR SPACE userAndExpression)* ;
userAndExpression: userConditionGroup (SPACE AND SPACE userConditionGroup)* ;
userConditionGroup: userCondition | '(' SPACE? userConditions SPACE? ')' SPACE? ;
userCondition: (NOT SPACE)? userComparisons SPACE? ;

// delimeter
delimeter: WHERE_DELIMETER | COLON_DELIMETER ;
WHERE_DELIMETER: SPACE W H E R E SPACE ;
COLON_DELIMETER: SPACE? ':' SPACE ;

// resource comparisons
processComparisons: idComparison
                  | identifierComparison
                  | versionComparison
                  | titleComparison
                  | creationDateComparison
                  ;

caseComparisons: idComparison
               | processIdComparison
               | titleComparison
               | creationDateComparison
               | authorComparison
               | placesComparison
               | tasksComparison
               | dataComparison
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
idComparison: ID SPACE stringComparison ;
titleComparison: TITLE SPACE stringComparison ;
identifierComparison: IDENTIFIER SPACE stringComparison ;
versionComparison: VERSION SPACE (EQ | LT | GT | LTE | GTE) SPACE VERSION_NUMBER ;
creationDateComparison: CREATION_DATE SPACE (dateComparison | dateTimeComparison) ; // todo NAE-1997: date/datetime?
processIdComparison: PROCESS_ID SPACE stringComparison ;
authorComparison: AUTHOR SPACE stringComparison ;
transitionIdComparison: TRANSITION_ID SPACE stringComparison ;
stateComparison: STATE SPACE stringComparison ;
userIdComparison: USER_ID SPACE stringComparison ;
caseIdComparison: CASE_ID SPACE stringComparison ;
lastAssignComparison: LAST_ASSIGN SPACE (dateComparison | dateTimeComparison) ; // todo NAE-1997: date/datetime?
lastFinishComparison: LAST_FINISH SPACE (dateComparison | dateTimeComparison) ; // todo NAE-1997: date/datetime?
nameComparison: NAME SPACE stringComparison ;
surnameComparison: SURNAME SPACE stringComparison ;
emailComparison: EMAIL SPACE stringComparison ;
dataComparison: data SPACE (stringComparison | numberComparison | dateComparison | dateTimeComparison | booleanComparison) ;
placesComparison: places SPACE numberComparison ;
tasksComparison: tasks SPACE stringComparison ;

// basic comparisons
stringComparison: (EQ | CONTAINS) SPACE STRING ;
numberComparison: (EQ | LT | GT | LTE | GTE) SPACE NUMBER ;
dateComparison: (EQ | LT | GT | LTE | GTE) SPACE DATE ;
dateTimeComparison: (EQ | LT | GT | LTE | GTE) SPACE DATETIME ;
booleanComparison: EQ SPACE BOOLEAN ;

// special attribute rules
data: DATA '.' fieldId=JAVA_ID '.' (VALUE | OPTIONS) ;
places: PLACES '.' placeId=JAVA_ID '.' MARKING ;
tasks: TASKS '.' taskId=JAVA_ID '.' (STATE | USER_ID) ;

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
