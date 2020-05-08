package com.sstewartgallus.term;

import com.sstewartgallus.ir.Category;
import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.pass1.Pass1;
import com.sstewartgallus.type.HList;
import com.sstewartgallus.type.Type;

import java.util.Map;
import java.util.Set;

public record Var<A>(Type<A>type, int number) implements  Comparable<Var<?>> {


    public String toString() {
        return "v" + number();
    }

    @Override
    public int compareTo(com.sstewartgallus.term.Var<?> var) {
        return var.number() - number();
    }
}
