# P38: Private Method Testing Utility

A lightweight Java utility for unit testing private methods using the **MethodHandle API** instead of raw reflection.

## Overview

`P38` provides a clean, efficient way to invoke private (non-public) methods during unit testing. It leverages `java.lang.invoke.MethodHandle` to dynamically call instance methods without modifying access modifiers or using verbose reflection code.

This is useful when:
- You want to test private helper methods in isolation
- You need to verify internal method behavior that shouldn't be public API
- You prefer not to change code structure just to make methods testable

## Why MethodHandles?

The MethodHandle API (introduced in Java 7, refined since) is superior to raw reflection for this use case:

| Feature | Reflection | MethodHandle |
|---------|-----------|--------------|
| **Performance** | Slower, interpretation overhead | Faster, JIT compilation friendly |
| **Exception wrapping** | Wraps in `InvocationTargetException` | Preserves original exception when bound |
| **Modern API** | Legacy approach | Current best practice |
| **Inlining** | Limited JVM optimization | Better JVM optimization |

## Usage

### Basic Example: No-Argument Method

```java
public class MyClassTest {
    @Test
    void testPrivateHelper() throws Throwable {
        MyClass obj = new MyClass();
        // Call private method: private void helper()
        Object result = P38.call("helper", obj);
        assertNull(result);
    }
}
```

### With Arguments

```java
@Test
void testPrivateMethodWithArgs() throws Throwable {
    MyClass obj = new MyClass();
    Object result = P38.call("compute", obj, new Object[]{42, "test"});
    assertEquals("expected", result);
}
```

## API Reference

### `call(String methodName, Object instance)`

Invokes a private instance method with no parameters.

**Parameters:**
- `methodName` — Name of the private method to invoke
- `instance` — The object instance on which to invoke the method

**Returns:** The return value of the invoked method (null if void)

**Throws:** `Throwable` — Any exception thrown by the method, or method lookup failures

**Example:**
```java
P38.call("setup", myObject);
```

### `call(String methodName, Object instance, Object[] args)`

Invokes a private instance method with parameters.

**Parameters:**
- `methodName` — Name of the private method to invoke
- `instance` — The object instance on which to invoke the method
- `args` — Array of arguments to pass to the method

**Returns:** The return value of the invoked method (null if void)

**Throws:** `Throwable` — Any exception thrown by the method, or method lookup failures

**Example:**
```java
Object result = P38.call("calculate", myObject, new Object[]{10, 20});
int value = (Integer) result;
```

## Important Notes

### Type Matching
The second overload infers parameter types from the argument objects:
```java
Object[] args = {42, "hello", 3.14};
// Types are: Integer, String, Double
P38.call("method", obj, args);
```

If you have `null` arguments or need exact type matching (e.g., superclass types), you may need to use reflection directly to find the correct overload.

### Null Return Values
Both overloads return `null` for void methods:
```java
P38.call("doSomething", obj);  // Returns null if method is void
```

### Exception Handling
The method throws `Throwable` (not `Exception`) to match the Java reflection contract. Catch appropriately:
```java
try {
    P38.call("method", obj);
} catch (Throwable e) {
    // Handle any exception from the private method
}
```

### Thread Safety
`MethodHandle` instances are **thread-safe** and can be safely shared across threads. However, getting a handle requires `MethodHandles.lookup()`, which uses the caller's lookup context — thread-safe but uses fresh lookup per call. For high-frequency calls, consider caching the `MethodHandle`.

## Limitations

1. **Method resolution**: Only finds methods declared on the exact class (not inherited). For inherited private methods, you may need to adjust the approach.
2. **Overload resolution**: Parameter types are inferred from argument runtime types, so automatic widening (e.g., passing `int` when method expects `long`) won't work.
3. **Varargs**: Not directly supported; pass a fully-constructed array instead.

## Thread Safety

- `MethodHandle` itself is immutable and thread-safe
- Each call performs a fresh method lookup, which is thread-safe
- The `MethodHandles.lookup()` context is caller-aware and safe for concurrent use

## Testing Conventions

When testing private methods, consider:
- **Keep tests focused**: Test behavior, not implementation details
- **Use sparingly**: Excessive reliance on private method testing may indicate poor design
- **Document intent**: Comment why you're testing a private method (it's often a code smell)
- **Prefer package-private**: If you find yourself frequently testing private methods, make them package-private for easier testing

## See Also

- [Java MethodHandle Documentation](https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/MethodHandle.html)
- [MethodHandles.lookup()](https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/MethodHandles.html#lookup--)
- [Reflection vs MethodHandle Performance](https://www.baeldung.com/java-method-handles)
