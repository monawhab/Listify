# Crash Analysis: LIS-20

## Crash
NetworkOnMainThreadException in ProductRepository

## Root Cause
Network call made on the main thread.

## Suggested Fix
Wrap in viewModelScope.launch { withContext(Dispatchers.IO) { ... } }

## Confidence
high

## Affected File
app/src/main/java/com/listify/data/repository/ProductRepositoryImpl/ProductRepositoryImpl.kt
