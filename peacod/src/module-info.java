module peacod {
    requires jdk.dynalink;
    requires org.objectweb.asm;
    requires org.objectweb.asm.util;
    requires peacod.plato;
    requires peacod.interpreter;
    exports com.sstewartgallus.runtime;
    exports com.sstewartgallus.optimizers;
    exports com.sstewartgallus.ext.java;
    exports com.sstewartgallus.ext.mh;
    exports com.sstewartgallus;
}