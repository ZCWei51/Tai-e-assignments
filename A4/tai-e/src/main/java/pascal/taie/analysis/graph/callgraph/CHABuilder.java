/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2022 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2022 Yue Li <yueli@nju.edu.cn>
 *
 * This file is part of Tai-e.
 *
 * Tai-e is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Tai-e is distributed in the hope that it will be useful,but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tai-e. If not, see <https://www.gnu.org/licenses/>.
 */

package pascal.taie.analysis.graph.callgraph;

import pascal.taie.World;
import pascal.taie.ir.proginfo.MethodRef;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.classes.Subsignature;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the CHA algorithm.
 */
class CHABuilder implements CGBuilder<Invoke, JMethod> {

    private ClassHierarchy hierarchy;

    @Override
    public CallGraph<Invoke, JMethod> build() {
        hierarchy = World.get().getClassHierarchy();
        return buildCallGraph(World.get().getMainMethod());
    }

    private CallGraph<Invoke, JMethod> buildCallGraph(JMethod entry) {
        DefaultCallGraph callGraph = new DefaultCallGraph();
        callGraph.addEntryMethod(entry);
        // TODO - finish me
        Queue<JMethod> workingList = new ArrayDeque<>();
        Queue<JMethod> reachMethod = new ArrayDeque<>();
//        reachMethod.add(entry);
        workingList.add(entry);
        while (!workingList.isEmpty())
        {
            JMethod jMethod = workingList.poll();
            if(!reachMethod.contains(jMethod)){
                reachMethod.add(jMethod);
                callGraph.addReachableMethod(jMethod);
                // 关于stream的处理-如何遍历
//                Stream<Invoke> callSites = callGraph.callSitesIn(jMethod);
//                List<Invoke> csList = new ArrayList<>();
//                callSites.forEach(csList::add);
//                for(Invoke callSite : csList)
                // 之前stream的遍历有一些问题，导致建立的icfg顺序出现问题
                for(Invoke callSite : callGraph.callSitesIn(jMethod).toList())
                {
                    Set<JMethod> T = resolve(callSite);
                    for(JMethod m : T)
                    {
//                        if(!callGraph.contains(m))
//                        {
                            // 运行出现bug 现在初步怀疑是这里建图的时候有问题！
//                            callGraph.addReachableMethod(m);
                            Edge<Invoke, JMethod> Edge = new Edge<>(CallGraphs.getCallKind(callSite),callSite,m);
                            callGraph.addEdge(Edge);
                            workingList.add(m);
//                        }
                    }
                }
            }
        }
        return callGraph;
    }
    /**
     * Resolves call targets (callees) of a call site via CHA.
     */
    private Set<JMethod> resolve(Invoke callSite) {
        // TODO - finish me
        Set<JMethod> T = new HashSet<JMethod>();
        MethodRef methodRef = callSite.getMethodRef();
        if(CallGraphs.getCallKind(callSite) == CallKind.STATIC)
        {
            T.add(methodRef.getDeclaringClass().getDeclaredMethod(methodRef.getSubsignature()));
        } else if (CallGraphs.getCallKind(callSite) == CallKind.SPECIAL) {
            JMethod jMethod = dispatch(methodRef.getDeclaringClass(),methodRef.getSubsignature());
            if(jMethod != null)
                T.add(jMethod);
        }else if (CallGraphs.getCallKind(callSite) == CallKind.VIRTUAL || CallGraphs.getCallKind(callSite) == CallKind.INTERFACE) {
            JClass jClass = methodRef.getDeclaringClass();
            Queue<JClass> workinglist = new ArrayDeque<>();
            workinglist.add(jClass);
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
//            for(JClass subClass : hierarchy.getDirectSubclassesOf(jClass))
//            {
//                JMethod jMethod =dispatch(subClass,methodRef.getSubsignature());
//                if(jMethod != null)
//                    T.add(jMethod);
//            }
//            JMethod jMethod =dispatch(jClass,methodRef.getSubsignature());
//            if(jMethod != null)
//                T.add(jMethod);
        }
        return T;
    }

    /**
     * Looks up the target method based on given class and method subsignature.
     *
     * @return the dispatched target method, or null if no satisfying method
     * can be found.
     */
    private JMethod dispatch(JClass jclass, Subsignature subsignature) {
        // TODO - finish me
        JMethod jmethod = jclass.getDeclaredMethod(subsignature);
        if (jmethod != null && !jmethod.isAbstract())
        {
            return jmethod;
        }else {
            if (jclass.getSuperClass() != null)
            {
                jmethod = dispatch(jclass.getSuperClass(), subsignature);
                return jmethod;
            }
            else
                return null;
        }
    }
}
