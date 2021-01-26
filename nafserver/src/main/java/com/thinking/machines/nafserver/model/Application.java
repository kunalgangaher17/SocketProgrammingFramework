package com.thinking.machines.nafserver.model;
import com.thinking.machines.nafcommon.*;
import java.util.*;
public class Application
{
    private HashMap<String,Service> services;
    public Application()
    {
        this.services=new HashMap<>();
    }
    public void add(String path,Service service) throws ApplicationException
    {
        if(services.containsKey(path)) throw new ApplicationException("Yet to be finalized");
        services.put(path,service);
    }
    public boolean containsService(String path)
    {
        return services.containsKey(path);
    }
    public Service getService(String path) throws ApplicationException
    {
        Service service=services.get(path);
        if(service==null) throw new ApplicationException("Invalid path :"+path);
        return service;
    }
    public HashMap<String,Service> getServices() throws ApplicationException
    {
        if(this.services.size()==0) throw new ApplicationException("No services");
        return services;
    }
    public void setServices(HashMap<String,Service> services)
    {
        this.services=services;
    }
}