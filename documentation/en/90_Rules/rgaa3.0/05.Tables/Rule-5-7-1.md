# RGAA 3.0 -  Rule 5.7.1

## Summary

The test consists in detecting data or complex tables on the page.

Checking whether each header of a data table applied to the whole row or to the whole column have a unique id attribute or a scope attribute has to be done manually.

## Business description

### Criterion

[5.7](http://disic.github.io/rgaa_referentiel_en/RGAA3.0_Criteria_English_version_v1.html#crit-5-7)

### Test

[5.7.1](http://disic.github.io/rgaa_referentiel_en/RGAA3.0_Criteria_English_version_v1.html#test-5-7-1)

### Description
Does each header cell (<code>th</code>
    tag) applied to the whole row or to the whole column
    have a unique <code>id</code> attribute or a <code>scope</code> attribute? 


### Level

**A**

## Technical description

### Scope

**Page**

### Decision level

**Semi-Decidable**

## Algorithm

### Selection

#### Set1 (table tags identified as data or complex table from html markers)

All the `<table>` tags with an "id" attribute or a "class" attribute or a "role" attribute that matches one of the values set by the user through the "DATA_TABLE_MARKER" parameter or the "COMPLEX_TABLE_MARKER" parameter AND with `<th>` child tags.

#### Set2 (table tags not identified as data table from html markers)

All the `<table>` tags that don't have an "id" attribute or a "class" attribute or a "role" attribute that matches one the values set by the user through the "PRESENTATION_TABLE_MARKER" parameter or the
"DATA_TABLE_MARKER" parameter or the "COMPLEX_TABLE_MARKER" parameter AND with `<th>` child tags. 

That means select all the `<table>` tags of the page when these parameters are empty.

### Process

#### Test1

For each occurence of **Set1**, raise a MessageA

#### Test2

For each occurence of **Set2**, raise a MessageB

###### MessageA : Check the definition of headers for data tables

-   code :CheckDefinitionOfHeaderForDataTable
-   status: Pre-Qualified
-   parameter : snippet
-   present in source : yes

###### MessageB : Check the nature of table and the definition of headers for data tables

-   code :CheckNatureOfTableAndHeadersDefinition
-   status: Pre-Qualified
-   parameter : snippet
-   present in source : yes

### Analysis

#### Not Applicable

The page has no `<table>` tag with `<th>` child tags or only tables identified as presentation table (**Set1** AND **Set2** are empty)

#### Pre-qualified

In all other cases

## Notes

We only detect the elements of the scope of the test to determine whether the test is applicable.

Complex tables are seen as a subset of data tables. That's tables identified as complex tables are added to the set of identified data tables.



##  TestCases 

[TestCases files for rule 5.7.1](https://github.com/Asqatasun/Asqatasun/tree/master/rules/rules-rgaa3.0/src/test/resources/testcases/rgaa30/Rgaa30Rule050701/) 


