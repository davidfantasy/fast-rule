package com.github.davidfantasy.fastrule.fact;

import java.util.Collection;

public interface Fact {

    String getId();

    Long getTs();

    Object getValue(String name);

    Collection<Object> getValues();

}
