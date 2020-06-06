module peacod.plato {
    requires org.objectweb.asm;
    requires org.objectweb.asm.util;
    requires jdk.dynalink;
    requires projog.core;

    exports com.sstewartgallus.plato.runtime;
    exports com.sstewartgallus.plato.cbpv;
    exports com.sstewartgallus.plato.syntax.term;
    exports com.sstewartgallus.plato.syntax.type;
    exports com.sstewartgallus.plato.syntax.ext.pretty;
    exports com.sstewartgallus.plato.syntax.ext.variables;
    exports com.sstewartgallus.plato.java;
    exports com.sstewartgallus.plato.frontend;
}