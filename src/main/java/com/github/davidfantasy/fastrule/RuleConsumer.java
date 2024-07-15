package com.github.davidfantasy.fastrule;

@FunctionalInterface
public interface RuleConsumer {

    boolean accept(Rule fact);

}
