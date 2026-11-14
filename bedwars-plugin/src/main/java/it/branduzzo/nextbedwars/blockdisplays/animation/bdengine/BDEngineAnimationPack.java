package it.branduzzo.nextbedwars.blockdisplays.animation.bdengine;

import it.branduzzo.nextbedwars.blockdisplays.file.mcfunction.MCFunction;

import java.util.Map;

public interface BDEngineAnimationPack {
    Map<String, FunctionAnimation> getAnimations();

    Map<String, FunctionAnimator> getAnimators();

    MCFunction getCreateFunction();

    MCFunction getDeleteFunction();

    MCFunction getStopFunction();

    String getTag();
}
