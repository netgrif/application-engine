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
delimeter: SPACE WHERE SPACE | SPACE? ':' SPACE ;
WHERE: W H E R E ;

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
               | processIdObjIdComparison
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
idComparison: ID SPACE objectIdComparison # idBasic
            | ID SPACE inListStringComparison # idList
            ;
titleComparison: TITLE SPACE stringComparison # titleBasic
               | TITLE SPACE inListStringComparison # titleList
               | TITLE SPACE inRangeStringComparison # titleRange
               ;
identifierComparison: IDENTIFIER SPACE stringComparison # identifierBasic
                    | IDENTIFIER SPACE inListStringComparison # identifierList
                    | IDENTIFIER SPACE inRangeStringComparison # identifierRange
                    ;
versionComparison: VERSION SPACE (NOT SPACE?)? op=(EQ | LT | GT | LTE | GTE) SPACE VERSION_NUMBER # versionBasic
                 | VERSION SPACE inListVersionComparison # versionListCmp
                 | VERSION SPACE inRangeVersionComparison # versionRangeCmp
                 ;
creationDateComparison: CREATION_DATE SPACE dateComparison # cdDateBasic
                      | CREATION_DATE SPACE dateTimeComparison # cdDateTimeBasic
                      | CREATION_DATE SPACE inListDateComparison # cdDateList
                      | CREATION_DATE SPACE inRangeDateComparison # cdDateRange
                      ;
processIdComparison: PROCESS_ID SPACE stringComparison # processIdBasic
                   | PROCESS_ID SPACE inListStringComparison # processIdList
                   ;
processIdObjIdComparison: PROCESS_ID SPACE objectIdComparison # processIdObjIdBasic
                   | PROCESS_ID SPACE inListStringComparison # processIdObjIdList
                   ;
processIdentifierComparison: PROCESS_IDENTIFIER SPACE stringComparison # processIdentifierBasic
                           | PROCESS_IDENTIFIER SPACE inListStringComparison # processIdentifierList
                           | PROCESS_IDENTIFIER SPACE inRangeStringComparison # processIdentifierRange
                           ;
authorComparison: AUTHOR SPACE stringComparison # authorBasic
                | AUTHOR SPACE inListStringComparison # authorList
                ;
transitionIdComparison: TRANSITION_ID SPACE stringComparison # transitionIdBasic
                      | TRANSITION_ID SPACE inListStringComparison # transitionIdList
                      | TRANSITION_ID SPACE inRangeStringComparison # transitionIdRange
                      ;
stateComparison: STATE SPACE EQ SPACE state=(ENABLED | DISABLED) ;
userIdComparison: USER_ID SPACE stringComparison # userIdBasic
                | USER_ID SPACE inListStringComparison # userIdList
                ;
caseIdComparison: CASE_ID SPACE stringComparison # caseIdBasic
                | CASE_ID SPACE inListStringComparison # caseIdList
                ;
lastAssignComparison: LAST_ASSIGN SPACE dateComparison # laDateBasic
                    | LAST_ASSIGN SPACE dateTimeComparison # laDateTimeBasic
                    | LAST_ASSIGN SPACE inListDateComparison # laDateList
                    | LAST_ASSIGN SPACE inRangeDateComparison # laDateRange
                    ;
lastFinishComparison: LAST_FINISH SPACE dateComparison # lfDateBasic
                    | LAST_FINISH SPACE dateTimeComparison # lfDateTimeBasic
                    | LAST_FINISH SPACE inListDateComparison # lfDateList
                    | LAST_FINISH SPACE inRangeDateComparison # lfDateRange
                    ;
nameComparison: NAME SPACE stringComparison # nameBasic
              | NAME SPACE inListStringComparison # nameList
              | NAME SPACE inRangeStringComparison # nameRange
              ;
surnameComparison: SURNAME SPACE stringComparison # surnameBasic
                 | SURNAME SPACE inListStringComparison # surnameList
                 | SURNAME SPACE inRangeStringComparison # surnameRange
                 ;
emailComparison: EMAIL SPACE stringComparison # emailBasic
               | EMAIL SPACE inListStringComparison # emailList
               | EMAIL SPACE inRangeStringComparison # emailRange
               ;
dataValueComparison: dataValue SPACE stringComparison # dataString
              | dataValue SPACE numberComparison # dataNumber
              | dataValue SPACE dateComparison # dataDate
              | dataValue SPACE dateTimeComparison # dataDatetime
              | dataValue SPACE booleanComparison # dataBoolean
              | dataValue SPACE inListStringComparison # dataStringList
              | dataValue SPACE inListNumberComparison # dataNumberList
              | dataValue SPACE inListDateComparison # dataDateList
              | dataValue SPACE inRangeStringComparison # dataStringRange
              | dataValue SPACE inRangeNumberComparison # dataNumberRange
              | dataValue SPACE inRangeDateComparison # dataDateRange
              ;
