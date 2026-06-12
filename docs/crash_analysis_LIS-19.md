# Crash Analysis: LIS-19

## Crash
IndexOutOfBoundsException in ProductAdapter

## Root Cause
List/array accessed at index that doesn't exist.

## Suggested Fix
Use list.getOrNull(index) or check list.size before access.

## Confidence
medium

## Affected File
app/src/main/java/com/listify/presentation/productlist/ProductAdapter/ProductAdapter.kt
