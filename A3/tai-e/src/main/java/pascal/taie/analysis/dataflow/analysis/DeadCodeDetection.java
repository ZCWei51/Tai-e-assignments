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

package pascal.taie.analysis.dataflow.analysis;

import pascal.taie.analysis.MethodAnalysis;
import pascal.taie.analysis.dataflow.analysis.constprop.CPFact;
import pascal.taie.analysis.dataflow.analysis.constprop.ConstantPropagation;
import pascal.taie.analysis.dataflow.analysis.constprop.Value;
import pascal.taie.analysis.dataflow.fact.DataflowResult;
import pascal.taie.analysis.dataflow.fact.SetFact;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.analysis.graph.cfg.CFGBuilder;
import pascal.taie.analysis.graph.cfg.Edge;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.exp.*;
import pascal.taie.ir.stmt.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;

import java.util.*;

public class DeadCodeDetection extends MethodAnalysis {

    public static final String ID = "deadcode";

    public DeadCodeDetection(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Set<Stmt> analyze(IR ir) {
        // obtain CFG
        CFG<Stmt> cfg = ir.getResult(CFGBuilder.ID);
        // obtain result of constant propagation
        DataflowResult<Stmt, CPFact> constants =
                ir.getResult(ConstantPropagation.ID);
        // obtain result of live variable analysis
        DataflowResult<Stmt, SetFact<Var>> liveVars =
                ir.getResult(LiveVariableAnalysis.ID);
        // keep statements (dead code) sorted in the resulting set
        Set<Stmt> deadCode = new TreeSet<>(Comparator.comparing(Stmt::getIndex));
        Set<Stmt> noDeadCode = new TreeSet<>(Comparator.comparing(Stmt::getIndex));
        ArrayList<String> arrayInfo = new ArrayList<>();

        // TODO - finish me
        // Your task is to recognize dead code in ir and add it to deadCode
//        deadCode.add();
        Queue<Stmt> cfgWorkList = new ArrayDeque<>();
        Stmt entryNode = cfg.getEntry();
        cfgWorkList.add(entryNode);
        Set<Stmt> allNode = cfg.getNodes();
        int cnt = 0;
        arrayInfo.add("所有的stmt数量为" + cfg.getNumberOfNodes());
        while (!cfgWorkList.isEmpty()) {
            arrayInfo.add("\n这是第"+cnt+"次");
            cnt += 1;
            Stmt cfgNode = cfgWorkList.poll();
            arrayInfo.add("LineNumber"+cfgNode.getLineNumber());
            if (noDeadCode.contains(cfgNode))
                continue;
            arrayInfo.add(cfgNode.toString());
            arrayInfo.add(cfgNode.getClass().getTypeName());
            arrayInfo.add("这里是遍历cfgNode");
            if (cfg.isExit(cfgNode)) {
                cfgWorkList.clear();
                break;
            }
            CPFact constantsOutFact = constants.getOutFact(cfgNode);
            CPFact constantsIntFact = constants.getInFact(cfgNode);
            SetFact<Var> liveVarsOutFact = liveVars.getOutFact(cfgNode);
            if (cfgNode instanceof AssignStmt assignStmt) {
                // 无用赋值-活跃变量
                // 处理AssignStmt
                for (Edge<Stmt> edge : cfg.getOutEdgesOf(cfgNode)) {
                    cfgWorkList.add(edge.getTarget());
//                   这种写法和cfgWorkList.addAll(cfg.getSuccsOf(cfgNode));结果上是等价的
                }
                arrayInfo.add("这里到达了 AssignStmt");
                if(!hasNoSideEffect(assignStmt.getRValue()))
                {
                    continue;
                }
                arrayInfo.add("通过了副作用检查");
                if ( assignStmt.getLValue() instanceof Var var && liveVarsOutFact.contains(var)) {
                    arrayInfo.add(cfgNode.toString());
                    noDeadCode.add(cfgNode);
                }
            } else if (cfgNode instanceof If ifstmt) {
                arrayInfo.add("这里到达了 IfStmt");
                // 不可到达-常量传播
                // 处理 IF
                Value evaluateValue = ConstantPropagation.evaluate(ifstmt.getCondition(), constantsIntFact);
                if (evaluateValue.isConstant()) {
                    int varConstant = evaluateValue.getConstant();
                    arrayInfo.add("这里是变量varConstant的值" + varConstant);
                    for (Edge<Stmt> edge : cfg.getOutEdgesOf(cfgNode)) {
                        if (edge.getKind() == Edge.Kind.IF_TRUE && varConstant > 0) {
                            // true
                            arrayInfo.add("here IF_TRUE");
                            noDeadCode.add(cfgNode);
                            cfgWorkList.add(edge.getTarget());
                            break;
                        } else if (edge.getKind() == Edge.Kind.IF_FALSE && varConstant <= 0) {
                            // false
                            noDeadCode.add(cfgNode);
                            cfgWorkList.add(edge.getTarget());
                            break;
                        }
                    }
                } else {
                    noDeadCode.add(cfgNode);
                    for (Edge<Stmt> edge : cfg.getOutEdgesOf(cfgNode)) {
                        cfgWorkList.add(edge.getTarget());
                    }
                }

            } else if (cfgNode instanceof SwitchStmt switchstmt) {
                arrayInfo.add("这里到达了 SwitchStmt");
                // 不可到达-常量传播
                // 处理Switch
//                Var var = switchstmt.getVar();
//                Value evaluateValue = ConstantPropagation.evaluate(var, constantsIntFact);
                Value evaluateValue = constants.getResult(switchstmt).get(switchstmt.getVar());
                if (evaluateValue.isConstant()) {
                    int varConstant = evaluateValue.getConstant();
                    for (Edge<Stmt> edge : cfg.getOutEdgesOf(cfgNode)) {
                        if (edge.getKind() == Edge.Kind.SWITCH_CASE && edge.getCaseValue() == varConstant) {
//                            noDeadCode.add(edge.getTarget());
                            noDeadCode.add(cfgNode);
                            cfgWorkList.add(edge.getTarget());
                            break;
                        } else if (edge.getKind() == Edge.Kind.SWITCH_DEFAULT) {
                            // 如果没有break则说明会到达default分支
                            noDeadCode.add(cfgNode);
                            cfgWorkList.add(edge.getTarget());
                        }
                    }
                } else {
                    noDeadCode.add(cfgNode);
                    for (Edge<Stmt> edge : cfg.getOutEdgesOf(cfgNode)) {
                        cfgWorkList.add(edge.getTarget());
                    }
                }
            } else {
                arrayInfo.add("here other stmt");
                for (Edge<Stmt> edge : cfg.getOutEdgesOf(cfgNode)) {
                    cfgWorkList.add(edge.getTarget());
                }
                noDeadCode.add(cfgNode);
            }
        }
        arrayInfo.add("遍历输出allNode");
        arrayInfo.add("---------开始----------");
        for (Stmt stmt : allNode) {
            arrayInfo.add(stmt.toString());
        }
        arrayInfo.add("---------结束----------");
        arrayInfo.add("遍历输出noDeadCode");
        arrayInfo.add("---------开始----------");
        for (Stmt stmt : noDeadCode) {
            arrayInfo.add(stmt.toString());
        }
        arrayInfo.add("---------结束----------");
        for(String str : arrayInfo)
        {
            System.out.println(str);
        }
        for (Stmt stmt : allNode) {
            if (cfg.isExit(stmt) || cfg.isEntry(stmt))
                continue;
            if (!noDeadCode.contains(stmt)) {
                deadCode.add(stmt);
            }
        }
//        return noDeadCode;
        return deadCode;
    }

    /**
     * @return true if given RValue has no side effect, otherwise false.
     */
    private static boolean hasNoSideEffect(RValue rvalue) {
        // new expression modifies the heap
        if (rvalue instanceof NewExp ||
                // cast may trigger ClassCastException
                rvalue instanceof CastExp ||
                // static field access may trigger class initialization
                // instance field access may trigger NPE
                rvalue instanceof FieldAccess ||
                // array access may trigger NPE
                rvalue instanceof ArrayAccess) {
            return false;
        }
        if (rvalue instanceof ArithmeticExp) {
            ArithmeticExp.Op op = ((ArithmeticExp) rvalue).getOperator();
            // may trigger DivideByZeroException
            return op != ArithmeticExp.Op.DIV && op != ArithmeticExp.Op.REM;
        }
        return true;
    }
}
