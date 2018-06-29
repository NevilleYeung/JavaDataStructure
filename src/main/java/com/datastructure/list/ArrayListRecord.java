package com.datastructure.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ArrayList代码走读记录
 *
 */
public class ArrayListRecord
{
    public static void main(String[] args)
    {
        /*
        ArrayList，相当于动态数组，非线程安全类；
        继承于AbstractList，实现了List, RandomAccess, Cloneable, java.io.Serializable这些接口。

        ArrayList 继承了AbstractList，实现了List。它是一个数组队列，提供了相关的添加、删除、修改、遍历等功能。
        ArrayList 实现了RandmoAccess接口，即提供了随机访问功能。RandmoAccess是java中用来被List实现，为List提供快速访问功能的。在ArrayList中，我们即可以通过元素的序号快速获取元素对象；这就是快速随机访问。
        ArrayList 实现了Cloneable接口，即覆盖了函数clone()，能被克隆。
        ArrayList 实现java.io.Serializable接口，这意味着ArrayList支持序列化，能通过序列化去传输。
         */

        /*
        ArrayList有两个重要的成员变量，elementData 和 size。
        elementData 是Object[]类型的动态数组，它保存了添加到ArrayList中的元素。
        elementData初始容量默认为10，也可通过构造方法自定义。

        size则是动态数组的元素个数。
         */
        List<String> lista = new ArrayList<String>();
        List<String> listb = new ArrayList<String>(2);


        /*
        每次add元素，都会调用ensureCapacityInternal(size + 1)方法判断数组长度是否需要调整。
        如果数组长度不够，则调用grow(int minCapacity)方法copy到一个新数组中。
        新的容量可简单理解为：newCapacity = oldCapacity + (oldCapacity >> 1)，其中oldCapacity=size + 1，即原来容量的1.5倍。
         */
        lista.add("aaa");
        lista.add("bbb");
        lista.add("ccc");
        lista.add("ddd");
        lista.add("eee");
        lista.add("f");
        lista.add("g");
        lista.add("h");
        System.out.println(lista.size());

        /*
        ArrayList的遍历方式：
        1、通过迭代器遍历。即通过Iterator去遍历
        2、随机访问，通过索引值去遍历
        3、for循环遍历

        三种方式的效率从高到底：2 > 3 > 1
         */
        // 方式1
        String value = null;
        Iterator iter = lista.iterator();
        while (iter.hasNext()) {
            value = (String)iter.next();
            System.out.println("interator: " + value);
        }

        // 方式2
        int size = lista.size();
        for (int i=0; i<size; i++) {
            System.out.println("随机访问: " + lista.get(i));
        }

        // 方式3
        for (String s: lista)
        {
            System.out.println("for循环: " + s);
        }

    }
}
