# PetriNet functions

This document contains information about new Petriflow functions introduced in NAE 5.6.0.

## Overview

Petriflow functions provide opportunity to declare your own functions without any changes in applications code.

### Scopes

Functions can have ``process`` and ``global`` scope. Functions declared in ``process`` scope are visible only for specific process exactly like private functions in Java. ``Global`` functions are visible like public functions and we can access them via process identifier in other processes or in process they are declared in, just with function name and parameters.

### Usage

Functions are written in Groovy Closure style:

```xml
<function scope="process" name="calc">
    { monthly, loan, period ->
        change monthly value { (loan + loan * 0.02 * period) / (period * 12) }
    }
</function>
```

```xml
<data type="number">
    <id>loan_amount</id>
    <title name="loan_amount_tite">Loan amount in EUR</title>
    <valid>inrange 10000,1000000</valid>
    <init>100000</init>
    <action trigger="set">
        loan: f.loan_amount,
        period: f.period,
        monthly: f.monthly_payment;
        calc(monthly, loan.value, period.value)
    </action>
</data>
```

Only one function per element is allowed. Method overloading is also implemented and if more than one method with same method signature are present exception is thrown. Functions are compiled in run-time and like actions have access to ``ActionDelegate``. Actions have access to one each other with one exception that global functions can call only other global function, not process functions.

```xml
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
   <id>all_data</id>
   <title>All Data</title>
   <initials>ALL</initials>
    <!-- FUNCTIONS -->
    <function scope="global" name="sum">
        { Double x, Double y ->
            return x + y
        }
    </function>


    <data type="number" immediate="true">
        <id>number</id>
        <title>Number</title>
        <init>10000</init>
        <action trigger="set">
            number: f.number,
            number2: f.number2,
            result: f.result;
            change result value { all_data.sum(number.value, number2.value) }
        </action>
    </data>
    ...
</document>
```
