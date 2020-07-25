grammar ReladomoOperation;

@lexer::members {
    public static final int WHITESPACE_CHANNEL = 1000;
    public static final int COMMENTS_CHANNEL = 2000;
    public static final int LINE_COMMENTS_CHANNEL = 3000;
}

compilationUnit: compositeOperation EOF;

compositeOperation
    : compositeOperation '&' compositeOperation # OperationAnd
    | compositeOperation '|' compositeOperation # OperationOr
    | '(' compositeOperation ')'                # OperationGroup
    | 'all' ('of' className)?                   # OperationAll
    | 'none'                                    # OperationNone
    // Order matters here because '== null' can match both unary and binary
    | attribute unaryOperator                   # OperationUnaryOperator
    | attribute binaryOperator parameter        # OperationBinaryOperator
    ;

attribute:         simpleAttribute | functionAttribute;
simpleAttribute:   ('this' | className) '.' (relationshipName '.')* attributeName;
functionAttribute
    : functionName=('lower' | 'toLowerCase') '(' attribute ')'           # FunctionToLowerCase
    | 'substring' '(' attribute ',' IntegerLiteral ',' IntegerLiteral ')'# FunctionToSubstring
    | functionName=('abs' | 'absoluteValue') '(' attribute ')'           # FunctionAbsoluteValue
    | functionName=Identifier '(' attribute ')'                          # FunctionUnknown
    ;

binaryOperator
    : operatorEq
    | operatorNotEq
    | operatorIn
    | operatorNotIn
    | operatorGreaterThan
    | operatorGreaterThanEquals
    | operatorLessThan
    | operatorLessThanEquals
    | operatorStartsWith
    | operatorNotStartsWith
    | operatorEndsWith
    | operatorNotEndsWith
    | operatorContains
    | operatorNotContains
    | operatorWildCardEquals
    | operatorWildCardNotEquals
    | operatorWildCardIn
    ;

operatorEq:                '='  | '==' | 'eq';
operatorNotEq:             '!=' | 'not eq' | 'notEq';
operatorGreaterThan:       '>'  | 'greaterThan';
operatorGreaterThanEquals: '>=' | 'greaterThanEquals';
operatorLessThan:          '<'  | 'lessThan';
operatorLessThanEquals:    '<=' | 'lessThanEquals';
operatorIn:                'in';
operatorNotIn:             'not in' | 'notIn';
operatorStartsWith:        'startsWith';
operatorNotStartsWith:     'not startsWith' | 'notStartsWith';
operatorEndsWith:          'endsWith';
operatorNotEndsWith:       'not endsWith' | 'notEndsWith';
operatorContains:          'contains';
operatorNotContains:       'not contains' | 'notContains';
operatorWildCardEquals:    'wildCardEquals';
operatorWildCardNotEquals: 'not wildCardEquals' | 'wildCardNotEquals';
operatorWildCardIn:        'wildCardIn';

unaryOperator
    : operatorIsNull
    | operatorIsNotNull
    | equalsEdgePoint;

operatorIsNull:    ('is'       'null') | ('==' 'null');
operatorIsNotNull: ('is' 'not' 'null') | ('!=' 'null');
equalsEdgePoint:   'equalsEdgePoint';

parameter
    : stringLiteral
    | booleanLiteral
    | characterLiteral
    | integerLiteral
    | floatingPointLiteral

    | stringListLiteral
    | booleanListLiteral
    | characterListLiteral
    | integerListLiteral
    | floatingPointListLiteral
    ;

NullLiteral:              'null';

stringLiteral:            NullLiteral | StringLiteral;
booleanLiteral:           NullLiteral | BooleanLiteral;
characterLiteral:         NullLiteral | CharacterLiteral;
integerLiteral:           NullLiteral | IntegerLiteral;
floatingPointLiteral:     NullLiteral | FloatingPointLiteral;

stringListLiteral:        '[' stringLiteral        (',' stringLiteral)*        ']';
booleanListLiteral:       '[' booleanLiteral       (',' booleanLiteral)*       ']';
characterListLiteral:     '[' characterLiteral     (',' characterLiteral)*     ']';
integerListLiteral:       '[' integerLiteral       (',' integerLiteral)*       ']';
floatingPointListLiteral: '[' floatingPointLiteral (',' floatingPointLiteral)* ']';

className:        Identifier;
relationshipName: Identifier;
attributeName:    Identifier;

// §3.10.1 Integer Literals

IntegerLiteral
    :    DecimalIntegerLiteral
    |    HexIntegerLiteral
    |    OctalIntegerLiteral
    |    BinaryIntegerLiteral
    ;

fragment
DecimalIntegerLiteral
    :    DecimalNumeral IntegerTypeSuffix?
    ;

fragment
HexIntegerLiteral
    :    HexNumeral IntegerTypeSuffix?
    ;

fragment
OctalIntegerLiteral
    :    OctalNumeral IntegerTypeSuffix?
    ;

fragment
BinaryIntegerLiteral
    :    BinaryNumeral IntegerTypeSuffix?
    ;

fragment
IntegerTypeSuffix
    :    [lL]
    ;

fragment
DecimalNumeral
    :    '0'
    |    NonZeroDigit (Digits? | Underscores Digits)
    ;

fragment
Digits
    :    Digit (DigitsAndUnderscores? Digit)?
    ;

