package org.torquebox.injection;

import java.lang.reflect.Method;

public class NullMemberException extends RuntimeException {
    
    private static final long serialVersionUID = 5514217582920793404L;
    
    private final Class<?> annotationType;
    private final Method method;

    public NullMemberException(Class<?> annotationType, Method method, String message)
    {
       super(message);
       this.annotationType = annotationType;
       this.method = method;
    }

    public Class<?> getAnnotationType()
    {
       return annotationType;
    }

    public Method getMethod()
    {
       return method;
    }

}
