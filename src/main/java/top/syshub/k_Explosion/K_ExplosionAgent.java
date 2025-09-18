package top.syshub.k_Explosion;

import java.lang.instrument.Instrumentation;

public class K_ExplosionAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[K-Explosion] K-Explosion ON");
        inst.addTransformer(new ExplosionTransformer(), false);
    }
}
