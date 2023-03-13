# A4部分总结
## InterConstantPropagation.java
该部分主要是转换节点与转换边的实现，需要根据理解

`transferCallNode`函数没有理解到位

`transferCallEdge`函数犯的错误参考147行的注释

`transferCallToReturnEdge`
1. stmt.getDef().get()之前需要stmt.getDef().isPresent()判断是否存在左值

`transferReturnEdge`函数问题比较大
1. 调用点左值
2. return的多个值没有处理好
   ```java
   //之前理解错了
   return a1,a2,a3
   
   if(...)
        return xxx;
   else
        return xxx;
   //这两种是不同的，后面这种第一次写的时候没有考虑到
   ```
3. return的具体值也不应该是用getDef来获取的
## CHABuilder.java
该部分主要是参考伪代码对算法进行实现，`dispatch`,`resolve`,`buildGrapg`，总体上来说根据伪代码这三个函数的实现大体上与算法保持一致，但是后两个一些细节上面的原因导致有问题

`dispatch`
1. `jmethod.isAbstract()`之前没有判断`jmethod`是否为`null`

`resolve`主要是处理`CallKind.VIRTUAL`与`CallKind.INTERFACE`的情况时需要按照如下情况处理
```java
while (!workinglist.isEmpty())
{
   JClass jClass1 = workinglist.poll();
   if (jClass1 == null)
      continue;
   if(jClass1.isInterface())
   {
      workinglist.addAll(hierarchy.getDirectSubinterfacesOf(jClass1));
      workinglist.addAll(hierarchy.getDirectImplementorsOf(jClass1));
   }else
   {
       workinglist.addAll(hierarchy.getDirectSubclassesOf(jClass1));
   }
      JMethod jMethod = dispatch(jClass1,methodRef.getSubsignature());
   if(jMethod != null)
      T.add(jMethod);
}
 
```

`buildGrapg`主要是两个问题
1. Stream遍历的问题，之前不了解使用toList等函数
2. `reachMethod.add(jMethod)`的位置
## InterSolver.java
1. 刚开始不知道如何遍历icfg
2. Stream遍历的问题（同上
3. while内部workinglist的逻辑按照Edges来遍历，这一点看了参考代码才想到