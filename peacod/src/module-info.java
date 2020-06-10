module peacod {
    requires jdk.dynalink;

    requires org.objectweb.asm;
    requires org.objectweb.asm.util;
    requires peacod.plato;
    exports com.sstewartgallus.runtime;
    exports com.sstewartgallus;
}