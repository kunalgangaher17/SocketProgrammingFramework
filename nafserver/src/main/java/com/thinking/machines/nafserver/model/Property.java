package com.thinking.machines.nafserver.model;
import java.lang.reflect.*;
public class Property
{
private String name;
private String type;
private Field field;
public Property()
{
}
public void setField(Field field)
{
this.field=field;
}
public Field getField()
{
return this.field;
}
public void setName(String name)
{
this.name=name;
}
public String getName()
{
return this.name;
}
public void setType(String type)
{
this.type=type;
}
public String getType()
{
return this.type;
}
}