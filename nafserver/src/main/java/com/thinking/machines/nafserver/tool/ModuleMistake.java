package com.thinking.machines.nafserver.tool;
import java.util.*;
public class ModuleMistake
{
private String className;
private List<String> mistakes;
private List<ServiceMistake> serviceMistakes;
public ModuleMistake(String className)
{
this.className=className;
this.mistakes=new LinkedList<>();
this.serviceMistakes=new LinkedList<>();
}
public String getClassName()
{
return this.className;
}
public void addMistake(String mistake)
{
this.mistakes.add(mistake);
}
public void addServiceMistake(ServiceMistake serviceMistake)
{
this.serviceMistakes.add(serviceMistake);
}
}