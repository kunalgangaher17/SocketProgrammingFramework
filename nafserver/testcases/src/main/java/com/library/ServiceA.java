package com.library;
import com.thinking.machines.nafserver.annotation.*;
import com.thinking.machines.nafcommon.*;
import java.io.*;
@Path("/serviceA")
public class ServiceA
{
    @Autowired
    private String stringModel;
    public String getWhatever()
    {
        return "Whatever";
    }
    @Path("/add")
    public int add(int a,int b)
    {
        System.out.println("Add");
        return a+b;
    }
    @Path("/getProduct")
    public int getProduct(int a,int b)
    {
        System.out.println("Get product");
        return a*b;
    }
    public int getDiff(int e,int f)
    {
        return e-f;
    }
    @Path("/tom")
    public String tom(aaa[] arrays)
    {
        System.out.println("Tom");
        for(int i=0;i<arrays.length;i++)
        {
            System.out.println("Tom :"+arrays[i].getNum());
        }
        return "aaa array from tom";
    }
    @Path("/printStringModel")
    public void printStringModel()
    {
        System.out.println("String model value :"+stringModel);
    }
    @Path("/setFile")
    public int setFile(File file)
    {
        try
        {
            Runtime r=Runtime.getRuntime();
            System.out.println(file.getPath());
            System.out.println("Name :"+file.getName());
            return 200;
        }catch(Exception e)
        {
            System.out.println(e);
        }
        return 200;
    }
}