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

import pascal.taie.analysis.dataflow.fact.SetFact;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.exp.LValue;
import pascal.taie.ir.exp.RValue;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Stmt;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of classic live variable analysis.
 */
public class LiveVariableAnalysis extends
        AbstractDataflowAnalysis<Stmt, SetFact<Var>> {

    public static final String ID = "livevar";

    public LiveVariableAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public boolean isForward() {
        return false;
    }

    @Override
    public SetFact<Var> newBoundaryFact(CFG<Stmt> cfg) {
        // TODO - finish me
//        Stmt entryStmt = cfg.getEntry();
//        SetFact<Var> entryVar = new SetFact<>();
//        entryVar.
//        SetFact<Var> entryVar= cfg.getInEdgesOf(entryStmt);
        return new SetFact<>();
//        return null;
    }

    @Override
    public SetFact<Var> newInitialFact() {
        // TODO - finish me
        return new SetFact<>();
//        return null;
    }

    @Override
    public void meetInto(SetFact<Var> fact, SetFact<Var> target) {
        // TODO - finish me
        target.union(fact);
    }

    @Override
    public boolean transferNode(Stmt stmt, SetFact<Var> in, SetFact<Var> out) {
        // TODO - finish me
        Optional<LValue> def = stmt.getDef();
//        LValue def1 = stmt.getDef();
        List<RValue> use = stmt.getUses();
//        def.get()
        boolean result = true;
        SetFact<Var> outCpoy = out.copy();
        if (def.isPresent() && def.get() instanceof Var defVar)
        {
            result = outCpoy.remove(defVar);
//            错误之处为：1.out写为in 2.没有做副本（copy），直接在原来的基础上做删除操作，会使得循环不会终止？
        }
        result = in.union(outCpoy);
        for(RValue r : use)
        {
            if(r instanceof Var useVar)
            {
                result |= in.add(useVar);
            }
        }
        return result;
    }
}
