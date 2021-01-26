package com.library.main;
import com.thinking.machines.nafclient.*;
import com.thinking.machines.nafcommon.*;
import java.io.*;
public class Main
{
    public static void main(String gg[])
    {
        try
        {
            TMNAFClient tmnafClient=new TMNAFClient("localhost",5000);
            System.out.println("Result :"+tmnafClient.process("/serviceA/add",10,20));
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}