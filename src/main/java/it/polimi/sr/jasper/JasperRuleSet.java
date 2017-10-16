package it.polimi.sr.jasper;

import be.ugent.idlab.rspservice.common.interfaces.RuleSet;
import it.polimi.yasper.core.engine.Entailment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JasperRuleSet implements RuleSet {

    private String id;
    private Entailment rules;

}
