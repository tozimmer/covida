input metauml;
beginfig(1);
	Package.pAa("streaming")();
	
	Package.pA("de.dfki.covida")();
	Package.pB("de.dfki.covida.covidacore")(pAa);
	Package.pC("de.dfki.covida.videovlc")();
	Package.pD("de.dfki.covida.visualjme2")();
	
	Package.peA("de.dfki.touchandwrite")();
	Package.peB("uk.co.caprica.vlcj")();
	Package.peC("com.jme")();
	
	
	
	topToBottom(50)(pB, pC, pD);
	topToBottom(50)(peA, peB, peC);
	leftToRight(50)(pA, pC, peB);

	drawObjects(pA,pAa, pB, pC, pD, peA, peB, peC);

	clink(association)(pB, pA);
	clink(association)(pC, pA);
	clink(association)(pD, pA);
	
	clink(association)(pC, peB);
	clink(association)(pB, peA);
	clink(association)(pD, peC);
endfig;
end