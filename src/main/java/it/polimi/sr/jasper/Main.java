package it.polimi.sr.jasper;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

public class Main {

public static void main(String[] args){
    String rules = "[rule1: (?a eg:p ?b) (?b eg:p ?c) ->     (?a eg:p ?c)]";
    Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
    reasoner.setDerivationLogging(true);
}
}
