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

package pascal.taie.analysis.dataflow.inter;

import org.checkerframework.checker.units.qual.C;
import pascal.taie.analysis.dataflow.analysis.constprop.CPFact;
import pascal.taie.analysis.dataflow.analysis.constprop.ConstantPropagation;
import pascal.taie.analysis.dataflow.analysis.constprop.Value;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.analysis.graph.cfg.CFGBuilder;
import pascal.taie.analysis.graph.icfg.CallEdge;
import pascal.taie.analysis.graph.icfg.CallToReturnEdge;
import pascal.taie.analysis.graph.icfg.NormalEdge;
import pascal.taie.analysis.graph.icfg.ReturnEdge;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.exp.Exp;
import pascal.taie.ir.exp.InvokeExp;
import pascal.taie.ir.exp.LValue;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.DefinitionStmt;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JMethod;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of interprocedural constant propagation for int values.
 */
public class InterConstantPropagation extends
        AbstractInterDataflowAnalysis<JMethod, Stmt, CPFact> {

    public static final String ID = "inter-constprop";

    private final ConstantPropagation cp;

    public InterConstantPropagation(AnalysisConfig config) {
        super(config);
        cp = new ConstantPropagation(new AnalysisConfig(ConstantPropagation.ID));
    }

    @Override
    public boolean isForward() {
        return cp.isForward();
    }

    @Override
    public CPFact newBoundaryFact(Stmt boundary) {
        IR ir = icfg.getContainingMethodOf(boundary).getIR();
        return cp.newBoundaryFact(ir.getResult(CFGBuilder.ID));
    }

    @Override
    public CPFact newInitialFact() {
        return cp.newInitialFact();
    }

    @Override
    public void meetInto(CPFact fact, CPFact target) {
        cp.meetInto(fact, target);
    }

    @Override
    protected boolean transferCallNode(Stmt stmt, CPFact in, CPFact out) {
        // TODO - finish me
//        return false;
//        if(stmt instanceof DefinitionStmt definitionStmt && definitionStmt.getLValue() instanceof Var var && cp.canHoldInt(var)){
//            Exp exp = definitionStmt.getRValue();
//            Value evaluateValue = cp.evaluate(exp, in);
//            CPFact inCopy = in.copy();
//            inCopy.remove(var);
//            boolean changed = out.update(var, evaluateValue);
//            changed |= out.copyFrom(inCopy);
//            out.remove(var);
//            return changed;
//
//        }else {
//            return out.copyFrom(in);
//        }
        return out.copyFrom(in);
    }

    @Override
    protected boolean transferNonCallNode(Stmt stmt, CPFact in, CPFact out) {
        // TODO - finish me
        return cp.transferNode(stmt,in,out);
//        return false;
    }

    @Override
    protected CPFact transferNormalEdge(NormalEdge<Stmt> edge, CPFact out) {
        // TODO - finish me
//        return null;
        return out;
    }

    @Override
    protected CPFact transferCallToReturnEdge(CallToReturnEdge<Stmt> edge, CPFact out) {
        // TODO - finish me
//        return null;
        Stmt stmt = edge.getSource();
        CPFact outCpoy = out.copy();
        if(stmt.getDef().isPresent() && stmt.getDef().get() instanceof Var var)
//            outCpoy.remove(var);
            outCpoy.update(var,Value.getUndef());
        return outCpoy;
    }

    @Override
    protected CPFact transferCallEdge(CallEdge<Stmt> edge, CPFact callSiteOut) {
        // TODO - finish me
//        return null;
        Stmt stmt = edge.getSource();
        List<Var> invokeList = new ArrayList<>();
        if(stmt instanceof Invoke invoke)
        {
            invokeList = invoke.getInvokeExp().getArgs();
        }
        CPFact CallEdgeIn = new CPFact();
        JMethod jMethod = edge.getCallee();
        IR calleeIR = jMethod.getIR();
        for(Var var:calleeIR.getParams())
        {
            // 之前犯的一个错误，这里调用callSiteOut.get时输入的var应该时调用点调用函数时的变量，但是之前直接用callee的参数去输入返回肯定就是null了
            Value value = callSiteOut.get(invokeList.get(var.getIndex()));
            CallEdgeIn.update(var,value);
        }
        return CallEdgeIn;
    }
//
    @Override
    protected CPFact transferReturnEdge(ReturnEdge<Stmt> edge, CPFact returnOut) {
        // TODO - finish me
//        return null;
        CPFact ReturnEdgeOut = new CPFact();
        Stmt callSite = edge.getCallSite();
//        Stmt stmt = edge.getSource();
        Stmt stmtTarget = edge.getTarget();
//        Var varDefTarget =  (Var) stmtTarget.getDef().get();

//        Var tmpVar = edge.getReturnVars().stream().toList().get(0);
//        Value tmpValue = returnOut.get(tmpVar);
        Value tmpValue = Value.getUndef();
        for(Var var : edge.getReturnVars())
        {

            tmpValue = cp.meetValue(returnOut.get(var),tmpValue);
            // 这里处理returnEdge的处理我肯定写的有问题
//            if(stmt.getDef().isPresent() && var == stmt.getDef().get())
//            {
//                ReturnEdgeOut.update(varDefCS,returnOut.get(var));
//            }
        }
        if(callSite.getDef().isPresent()) {
            Var varDefCS = (Var) callSite.getDef().get();
            ReturnEdgeOut.update(varDefCS, tmpValue);
        }
//        ReturnEdgeOut.update(varDefTarget,tmpValue);
        return ReturnEdgeOut;
    }


//    @Override
//    protected CPFact transferReturnEdge(ReturnEdge<Stmt> edge, CPFact returnOut) {
//        CPFact fact = new CPFact();
//        if (edge.getCallSite() instanceof Invoke invoke && invoke.getLValue() != null) {
//            Value value = Value.getUndef();
//            for (Var returnVar : edge.getReturnVars()) {
//                value = this.cp.meetValue(value, returnOut.get(returnVar));
//            }
//            fact.update(invoke.getLValue(), value);
//        }
//        return fact;
//    }
}
