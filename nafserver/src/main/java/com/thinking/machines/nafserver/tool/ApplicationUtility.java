package com.thinking.machines.nafserver.tool;
import java.text.*;
import com.thinking.machines.nafserver.model.*;
import com.thinking.machines.nafserver.annotation.*;
import com.thinking.machines.nafcommon.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;
import java.util.regex.*;
public class ApplicationUtility
{
    private static Application application=null;
    private static LinkedList<Object> applicationScopeMethodsReturnedValue=new LinkedList<>();
    private ApplicationUtility() {}
    public static Application getApplication()
    {
        String path="";
        if(application!=null) return application;
        String mainPackage=null;
        try
        {
            throw new RuntimeException();
        }catch(RuntimeException re)
        {
            StackTraceElement e[]=re.getStackTrace();
            String className=e[e.length-1].getClassName();
            try
            {
                mainPackage=Class.forName(className).getPackage().getName();
            }catch(Exception exception)
            {
                System.out.println("************* SERIOUS PROBLEM **************");
                System.exit(0);
            }
        }
        HashMap<String,List<ModuleMistake>> duplicateServices=new HashMap<>();
        HashMap<String,Service> services=new HashMap<>();
        LinkedList<ModuleMistake> moduleMistakes=new LinkedList<>();
        String packageToAnalyze=mainPackage;
        try
        {
            URLClassLoader ucl=(URLClassLoader)ClassLoader.getSystemClassLoader();
            URL urls[]=ucl.getURLs();
            String classPathEntry;
            ZipInputStream zis;
            ZipEntry ze;
            String zipEntryName;
            String packageName;
            String className;
            int dotPosition;
            String folderName;
            File directory;
            File files[];
            String fileName;
            for(URL u:urls)
            {
                classPathEntry=u.getFile();
                if(classPathEntry.endsWith(".jar"))
                {
// code to analyze jar file contents
                    zis=new ZipInputStream(u.openStream());
                    ze=zis.getNextEntry();
                    while(ze!=null)
                    {
                        zipEntryName=ze.getName();
                        if(zipEntryName.endsWith(".class"))
                        {
                            zipEntryName=zipEntryName.replaceAll("\\\\","\\.");
                            zipEntryName=zipEntryName.replaceAll("/","\\.");
                            dotPosition=zipEntryName.lastIndexOf(".",zipEntryName.length()-7);
                            if(dotPosition==-1)
                            {
                                packageName="";
                                className=zipEntryName;
                            }
                            else
                            {
                                packageName=zipEntryName.substring(0,dotPosition);
                                className=zipEntryName.substring(dotPosition+1);
                            }
                            if(packageName.startsWith(packageToAnalyze))
                            {
                                try
                                {
                                    Class ccc=Class.forName(zipEntryName.substring(0,zipEntryName.length()-6));
                                    System.out.println("Call of a module scanner function");
                                    moduleScanner(ccc,services,moduleMistakes,duplicateServices);
                                }catch(Throwable ee)
                                {
                                    System.out.println(ee); // remove after testing
                                }
                            }
                        }
                        ze=zis.getNextEntry();
                    }
                }
                else
                {
// code to analyze folder
                    folderName=classPathEntry+packageToAnalyze;
                    if(File.separator.equals("\\\\"))
                    {
                        folderName=folderName.replaceAll("\\.","\\\\");
                    }
                    else
                    {
                        folderName=folderName.replaceAll("\\.","/");
                    }
                    directory=new File(folderName);
                    if(directory.exists()==false) continue;
                    Stack<File> stack=new Stack<>();
                    stack.push(directory);
                    File fifi;
                    while(stack.size()>0)
                    {
                        fifi=stack.pop();
                        files=fifi.listFiles();
                        for(File file:files)
                        {
                            if(file.isDirectory())
                            {
                                stack.push(file);
                                continue;
                            }
                            if(file.getName().endsWith(".class"))
                            {
                                className=file.getName();
                                packageName=file.getAbsolutePath().substring(classPathEntry.length()-1);
                                packageName=packageName.substring(0,packageName.length()-className.length()-1);
                                packageName=packageName.replaceAll("\\\\","\\.");
                                packageName=packageName.replaceAll("/","\\.");
                                try
                                {
                                    Class ccc=Class.forName(packageName+"."+className.substring(0,className.length()-6));
                                    System.out.println("Call of a module scanner function");
                                    moduleScanner(ccc,services,moduleMistakes,duplicateServices);
                                }catch(Throwable ee)
                                {
                                    System.out.println(ee); // remove after testing
                                }
                            }
                        }
                    }//stack.size()>0
                }
            }
        }catch(Exception e)
        {
            StackTraceElement tt[]=e.getStackTrace();
            for(StackTraceElement t:tt)
            {
                System.out.println(t);
            }
        }
        if(moduleMistakes.size()>0 || duplicateServices.size()>0 || services.size()==0)
        {
            try
            {
                SimpleDateFormat sdf=new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");
                String fileName="error_"+sdf.format(new java.util.Date())+".err";
                File file=new File(fileName);
                if(file.exists()) file.delete();
                RandomAccessFile raf=new RandomAccessFile(file,"rw");
                if(moduleMistakes.size()==0 && duplicateServices.size()==0 && services.size()==0)
                {
                    raf.writeBytes("No  services defined");
                    raf.close();
                    System.out.println("Errors in application,see file ("+fileName+") for details.");
                    System.exit(0);
                }
                if(moduleMistakes.size()>0)
                {
                    raf.writeBytes("Some module mistakes\r\n");
                }
                if(duplicateServices.size()>0)
                {
                    raf.writeBytes("Some service mistakes\r\n");
                }
                raf.close();
                System.out.println("Errors in application,see file ("+fileName+") for details.");
                System.exit(0);
            }catch(IOException ioException)
            {
                System.out.println(ioException.getMessage());
                System.exit(0);
            }
            System.exit(0);
        }
        System.out.println("Services :"+services.size()+" in getApplication()");
        application=new Application();
        application.setServices(services);
        injectAutowiredPropertyValue(applicationScopeMethodsReturnedValue,services);
        return application;
    }
    public static void moduleScanner(Class ccc,HashMap<String,Service> services,LinkedList<ModuleMistake> moduleMistakes,HashMap<String,List<ModuleMistake>> duplicateServices)
    {
        System.out.println("Module scanner function");
        Class sessionScopeClass=SessionScope.class;
        Class applicationScopeClass=ApplicationScope.class;
        int j;
        Class parameterTypes[];
        ArrayList<Integer> indicesList;
        int indices[];
        LinkedList<Property> autoWiredProperties=new LinkedList<>();
        Property property;
        Field fields[];
        Field field;
        Module module=null;
        boolean flag;
        LinkedList<ModuleMistake> linkedList;
        ModuleMistake mm;
        ServiceMistake sm;
        Service s;
        Service service=null;
        String servicePathString;
        boolean isValidModulePath,isValidServicePath;
        isValidModulePath=false;
        isValidServicePath=false;
        Class pathClass=Path.class;
        Path modulePath=null;
        String modulePathString=null;
        ModuleMistake moduleMistake=null;
        if(ccc.isAnnotationPresent(pathClass))
        {
            modulePath=(Path)ccc.getAnnotation(pathClass);
            modulePathString=modulePath.value();
            isValidModulePath=isValidPath(modulePathString);
            if(isValidModulePath==false)
            {
                moduleMistake=new ModuleMistake(ccc.getName());
                moduleMistake.addMistake("Invalid path : "+modulePathString);
            }
            if(Modifier.isPublic(ccc.getModifiers())==false)
            {
                if(moduleMistake==null) moduleMistake=new ModuleMistake(ccc.getName());
                moduleMistake.addMistake("class is not declared as public");
            }
        }
        if(ccc.isAnnotationPresent(ApplicationAware.class))
        {
            try
            {
                Method applicationAwareMethods[]=ccc.getDeclaredMethods();
                for(Method applicationAwareMethod:applicationAwareMethods)
                {
                    if(applicationAwareMethod.isAnnotationPresent(applicationScopeClass))
                    {
                        Object o=applicationAwareMethod.invoke(ccc.newInstance());
                        applicationScopeMethodsReturnedValue.add(o);
                        System.out.println("Application scope annotation is present");
                    }
                }
            }catch(Exception exception)
            {
                System.out.println(exception.getMessage());
                System.out.println("exception in checking of isApplicationScope");
            }
        }
        Method  methods[]=ccc.getDeclaredMethods();
        Path methodPath=null;
        String methodPathString=null;
        ServiceMistake serviceMistake;
        for(Method m:methods)
        {
            System.out.println("Method loop");
            System.out.println("Analyzing : "+m+" for : "+pathClass.getName());
            serviceMistake=null;
            if(m.isAnnotationPresent(pathClass))
            {
                methodPath=(Path)m.getAnnotation(pathClass);
                methodPathString=methodPath.value();
                isValidServicePath=isValidPath(methodPathString);
                if(isValidServicePath==false)
                {
                    serviceMistake=new ServiceMistake(m.toString());
                    serviceMistake.addMistake("Invalid path :"+methodPathString);
                }
                if(!Modifier.isPublic(m.getModifiers()))
                {
                    if(serviceMistake==null) serviceMistake=new ServiceMistake(m.toString());
                    serviceMistake.addMistake("method is not declared as public");
                }
                if(isValidModulePath && isValidServicePath)
                {
                    servicePathString=modulePathString+methodPathString;
                    if(services.containsKey(servicePathString)==false)
                    {
                        if(moduleMistake==null && serviceMistake==null)
                        {
                            if(duplicateServices.containsKey(servicePathString)==false)
                            {
                                if(duplicateServices.size()==0 && moduleMistakes.size()==0)
                                {
                                    service=new Service();
                                    if(module==null)
                                    {
                                        module=new Module();
                                        module.setServiceClass(ccc);
                                        if(ccc.isAnnotationPresent(ApplicationAware.class)) module.setIsApplicationAware(true);
                                        if(ccc.isAnnotationPresent(SessionAware.class)) module.setIsSessionAware(true);
                                        autoWiredProperties=new LinkedList<>();
                                        fields=ccc.getDeclaredFields();
                                        for(Field ff:fields)
                                        {
                                            if(ff.getAnnotation(Autowired.class)!=null)
                                            {
                                                property=new Property();
                                                property.setName(ff.getName());
                                                property.setType(ff.getType().toString());
                                                property.setField(ff);
                                                autoWiredProperties.add(property);
                                            }
                                        }
                                        module.setAutowiredProperties(autoWiredProperties);
                                    }
                                    service.setModule(module);
                                    service.setMethod(m);
                                    service.setNumberOfParameters(m.getParameterCount());
                                    if(m.getReturnType().getSimpleName().equalsIgnoreCase("VOID"))
                                    {
                                        service.setIsVoid(true);
                                    }
                                    parameterTypes=m.getParameterTypes();
                                    indicesList=new ArrayList<>();
                                    for(j=0;j<parameterTypes.length;j++)
                                    {
                                        if(parameterTypes[j].equals(sessionScopeClass))
                                        {
                                            indicesList.add(j);
                                        }
                                    }
                                    if(indicesList.size()>0)
                                    {
                                        service.setInjectSession(true);
                                        indices=new int[indicesList.size()];
                                        j=0;
                                        for(Integer iii:indicesList)
                                        {
                                            indices[j]=iii;
                                            j++;
                                        }
                                        service.setSessionParameterIndexes(indices);
                                    }
                                    indicesList=new ArrayList<>();
                                    for(j=0;j<parameterTypes.length;j++)
                                    {
                                        if(parameterTypes[j].equals(applicationScopeClass))
                                        {
                                            indicesList.add(j);
                                        }
                                    }
                                    if(indicesList.size()>0)
                                    {
                                        service.setInjectApplication(true);
                                        indices=new int[indicesList.size()];
                                        j=0;
                                        for(Integer iii:indicesList)
                                        {
                                            indices[j]=iii;
                                            j++;
                                        }
                                        service.setApplicationParameterIndexes(indices);
                                    }
                                    System.out.println("----------------------");
                                    services.put(servicePathString,service);
                                }
                            }
                            else
                            {
                                linkedList=(LinkedList<ModuleMistake>)duplicateServices.get(servicePathString);
                                flag=false;
                                for(ModuleMistake mmm:linkedList)
                                {
                                    if(mmm.getClassName().equals(ccc.getName()))
                                    {
                                        sm=new ServiceMistake(m.toString());
                                        mmm.addServiceMistake(sm);
                                        flag=true;
                                        break;
                                    }
                                }
                                if(!flag)
                                {
                                    mm=new ModuleMistake(ccc.getName());
                                    sm=new ServiceMistake(m.toString());
                                    mm.addServiceMistake(sm);
                                    linkedList.add(mm);
                                }
                            }
                        }
                    }
                    else
                    {
                        s=services.remove(servicePathString);
                        mm=new ModuleMistake(s.getModule().getServiceClass().getName());
                        sm=new ServiceMistake(s.getMethod().toString());
                        mm.addServiceMistake(sm);
                        linkedList=new LinkedList<>();
                        linkedList.add(mm);
                        if(ccc.equals(s.getModule().getServiceClass()))
                        {
                            sm=new ServiceMistake(m.toString());
                            mm.addServiceMistake(sm);
                        }
                        else
                        {
                            mm=new ModuleMistake(ccc.getName());
                            sm=new ServiceMistake(m.toString());
                            mm.addServiceMistake(sm);
                            linkedList.add(mm);
                        }
                        duplicateServices.put(servicePathString,linkedList);
                    }
                }
                if(serviceMistake!=null)
                {
                    if(moduleMistake==null) moduleMistake=new ModuleMistake(ccc.getName());
                    moduleMistake.addServiceMistake(serviceMistake);
                }
            }
        }
        if(moduleMistake!=null)
        {
            moduleMistakes.add(moduleMistake);
        }
        System.out.println("mm : "+moduleMistakes.size()+",ds : "+duplicateServices.size()+", services: "+services.size());
        int x=0;
        while(x<duplicateServices.size())
        {
            Object[] objects=duplicateServices.values().toArray();
            List<ModuleMistake> modulesMistakes=(List<ModuleMistake>)objects[x];
            for(ModuleMistake mmm:moduleMistakes)
            {
                System.out.println("Mistake in "+mmm.getClassName()+" class");
            }
            x++;
        }
    }


