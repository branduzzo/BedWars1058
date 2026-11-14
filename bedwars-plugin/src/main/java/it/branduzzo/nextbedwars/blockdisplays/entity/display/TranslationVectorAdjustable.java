package it.branduzzo.nextbedwars.blockdisplays.entity.display;

import it.branduzzo.nextbedwars.blockdisplays.rotation.DirectedVector;

public interface TranslationVectorAdjustable {
    DirectedVector getTranslation();

    void setTranslation(DirectedVector translation);
}
