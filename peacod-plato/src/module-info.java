module peacod.plato {
    requires org.objectweb.asm;
    requires org.objectweb.asm.util;
    requires jdk.dynalink;

    exports com.sstewartgallus.plato.runtime;
    exports com.sstewartgallus.plato.ir.type;
    exports com.sstewartgallus.plato.java;
    exports com.sstewartgallus.plato.frontend;
    exports com.sstewartgallus.plato.ir.systemf;
    exports com.sstewartgallus.plato.ir.cbpv;
    exports com.sstewartgallus.plato.ir.cps;
    exports com.sstewartgallus.plato.ir.dethunk;
    exports com.sstewartgallus.plato.runtime.type;
    exports com.sstewartgallus.plato.ir;
    exports com.sstewartgallus.plato.compiler;
}