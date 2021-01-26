package com.thinking.machines.nafserver;
import com.thinking.machines.nafserver.model.*;
import com.thinking.machines.nafserver.tool.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
public class TMNAFServer
{
    private Application application;
    private ServerSocket serverSocket;
    public TMNAFServer()
    {
        initialize();
    }
    private void initialize()
    {
        try
        {
            application=ApplicationUtility.getApplication();
            serverSocket=new ServerSocket(5000);
            startListening();
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public void startListening()
    {
        try
        {
            while(true)
            {
                System.out.println("Server is listening on port number 5000");
                Socket socket=serverSocket.accept();
                new RequestProcessor(socket,application);
                System.out.println("After request processor");
            }
        }catch(Exception exception)
        {
            System.out.println(exception);
        }
    }
}