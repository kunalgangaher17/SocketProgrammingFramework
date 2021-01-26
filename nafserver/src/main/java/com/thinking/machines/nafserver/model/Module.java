package com.thinking.machines.nafserver.model;
import java.util.*;
public class Module
{
    private Class serviceClass;
    private Object serviceObject;
    private LinkedList<Property> autowiredProperties;
    private boolean isSessionAware;
    private boolean isApplicationAware;
    public Module()
    {
    }
    public void setServiceObject(Object serviceObject)
    {
        this.serviceObject=serviceObject;
    }
    public Object getServiceObject()
    {
        return this.serviceObject;
    }
    public void setServiceClass(Class serviceClass)
    {
        this.serviceClass=serviceClass;
    }
    public Class getServiceClass()
    {
        return this.serviceClass;
    }
    public void setAutowiredProperties(LinkedList<Property> autowiredProperties)
    {
        this.autowiredProperties=autowiredProperties;
    }
    public LinkedList<Property> getAutowiredProperties()
    {
        return this.autowiredProperties;
    }
    public void setIsSessionAware(boolean isSessionAware)
    {
        this.isSessionAware=isSessionAware;
    }
    public boolean getIsSessionAware()
    {
        return this.isSessionAware;
    }
    public void setIsApplicationAware(boolean isApplicationAware)
    {
        this.isApplicationAware=isApplicationAware;
    }
    public boolean getIsApplicationAware()
    {
        return this.isApplicationAware;
    }
}