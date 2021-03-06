/**
 * 
 */
package ac.technion.schemamatching.matchers.firstline;

import java.util.HashMap;


/**
 * This enum type lists the available @link{FirstLineMatcher}s in the Ontobuilder Research Environment. 
 * @author Tomer Sagi
 *
 */
public enum FLMList {AMCDataType(new AMCDataType()),AMCName(new AMCName())
	,AMCTokenPath(new AMCTokenPath()),AMCPath(new AMCPath()),AMCSibling(new AMCSibling())
	,OBGraphMatch(new OBGraphMatch()),OBPrecedence(new OBPrecedenceMatch()),
	OBSimilarityFlooding(new OBSimilarityFlooding()),OBTerm(new OBTermMatch()),
	OBValue(new OBValueMatch()),OBTermValue(new OBTermValueMatch()),OBCombined(new OBCombined())
	,Instance(new InstanceMatcher()),OBDomain(new DomainMatch())
	,TermOrder(new TermOrder()), 
	WordNet(new OBWordNet()),
	ProcessModel(new ProcessModelFLM())
	,OBTED(new OBTEDGraphMatch())
	,ContentBasedMatcher(new ContentBasedMatcher())
	,SemRep(new SemRepMatcher()),SatMatch(new SATMatcher()), OurWN(new OurWordnet()
			);

private FLMList(FirstLineMatcher flm)
{
	myFLM = flm;
}

public FirstLineMatcher getFLM() {
	return myFLM;
}

public static HashMap<Integer,FirstLineMatcher> getIdFLMHash()
{
	HashMap<Integer,FirstLineMatcher> res = new HashMap<Integer,FirstLineMatcher>();
	for (FLMList f : FLMList.values())
	{
		res.put(f.myFLM.getDBid(), f.myFLM);
	}
	return res;
}
private FirstLineMatcher myFLM;


}
