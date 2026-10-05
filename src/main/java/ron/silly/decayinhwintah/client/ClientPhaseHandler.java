package ron.silly.decayinhwintah.client;

import ron.silly.decayinhwintah.CycleManager;

/** Client-only target for the common network handler. */
public final class ClientPhaseHandler {
    private ClientPhaseHandler() {}
    public static void setPhase(CycleManager.Phase phase) { MusicDirector.setPhase(phase); }
}
