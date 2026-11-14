package it.branduzzo.nextbedwars.blockdisplays.animation.bdengine;

import it.branduzzo.nextbedwars.blockdisplays.file.mcfunction.MCFunction;

public interface FunctionAnimator {
    MCFunction getPlayFunction();

    MCFunction getPlayLoopFunction();
}
