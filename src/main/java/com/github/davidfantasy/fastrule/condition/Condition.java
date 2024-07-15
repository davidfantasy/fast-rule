package com.github.davidfantasy.fastrule.condition;

import com.github.davidfantasy.fastrule.fact.Fact;

public interface Condition {

    boolean evaluate(Fact fact);

}
