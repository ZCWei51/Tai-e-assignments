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

package pascal.taie.analysis.dataflow.analysis.constprop;

import pascal.taie.analysis.dataflow.analysis.AbstractDataflowAnalysis;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.exp.*;
import pascal.taie.ir.stmt.DefinitionStmt;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.type.PrimitiveType;
import pascal.taie.language.type.Type;
import pascal.taie.util.AnalysisException;
import pascal.taie.analysis.dataflow.analysis.constprop.Value;

import java.util.List;
import java.util.Optional;

public class ConstantPropagation extends
        AbstractDataflowAnalysis<Stmt, CPFact> {

    public static final String ID = "constprop";

    public ConstantPropagation(AnalysisConfig config) {
        super(config);
    }

    @Override
    public boolean isForward() {
        return true;
    }

    @Override
    public CPFact newBoundaryFact(CFG<Stmt> cfg) {
        // TODO - finish me
//        return null;
//        return new Value(Kind.NAC);
        CPFact fact = new CPFact();

        for(Var var : cfg.getIR().getParams())
        {
            if(canHoldInt(var))
            {
                fact.update(var, Value.getNAC());
            }
        }
        return fact;
    }


    @Override
    public CPFact newInitialFact() {
        // TODO - finish me
//        return null;
        return new CPFact();
    }

    @Override
    public void meetInto(CPFact fact, CPFact target) {
        // TODO - finish me
        for(Var var : fact.keySet())
        {
            target.update(var, meetValue(fact.get(var), target.get(var)));
        }
    }

    /**
     * Meets two Values.
     * Value一共有三种状态，常量(Constant)、非常量(NAC)以及非定义(UNDEFF)
     * 根据meet操作符的定义：
     * 1. 只要包含NAC则返回NAC
     * 2. 只要包含UNDEFF，结果取决于另一个Value
     * 3. 只有两者都是常量且值相同才会返回Constant的值
     */
    public Value meetValue(Value v1, Value v2) {
        // TODO - finish me
//        return null;
        if(v1.isNAC() || v2.isNAC() )
        {
            return Value.getNAC();
        }
        if(v1.isConstant())
        {
            if(v2.isConstant())
            {
                return (v1.getConstant() == v2.getConstant()? v1: Value.getNAC());
            }else
            {
                return v1;
            }
        }else {
            return v2;
        }
    }

    @Override
    public boolean transferNode(Stmt stmt, CPFact in, CPFact out) {
        // TODO - finish me
//        return false;
//        Wrong!
//        boolean changed = true;
//
//        Optional<LValue> lvalue = stmt.getDef();
//        List<RValue> rvalue = stmt.getUses();
//        CPFact inCopy = in.copy();
//        if (lvalue.isPresent() && lvalue.get() instanceof Var defVar)
//        {
//            inCopy = inCopy.remove(defVar);
//            out.update()
//        }
//
//        return changed;

        if(stmt instanceof DefinitionStmt definitionStmt && definitionStmt.getLValue() instanceof Var var && canHoldInt(var)){
            Exp exp = definitionStmt.getRValue();
            Value evaluateValue = evaluate(exp, in);

            CPFact inCopy = in.copy();
            inCopy.remove(var);
            boolean changed = out.update(var, evaluateValue);
//            changed &= out.copyFrom(inCopy);
            changed |= out.copyFrom(inCopy);
            return changed;
        }else {
            return out.copyFrom(in);
        }
//        if (stmt instanceof DefinitionStmt definitionStmt && definitionStmt.getLValue() instanceof Var varDef && canHoldInt(varDef)) {
//            // gen
//            Exp exp = definitionStmt.getRValue();
//            Value evaluateValue = evaluate(exp, in);
//            boolean changed = out.update(varDef, evaluateValue);
//            // U (IN[s] – {(x, _)})
//            CPFact inCopy = in.copy();
//            inCopy.remove(varDef);
//            changed |= out.copyFrom(inCopy);
//            return changed;
//        } else {
//            return out.copyFrom(in);
//        }
    }

    /**
     * @return true if the given variable can hold integer value, otherwise false.
     */
    public static boolean canHoldInt(Var var) {
        Type type = var.getType();
        if (type instanceof PrimitiveType) {
            switch ((PrimitiveType) type) {
                case BYTE:
                case SHORT:
                case INT:
                case CHAR:
                case BOOLEAN:
                    return true;
            }
        }
        return false;
    }

    /**
     * Evaluates the {@link Value} of given expression.
     *
     * @param exp the expression to be evaluated
     * @param in  IN fact of the statement
     * @return the resulting {@link Value}
     */
    public static Value evaluate(Exp exp, CPFact in) {
//        System.out.println("public static Value evaluate(Exp exp, CPFact in)");
        // TODO - finish me
//        return null;
        // 分三类处理
        // x = c
        // x = y
        // x = y op z
        if(exp instanceof IntLiteral intvalue)
        {
            return Value.makeConstant(intvalue.getValue()); // x=c
        }else if(exp instanceof Var var)
        {
            return in.get(var); // x=y
        }
        if(!(exp instanceof BinaryExp binaryExp))
        {
            return Value.getNAC();
        }
        // x = y op z
        Var var2 = binaryExp.getOperand2();
        Var var1 = binaryExp.getOperand1();
        Value value2 = in.get(var2);
        Value value1 = in.get(var1);

        // 好像是这里的逻辑有问题！
        if(value2.isConstant() && value2.getConstant() == 0 && binaryExp instanceof ArithmeticExp arithmeticExp){
            return switch (arithmeticExp.getOperator()){
                case DIV -> Value.getUndef();
                case REM -> Value.getUndef();
                default -> Value.getNAC();
            };
        }else if(value1.isNAC() || value2.isNAC())
        {
            return Value.getNAC();
        }else if(value1.isConstant() && value2.isConstant())
        {
            int value1Constant = value1.getConstant();
            int value2Constant = value2.getConstant();
            if(binaryExp instanceof ArithmeticExp arithmeticExp)
            {
//                System.out.println("here value2Constant == 0!");
                return switch (arithmeticExp.getOperator()) {
                    case ADD -> Value.makeConstant(value1Constant + value2Constant);
                    case MUL -> Value.makeConstant(value1Constant * value2Constant);
                    case SUB -> Value.makeConstant(value1Constant - value2Constant);
                    case DIV -> Value.makeConstant(value1Constant / value2Constant);
                    case REM -> Value.makeConstant(value1Constant % value2Constant);
                    //                        case ADD -> Value.getNAC();
//                        case MUL -> Value.getNAC();
//                        case SUB -> Value.getNAC();
//                    case DIV -> Value.getUndef();
//                    case REM -> Value.getUndef();
                };

            }else if(binaryExp instanceof ConditionExp conditionExp)
            {
                return switch (conditionExp.getOperator()){
                    case EQ -> Value.makeConstant(value1Constant == value2Constant? 1 : 0);
                    case NE -> Value.makeConstant(value1Constant != value2Constant? 1 : 0);
                    case LT -> Value.makeConstant(value1Constant < value2Constant? 1 : 0);
                    case GT -> Value.makeConstant(value1Constant > value2Constant? 1 : 0);
                    case LE -> Value.makeConstant(value1Constant <= value2Constant? 1 : 0);
                    case GE -> Value.makeConstant(value1Constant >= value2Constant? 1 : 0);
                };
            }else if(binaryExp instanceof ShiftExp shiftExp)
            {
                return switch (shiftExp.getOperator()){
                    case SHL -> Value.makeConstant(value1Constant << value2Constant);
                    case SHR -> Value.makeConstant(value1Constant >> value2Constant);
                    case USHR -> Value.makeConstant(value1Constant >>> value2Constant);
                };
            }else if(binaryExp instanceof BitwiseExp bitwiseExp)
            {
                return switch (bitwiseExp.getOperator()){
                    case OR -> Value.makeConstant(value1Constant | value2Constant);
                    case AND -> Value.makeConstant(value1Constant & value2Constant);
                    case XOR -> Value.makeConstant(value1Constant ^ value2Constant);
                };
            }else
            {
                return Value.getNAC();
            }
        }else
        {
            return Value.getUndef();
        }
//        wrong!
//        for(RValue rvalue : exp.getUses())
//        {
//            op = rvalue.
//        }
//        return Value.getNAC();
    }
}