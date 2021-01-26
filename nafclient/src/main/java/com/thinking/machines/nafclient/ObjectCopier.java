package com.thinking.machines.nafclient;
import java.util.*;
import java.lang.reflect.*;
class Pair<T1,T2>
{
    public T1 first;
    public T2 second;
    Pair(T1 first,T2 second)
    {
        this.first=first;
        this.second=second;
    }
}
public class ObjectCopier
{
    public static void copyObject(Object target,Object source)
    {
        Class sourceClass,targetClass;
        targetClass=target.getClass();
        sourceClass=source.getClass();
        Method targetMethods[];
        targetMethods=targetClass.getMethods();
        Method sourceMethods[];
        sourceMethods=sourceClass.getMethods();
        LinkedList<Pair<Method,Method>> setterGetters=new LinkedList<Pair<Method,Method>>();
        LinkedList<Method> sourceGetterMethods=new LinkedList<>();
        for(Method method:sourceMethods)
        {
            if(isGetter(method))
            {
                sourceGetterMethods.add(method);
            }
        }
        String setterName,getterName;
        Method getterMethod;
        for(Method method:targetMethods)
        {
            if(!isSetter(method)) continue;
            getterMethod=getGetterOf(method,sourceGetterMethods);
            if(getterMethod!=null) setterGetters.add(new Pair(method,getterMethod));
        }
// Information extraction about setter / getter complete
        Class propertyType;
        Object object;
        for(Pair<Method,Method> pair:setterGetters)
        {
            System.out.println(pair.first.getName()+" ----------"+pair.second.getName());
            propertyType=pair.second.getReturnType();
            if(isPrimitive(propertyType))
            {
                try
                {
                    pair.first.invoke(target,pair.second.invoke(source));
                }catch(IllegalAccessException iae){}
                catch(InvocationTargetException ite){}
                catch(Throwable t){}
                continue;
            }
            else
            {
                if(isOneDimensionalArray(propertyType))
                {
                    try
                    {
                        pair.first.invoke(target,pair.second.invoke(source));
                    }catch(IllegalAccessException iae){}
                    catch(InvocationTargetException ite){}
                    catch(Throwable t){}
                    continue;
                }
                else
                {
                    try
                    {
                        Method complexMethods[]=propertyType.getDeclaredMethods();
                        LinkedList<Method> setterComplexMethods=new LinkedList<>();
                        LinkedList<Method> getterComplexMethods=new LinkedList<>();
                        for(Method mm:complexMethods)
                        {
                            if(isSetter(mm))
                            {
                                setterComplexMethods.add(mm);
                            }
                            if(isGetter(mm))
                            {
                                getterComplexMethods.add(mm);
                            }
                        }
                        if(setterComplexMethods.size()==getterComplexMethods.size())
                        {
                            int x=0;
                            for(Method complexSetterMethod:setterComplexMethods)
                            {
                                Method complexGetterMethod=getterComplexMethods.get(x);
                                complexSetterMethod.invoke(pair.second.invoke(target),complexGetterMethod.invoke(pair.second.invoke(source)));
                                x++;
                            }
//pair.first.invoke(target,pair.second.invoke(source));
                        }
                    }catch(IllegalAccessException iae){}
                    catch(InvocationTargetException ite){}
                    catch(Throwable t){}
                }
            }
// do whatever remains
        }
    } // ObjectCopier ends
    public static boolean isPrimitive(Class type)
    {
        if(type.toString().equals("int") || type.toString().equals("class java.lang.Long") || type.toString().equals("class java.lang.String") || type.toString().equals("class java.lang.Double") || type.toString().equals("class java.lang.Float") || type.toString().equals("class java.lang.Short") || type.toString().equals("class java.lang.Byte") || type.toString().equals("class java.lang.Char") || type.toString().equals("class java.lang.Boolean"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public static boolean isOneDimensionalArray(Class type)
    {
        if(type.isArray())
        {
            System.out.println("One dimensional array");
            return true;
        }
        else
        {
            System.out.println("Not a 1-d array");
            return false;
        }
    }

    public static Method getGetterOf(Method setterMethod,LinkedList<Method> getterMethods)
    {
        String setterPropertyName="";
        String setterName=setterMethod.getName();
        Class setterPropertyType;
        Class getterPropertyType;
        if(setterName.length()>3) setterPropertyName=setterName.substring(3);
        String getterName;
        setterPropertyType=setterMethod.getParameterTypes()[0];
        String getterPropertyName;
        for(Method method:getterMethods)
        {
            getterPropertyName="";
            getterName=method.getName();
            if(getterName.length()>3) getterPropertyName=getterName.substring(3);
            getterPropertyType=method.getReturnType();
            if(setterPropertyName.equals(getterPropertyName) && setterPropertyType.equals(getterPropertyType))
            {
                return method;
            }
        }
        return null;
    }
    public static boolean isSetter(Method method)
    {
        return method.getName().startsWith("set") && method.getParameterCount()==1;
    }
    public static boolean isGetter(Method method)
    {
        if(method.getName().startsWith("get")==false) return false;
        if(method.getReturnType().getSimpleName().toUpperCase().equals("VOID")) return false;
        if(method.getParameterCount()>0) return false;
        return true;
    }
}