fragment
Digit
    :    '0'
    |    NonZeroDigit
    ;

fragment
NonZeroDigit
    :    [1-9]
    ;

fragment
DigitsAndUnderscores
    :    DigitOrUnderscore+
    ;

fragment
DigitOrUnderscore
    :    Digit
    |    '_'
    ;

fragment
Underscores
    :    '_'+
    ;

fragment
HexNumeral
    :    '0' [xX] HexDigits
    ;

fragment
HexDigits
    :    HexDigit (HexDigitsAndUnderscores? HexDigit)?
    ;

fragment
HexDigit
    :    [0-9a-fA-F]
    ;

fragment
HexDigitsAndUnderscores
    :    HexDigitOrUnderscore+
    ;

fragment
HexDigitOrUnderscore
    :    HexDigit
    |    '_'
    ;

fragment
OctalNumeral
    :    '0' Underscores? OctalDigits
    ;

fragment
OctalDigits
    :    OctalDigit (OctalDigitsAndUnderscores? OctalDigit)?
    ;

fragment
OctalDigit
    :    [0-7]
    ;

fragment
OctalDigitsAndUnderscores
    :    OctalDigitOrUnderscore+
    ;

fragment
OctalDigitOrUnderscore
    :    OctalDigit
    |    '_'
    ;

fragment
BinaryNumeral
    :    '0' [bB] BinaryDigits
    ;

fragment
BinaryDigits
    :    BinaryDigit (BinaryDigitsAndUnderscores? BinaryDigit)?
    ;

fragment
BinaryDigit
    :    [01]
    ;

fragment
BinaryDigitsAndUnderscores
    :    BinaryDigitOrUnderscore+
    ;

fragment
BinaryDigitOrUnderscore
    :    BinaryDigit
    |    '_'
    ;

// §3.10.2 Floating-Point Literals

FloatingPointLiteral
    :    DecimalFloatingPointLiteral
    |    HexadecimalFloatingPointLiteral
    ;

fragment
DecimalFloatingPointLiteral
    :    Digits '.' Digits ExponentPart? FloatTypeSuffix?
    |    '.' Digits ExponentPart? FloatTypeSuffix?
    |    Digits ExponentPart FloatTypeSuffix?
    |    Digits FloatTypeSuffix
    ;

fragment
ExponentPart
    :    ExponentIndicator SignedInteger
    ;

fragment
ExponentIndicator
    :    [eE]
    ;

fragment
SignedInteger
    :    Sign? Digits
    ;

fragment
Sign
    :    [+-]
    ;

fragment
FloatTypeSuffix
    :    [fFdD]
    ;

fragment
HexadecimalFloatingPointLiteral
    :    HexSignificand BinaryExponent FloatTypeSuffix?
    ;

fragment
HexSignificand
    :    HexNumeral '.'?
    |    '0' [xX] HexDigits? '.' HexDigits
    ;

fragment
BinaryExponent
    :    BinaryExponentIndicator SignedInteger
    ;

fragment
BinaryExponentIndicator
    :    [pP]
    ;

// §3.10.3 Boolean Literals

BooleanLiteral
    :    LITERAL_TRUE
    |    LITERAL_FALSE
    ;

LITERAL_TRUE  : 'true';
LITERAL_FALSE : 'false';

// §3.10.4 Character Literals

CharacterLiteral
    :    '\'' SingleCharacter '\''
    |    '\'' EscapeSequence '\''
    ;

fragment
SingleCharacter
    :    ~['\\\r\n]
    ;
// §3.10.5 String Literals
StringLiteral
    :    '"' StringCharacters? '"'
    ;
fragment
StringCharacters
    :    StringCharacter+
    ;
fragment
StringCharacter
    :    ~["\\\r\n]
    |    EscapeSequence
    ;
// §3.10.6 Escape Sequences for Character and String Literals
fragment
EscapeSequence
    :    '\\' [btnfr"'\\]
    |    OctalEscape
    |   UnicodeEscape // This is not in the spec but prevents having to preprocess the input
    ;

fragment
OctalEscape
    :    '\\' OctalDigit
    |    '\\' OctalDigit OctalDigit
    |    '\\' ZeroToThree OctalDigit OctalDigit
    ;

fragment
ZeroToThree
    :    [0-3]
    ;

// This is not in the spec but prevents having to preprocess the input
fragment
UnicodeEscape
    :   '\\' 'u'+  HexDigit HexDigit HexDigit HexDigit
    ;

// §3.8 Identifiers (must appear after all keywords in the grammar)

Identifier
    :    JavaLetter JavaLetterOrDigit*
    ;

fragment
JavaLetter
    :    [a-zA-Z$_] // these are the "java letters" below 0x7F
    |    // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
        {Character.isJavaIdentifierStart(_input.LA(-1))}?
    |    // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

fragment
JavaLetterOrDigit
    :    [a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
    |    // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
        {Character.isJavaIdentifierPart(_input.LA(-1))}?
    |    // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

//
// Whitespace and comments
//

WHITESPACE  : [ \t\u000C]+  -> channel(1000);
NEWLINE     : [\r\n]        -> channel(1000);
COMMENT     : '/*' .*? '*/' -> channel(2000);
LINE_COMMENT: '//' ~[\r\n]* -> channel(3000);
