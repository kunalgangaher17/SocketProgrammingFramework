package com.thinking.machines.nafserver.model;
import java.lang.reflect.*;
public class Service
{
private Module module;
private Method method;
private int numberOfParameters;
private boolean isVoid;
private boolean injectSession;
private boolean injectApplication;
private int sessionParameterIndexes[];
private int applicationParameterIndexes[];
public Service()
{
}
public void setModule(Module module)
{
this.module=module;
}
public Module getModule()
{
return this.module;
}
public void setMethod(Method method)
{
this.method=method;
}
public Method getMethod()
{
return this.method;
}
public void setNumberOfParameters(int numberOfParameters)
{
this.numberOfParameters=numberOfParameters;
}
public int getNumberOfParameters()
{
return this.numberOfParameters;
}
public void setIsVoid(boolean isVoid)
{
this.isVoid=isVoid;
}
public boolean getIsVoid()
{
return this.isVoid;
}
public void setInjectSession(boolean injectSession)
{
this.injectSession=injectSession;
}
public boolean getInjectSession()
{
return this.injectSession;
}
public void setInjectApplication(boolean injectApplication)
{
this.injectApplication=injectApplication;
}
public boolean getInjectApplication()
{
return this.injectApplication;
}
public void setSessionParameterIndexes(int sessionParameterIndexes[])
{
this.sessionParameterIndexes=sessionParameterIndexes;
}
public int[] getSessionParameterIndexes()
{
return this.sessionParameterIndexes;
}
public void setApplicationParameterIndexes(int applicationParameterIndexes[])
{
this.applicationParameterIndexes=applicationParameterIndexes;
}
public int[] getApplicationParameterIndexes()
{
return this.applicationParameterIndexes;
}

}
