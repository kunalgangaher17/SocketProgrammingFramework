package com.thinking.machines.nafserver.tool;
import java.util.*;
public class ServiceMistake
{
private String method;
private List<String> mistakes;
public ServiceMistake(String method)
{
this.method=method;
this.mistakes=new LinkedList<>();
}
public void addMistake(String mistake)
{
this.mistakes.add(mistake);
}
}