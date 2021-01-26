package com.thinking.machines.nafserver;
import static com.thinking.machines.nafcommon.Protocol.*;
import java.io.*;
import java.net.*;
import com.thinking.machines.nafcommon.*;
import com.thinking.machines.nafserver.model.*;
import java.lang.reflect.*;
public class RequestProcessor extends Thread
{
    private Socket client;
    private Application application;
    private Request request;
    private InputStream is;
    private OutputStream os;
    private ByteArrayInputStream bais;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private byte responseBytes[];
    private byte responseLengthInBytes[];
    private int responseLength;
    private int chunkSize;
    private Response response;
    private byte[] requestLengthInBytes;
    private int requestLength;
    private int byteCount;
    private int bytesToRead;
    private int bytesToWrite;
    private ByteArrayOutputStream baos;
    public RequestProcessor(Socket client,Application application)
    {
        this.client=client;
        this.application=application;
        start();
    }
    public void run()
    {
        try
        {
            requestLengthInBytes=new byte[4];
            byte ack[]=new byte[1];
            byte requestBytes[];
            byte chunk[]=new byte[1024];
            is=client.getInputStream();
            byteCount=is.read(requestLengthInBytes);
            requestLength=(requestLengthInBytes[0] & 0xFF)<<24 | (requestLengthInBytes[1] & 0xFF)<<16 | (requestLengthInBytes[2] & 0xFF)<<8 | (requestLengthInBytes[3] & 0xFF);
            ack[0]=(byte)ACKNOWLEDGEMENT_CODE;
            os=client.getOutputStream();
            os.write(ack,0,1);
            os.flush();
            baos=new ByteArrayOutputStream();
            bytesToRead=requestLength;
            while(bytesToRead>0)
            {
                byteCount=is.read(chunk);
                if(byteCount>0)
                {
                    baos.write(chunk,0,byteCount);
                }
                bytesToRead-=byteCount;
            }
            ack[0]=(byte)ACKNOWLEDGEMENT_CODE;
            os.write(ack,0,1);
            os.flush();
            requestBytes=baos.toByteArray();
            bais=new ByteArrayInputStream(requestBytes);
            ois=new ObjectInputStream(bais);
            request=(Request)ois.readObject();
            Object arguments[]=request.getArguments();
            boolean dataType=false;
            File file=null;
            for(Object o:arguments)
            {
                if(isFile(o))
                {
                    file=(File)o;
                    dataType=true;
                }
            }
            if(dataType)
            {
                System.out.println("in getFileFromClient if");
                getFileFromClient(file);
            }
            System.out.println("After getFileFromClient");
            response=methodInvoker(request);
            System.out.println("After method invoker");
//Before sending response to client
            System.out.println("Before sending response to client");
            baos=new ByteArrayOutputStream();
            oos=new ObjectOutputStream(baos);
            oos.writeObject(response);
            oos.flush();
            responseBytes=baos.toByteArray();
            responseLength=responseBytes.length;
            System.out.println("Response length :"+responseLength);
            responseLengthInBytes=new byte[20];
/*
responseLengthInBytes[0]=(byte)(responseLength>>24);
responseLengthInBytes[1]=(byte)(responseLength>>16);
responseLengthInBytes[2]=(byte)(responseLength>>8);
responseLengthInBytes[3]=(byte)responseLength;
*/
            int k=19;
            byte e=0;
            int f=responseLength;
            while(k>=0)
            {
                e=(byte)(f%10);
                f=f/10;
                responseLengthInBytes[k]=e;
                k--;
            }
            for(int x=0;x<=19;x++)
            {
                System.out.println("Response length in bytes :"+responseLengthInBytes[x]);
            }
            os.write(responseLengthInBytes,0,20);
            os.flush();
            System.out.println("After sending response length");
            byteCount=is.read(ack);
            if(ack[0]!=ACKNOWLEDGEMENT_CODE) throw new RuntimeException("Unable to recieve acknowledgement");
            bytesToWrite=responseLength;
            chunkSize=1024;
            int i=0;
            while(bytesToWrite>0)
            {
                if(bytesToWrite<chunkSize) chunkSize=bytesToWrite;
                os.write(responseBytes,i,chunkSize);
                os.flush();
                i+=chunkSize;
                bytesToWrite-=chunkSize;
            }
            byteCount=is.read(ack);
            if(ack[0]!=ACKNOWLEDGEMENT_CODE) throw new RuntimeException("Unable to recieve acknowledgement");
            client.close();
//After response is sended to client
        }catch(Exception exception)
        {
            System.out.println(exception);
        }
    }
    public boolean isFile(Object object)
    {
        if(object instanceof File)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public Response methodInvoker(Request request)
    {
        Response response=null;
        try
        {
            Service service=application.getService(request.getPath());
            Object result;
            if(request.getArguments().length==service.getNumberOfParameters())
            {
                Object args[]=request.getArguments();
                try
                {
                    System.out.println("In request processorrrrrrrrrrrrrr:"+service.getModule().getServiceClass().newInstance());
                    result=service.getMethod().invoke(service.getModule().getServiceObject(),args);
                    response=new Response();
                    response.setIsVoid(service.getIsVoid());
                    response.setArguments(args);
                    if(result==null)
                    {
                        response.setResult(null);
                    }
                    if(!response.getIsVoid())
                    {
                        response.setResult(result);
                    }
                    response.setIsSuccessful(true);
                }catch(IllegalAccessException illegalAccessException)
                {
                    response.setIsError(true);
                    response.setIsSuccessful(false);
                    response.setException(illegalAccessException.getMessage());
                }catch(IllegalArgumentException illegalArgumentException)
                {
                    response.setIsError(true);
                    response.setIsSuccessful(false);
                    response.setException("Invalid parameters passes parameter Required :"+service.getMethod().getParameterTypes().toString());
                }
                catch(InvocationTargetException ite)
                {
                    response.setIsException(true);
                }
            }
            return response;
        }catch(Exception exception)
        {
            System.out.println(exception);
        }
        return response;
    }


    public void getFileFromClient(File file)
    {
        try
        {
            System.out.println("getFileFromClient");
            byte response[]={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
            OutputStream outputStream=client.getOutputStream();
            int headerSize=20;
            byte header[]=new byte[headerSize];
            InputStream inputStream=client.getInputStream();
            System.out.println("Serialized form of file name received");
            inputStream.read(header);
            outputStream.write(response,0,headerSize);
            outputStream.flush();
            long lengthOfFile;
            lengthOfFile=0;
            int e=headerSize-1;
            int f=1;
            while(e>=0)
            {
                lengthOfFile=lengthOfFile+(header[e]*f);
                e--;
                f=f*10;
            }
            System.out.println("Length of file :"+lengthOfFile);
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            BufferedOutputStream bos=new BufferedOutputStream(fileOutputStream);
            byte bytes[]=new byte[1024];
            int i=0;
            int bytesRead;
            while(true)
            {
                bytesRead=inputStream.read(bytes);
                if(bytesRead<0) break;
                i=i+bytesRead;
                bos.write(bytes,0,bytesRead);
                bos.flush();
                if(i==lengthOfFile) break;
            }
            fileOutputStream.close();
            bos.close();
            outputStream.write(response,0,headerSize);
            outputStream.flush();
            System.out.println("File received");
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}