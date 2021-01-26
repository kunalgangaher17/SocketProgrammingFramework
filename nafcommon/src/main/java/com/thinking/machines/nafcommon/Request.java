package com.thinking.machines.nafcommon;
public class Request implements java.io.Serializable
{
private String path;
private Object arguments[];
private String clientId;
public void setPath(String path)
{
this.path=path;
}
public String getPath()
{
return this.path;
}
public void setArguments(Object arguments[])
{
this.arguments=arguments;
}
public Object[] getArguments()
{
return this.arguments;
}
public void setClientId(String clientId)
{
this.clientId=clientId;
}
public String getClientId()
{
return this.clientId;
}
}