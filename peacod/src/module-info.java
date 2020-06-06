module peacod {
    requires jdk.dynalink;
    requires projog.core;

    requires org.objectweb.asm;
    requires org.objectweb.asm.util;
    requires peacod.plato;
    exports com.sstewartgallus.runtime;
    exports com.sstewartgallus;
}