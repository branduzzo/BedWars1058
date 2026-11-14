package it.branduzzo.nextbedwars.blockdisplays.web.bdengine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.SummonDisplayCommandLine;
import it.branduzzo.nextbedwars.blockdisplays.nbt.entity.NbtEntity;

import java.util.List;

public interface BDEngineModel {
    String getName();

    String getAuthor();

    String getCategory();

    String getImgUrl();

    int getModelCount();

    JsonObject getCommands();

    List<SummonDisplayCommandLine> decodeCommands();

    JsonArray getPassengers();

    List<NbtEntity> decodePassengers();
}
