package edu.msudenver.cs.replican;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class P38 {
    public static Object call (String methodName, Object o) throws Throwable {
        /*
         * Use the java.lang.invoke API instead of raw reflection.  A
         * MethodHandle is obtained for the target method and then
         * bound to the receiver.  We still need to make the reflected
         * Method accessible in order to obtain a method handle for
         * non‑public members.
         */
        Method m = o.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        MethodHandle mh = MethodHandles.lookup().unreflect(m).bindTo(o);
        try {
            return mh.invokeWithArguments();
        } catch (Throwable t) {
            /*
             * invokeWithArguments wraps an exception the same way
             * reflection does, but since we bound the handle to the
             * receiver there is no InvocationTargetException.
             */
            throw t;
        }
    }

    public static Object call (String methodName, Object o, Object args[]) throws Throwable {
        // Get the parameter types for the method handle lookup.
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }

        Method m = o.getClass().getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        MethodHandle mh = MethodHandles.lookup().unreflect(m).bindTo(o);
        try {
            return mh.invokeWithArguments(args);
        } catch (Throwable t) {
            throw t;
        }
    }
}
