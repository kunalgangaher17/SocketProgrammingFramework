package com.thinking.machines.nafclient;
import com.thinking.machines.nafcommon.*;
import static com.thinking.machines.nafcommon.Protocol.*;
import java.io.*;
import java.net.*;
public class TMNAFClient
{
    private String ipAdress;
    private int portNumber;
    private Request request;
    private Socket socket;
    public TMNAFClient(String ipAdress,int portNumber)
    {
        this.ipAdress=ipAdress;
        this.portNumber=portNumber;
    }
    public Object process(Object... args)
    {
        this.request=new Request();
        request.setPath((String)args[0]);
        Object[] arguments=new Object[args.length-1];
        int k,j,fileIndex=0;
        boolean isFilePresent=false;
        for(k=0,j=1;j<args.length;k++,j++)
        {
            arguments[k]=args[j];
            if(arguments[k] instanceof File)
            {
                isFilePresent=true;
                fileIndex=k;
            }
        }
        request.setArguments(arguments);
        try
        {
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ObjectOutputStream oos=new ObjectOutputStream(baos);
            oos.writeObject(request);
            oos.flush();
            byte requestBytes[]=baos.toByteArray();
            int requestSize=requestBytes.length;
            System.out.println("Request size :"+requestSize);
            byte[] requestSizeInBytes=new byte[4];
            requestSizeInBytes[0]=(byte)(requestSize>>24);
            requestSizeInBytes[1]=(byte)(requestSize>>16);
            requestSizeInBytes[2]=(byte)(requestSize>>8);
            requestSizeInBytes[3]=(byte)requestSize;
            socket=new Socket("localhost",5000);
            OutputStream os=socket.getOutputStream();
            os.write(requestSizeInBytes,0,4);
            os.flush();
            InputStream is=socket.getInputStream();
            byte ack[]=new byte[1];
            int byteCount=is.read(ack);
            if(ack[0]!=79) throw new RuntimeException("Unable to receive acknowledgement");
            int bytesToSend=requestSize;
            int chunkSize=1024;
            int i=0;
            while(bytesToSend>0)
            {
                if(bytesToSend<chunkSize) chunkSize=bytesToSend;
                os.write(requestBytes,i,chunkSize);
                os.flush();
                i=i+chunkSize;
                bytesToSend-=chunkSize;
            }
            if(isFilePresent)
            {
                File file=(File)arguments[fileIndex];
                sendFileToServer(file.getAbsolutePath());
            }
            System.out.println("After send File To Server");
//Receiving response from server

            byte [] responseLengthInBytes=new byte[20];
            System.out.println("Before requesting for stream "+os);
            System.out.println("After requesting for stream");
            byteCount=is.read(responseLengthInBytes);

            System.out.println("-------------------------------------");
            byteCount=is.read(responseLengthInBytes);

            int responseLength=0;
//responseLength=(responseLengthInBytes[0] & 0xFF) << 24 | (responseLengthInBytes[1] & 0xFF) << 16 | (responseLengthInBytes[2] & 0xFF) << 8 | (responseLengthInBytes[3] & 0xFF);
            int e=1;
            for(int k1=19;k1>=0;k1--)
            {
                responseLength+=((int)responseLengthInBytes[k1]*e);
                e=e*10;
            }
            ack[0]=(byte)ACKNOWLEDGEMENT_CODE;
            System.out.println("After accepting response length :"+responseLength);
            os.write(ack,0,1);
            os.flush();
            baos=new ByteArrayOutputStream();
            byte chunk[]=new byte[1024];
            int bytesToRead=responseLength;
            while(bytesToRead>0)
            {
                byteCount=is.read(chunk);
                if(byteCount>0)
                {
                    baos.write(chunk,0,byteCount);
                    baos.flush();
                }
                bytesToRead-=byteCount;
            }
            os.write(ack,0,1);
            os.flush();
            byte responseBytes[]=baos.toByteArray();
            ByteArrayInputStream bais=new ByteArrayInputStream(responseBytes);
            ObjectInputStream ois=new ObjectInputStream(bais);
            Response response=(Response)ois.readObject();
            System.out.println("Result :"+response.getResult());
            socket.close();
            return response.getResult();

//Response is received from server
        }catch(Exception exception)
        {
            System.out.println("TMNAFCLIENT "+exception);
        }
        return null;
    }


    public void sendFileToServer(String absolutePath)
    {
        try
        {
            File tmpFile=new File("c:/framework/naf/nafclient/");
            File file=new File(absolutePath);
            if(file.exists()==false)
            {
                System.out.println("File not found :"+file.getPath());
//return;
            }
            byte response[]={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
            InputStream inputStream=socket.getInputStream();
            OutputStream outputStream;
            outputStream=socket.getOutputStream();
            System.out.println("In send file to server outputStream "+outputStream);
            int i;
            int bufferSize=1024;
            int numberOfBytesToWrite=bufferSize;
            i=0;
            long lengthOfFile=file.length();
            System.out.println("Length of file :"+lengthOfFile);
            byte header[]=new byte[20];
            int headerSize=20;
            int k=headerSize-1;
            int f=(int)lengthOfFile;
            while(k>=0)
            {
                header[k]=(byte)(f%10);
                f=f/10;
                k--;
            }
            outputStream.write(header,0,headerSize);
            outputStream.flush();
            inputStream.read(response);
            FileInputStream fileInputStream;
            fileInputStream=new FileInputStream(file);
            BufferedInputStream bis=new BufferedInputStream(fileInputStream);
            byte contents[]=new byte[1024];
            int bytesRead;
            i=0;
            int number=0;
            while(i<lengthOfFile)
            {
                bytesRead=bis.read(contents);
                if(bytesRead<0) break;
                outputStream.write(contents,0,bytesRead);
                outputStream.flush();
                i=i+bytesRead;
            }
            fileInputStream.close();
            bis.close();
            byte response1[]=new byte[20];
            inputStream.read(response1);
            System.out.println("Response length :"+response.length);
            System.out.println("Uploaded");
            for(int e=0;e<=19;e++)
            {
                System.out.print(response1[e]+" ");
            }
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}