dataOptionsComparison: dataOptions SPACE stringComparison # dataOptionsBasic
                     | dataOptions SPACE inListStringComparison # dataOptionsList
                     | dataOptions SPACE inRangeStringComparison # dataOptionsRange
                     ;
placesComparison: places SPACE numberComparison # placesBasic
                | places SPACE inListNumberComparison # placesList
                | places SPACE inRangeNumberComparison # placesRange
                ;
tasksStateComparison: tasksState SPACE (NOT SPACE?)? op=EQ SPACE state=(ENABLED | DISABLED) ;
tasksUserIdComparison: tasksUserId SPACE stringComparison # tasksUserIdBasic
                     | tasksUserId SPACE inListStringComparison # tasksUserIdList
                     ;

// basic comparisons
objectIdComparison: (NOT SPACE?)? op=EQ SPACE STRING ;
stringComparison: (NOT SPACE?)? op=(EQ | CONTAINS | LT | GT | LTE | GTE) SPACE STRING ;
numberComparison: (NOT SPACE?)? op=(EQ | LT | GT | LTE | GTE) SPACE number=(INT | DOUBLE) ;
dateComparison: (NOT SPACE?)? op=(EQ | LT | GT | LTE | GTE) SPACE DATE ;
dateTimeComparison: (NOT SPACE?)? op=(EQ | LT | GT | LTE | GTE) SPACE DATETIME ;
booleanComparison: (NOT SPACE?)? op=EQ SPACE BOOLEAN ;

// in list/in range comparisons
inListStringComparison: (NOT SPACE?)? op=IN SPACE stringList ;
inListNumberComparison: (NOT SPACE?)? op=IN SPACE (intList | doubleList) ;
inListDateComparison: (NOT SPACE?)? op=IN SPACE (dateList | dateTimeList) ;
inListVersionComparison: (NOT SPACE?)? op=IN SPACE versionList ;
inRangeStringComparison: (NOT SPACE?)? op=IN SPACE stringRange ;
inRangeNumberComparison: (NOT SPACE?)? op=IN SPACE (intRange | doubleRange) ;
inRangeDateComparison: (NOT SPACE?)? op=IN SPACE (dateRange | dateTimeRange) ;
inRangeVersionComparison: (NOT SPACE?)? op=IN SPACE versionRange ;

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
IN: I N ;

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
stringList: '(' SPACE? (STRING SPACE? (',' SPACE? STRING SPACE? )* )? SPACE? ')' ;
intList: '(' SPACE? (INT SPACE? (',' SPACE? INT SPACE? )* )? SPACE? ')' ;
doubleList: '(' SPACE? (DOUBLE SPACE? (',' SPACE? DOUBLE SPACE? )* )? SPACE? ')' ;
dateList: '(' SPACE? (DATE SPACE? (',' SPACE? DATE SPACE? )* )? SPACE? ')' ;
dateTimeList: '(' SPACE? (DATETIME SPACE? (',' SPACE? DATETIME SPACE? )* )? SPACE? ')' ;
versionList: '(' SPACE? (VERSION_NUMBER SPACE? (',' SPACE? VERSION_NUMBER SPACE? )* )? SPACE? ')' ;
stringRange: leftEndpoint=('(' | '[') SPACE? STRING SPACE? ':' SPACE? STRING SPACE? rightEndpoint=(')' | ']') ;
intRange: leftEndpoint=('(' | '[') SPACE? INT SPACE? ':' SPACE? INT SPACE? rightEndpoint=(')' | ']') ;
doubleRange: leftEndpoint=('(' | '[') SPACE? DOUBLE SPACE? ':' SPACE? DOUBLE SPACE? rightEndpoint=(')' | ']') ;
dateRange: leftEndpoint=('(' | '[') SPACE? DATE SPACE? ':' SPACE? DATE SPACE? rightEndpoint=(')' | ']') ;
dateTimeRange: leftEndpoint=('(' | '[') SPACE? DATETIME SPACE? ':' SPACE? DATETIME SPACE? rightEndpoint=(')' | ']') ;
versionRange: leftEndpoint=('(' | '[') SPACE? VERSION_NUMBER SPACE? ':' SPACE? VERSION_NUMBER SPACE? rightEndpoint=(')' | ']') ;
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