    private static void injectAutowiredPropertyValue(LinkedList<Object> returnedValues,HashMap<String,Service> services)
    {
        try
        {
            Set<String> keys=services.keySet();
            Iterator<String> iterator=keys.iterator();
            HashMap<String,Module> modules=new HashMap<>();
            Module module;
            String serviceClassName=" ";
            while(iterator.hasNext())
            {
                module=services.get(iterator.next()).getModule();
                if(serviceClassName.equals(module.getServiceClass().getName()))
                {
                }
                else
                {
                    serviceClassName=module.getServiceClass().getName();
                    LinkedList<Property> autowiredProperties=module.getAutowiredProperties();
                    for(Property property:autowiredProperties)
                    {
                        for(Object returnValue:returnedValues)
                        {
                            if(returnValue.getClass().getName().equals(property.getType().split(" ")[1]))
                            {
                                Field field=Class.forName(module.getServiceClass().getName()).getDeclaredField(property.getName());
                                field.setAccessible(true);
                                Object o=module.getServiceClass().newInstance();
                                field.set(o,returnValue);
                                module.setServiceObject(o);
                                System.out.println("Service object is setted.....................");
                            }
                        }
                    }
                }
            }
            System.out.println("Modules size :"+modules.size());
            for(Object returnValue:returnedValues)
            {
                System.out.println("Application scope returned value :"+returnValue.getClass().getName());
            }
        }catch(Exception exception)
        {
            System.out.println("Catch of inject method :"+exception.getMessage());
        }
    }

    private static boolean isValidPath(String path)
    {
        String pattern="[/][^?-](.*)(\\w)[^/-]|(\\d)[^/-]";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(path);
        if(m.matches())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}