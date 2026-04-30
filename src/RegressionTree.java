class RegressionTree {

	private Node root;
	private RegressionTree[] childTree;

	public RegressionTree() {

	}
	
	RegressionTree(Data trainingSet){
		learnTree(trainingSet,0,trainingSet.getNumberOfExamples()-1,trainingSet.getNumberOfExamples()*10/100);
	}

	boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		int numberOfExamples = end - begin + 1;
		if (numberOfExamples <= numberOfExamplesPerLeaf) {
			return true;
		}
		return false;
	}

	SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
    	SplitNode bestNode = null;
    	double minVariance = Double.MAX_VALUE;

    	for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
        	DiscreteAttribute currentAttribute = (DiscreteAttribute) trainingSet.getExplanatoryAttribute(i);
        	DiscreteNode tempNode = new DiscreteNode(trainingSet, begin, end, currentAttribute);
        
        	double currentVariance = tempNode.getVariance();
        
        	// Se è la varianza più bassa trovata finora (o se è il primo giro), assegno proprio tempNode a bestNode
        	if (bestNode == null || currentVariance < minVariance) {
            	minVariance = currentVariance;
            	bestNode = tempNode;
        	}
    	}

    	// Una volta trovato il bestNode, ordino i dati del dataset in base a quell'attributo
    	if (bestNode != null) {
        	trainingSet.sort(bestNode.getAttribute(), begin, end);
    	}
    	return bestNode;
	}

		
	void learnTree(Data trainingSet,int begin, int end,int numberOfExamplesPerLeaf){
		if (isLeaf(trainingSet, begin, end, numberOfExamplesPerLeaf)){
			//determina la classe che compare più frequentemente nella partizione corrente
			root=new LeafNode(trainingSet,begin,end);
		}
		else //split node
		{
			root=determineBestSplitNode(trainingSet, begin, end);
			
			if (root.getNumberOfChildren()>1){
				childTree=new RegressionTree[root.getNumberOfChildren()];
				for(int i=0;i<root.getNumberOfChildren();i++){
					childTree[i]=new RegressionTree();
					childTree[i].learnTree(trainingSet, ((SplitNode)root).getSplitInfo(i).beginIndex, ((SplitNode)root).getSplitInfo(i).endIndex, numberOfExamplesPerLeaf);
				}
			}
			else
				root=new LeafNode(trainingSet,begin,end);	
		}
	}
			

	void printTree(){
		System.out.println("********* TREE **********\n");
		System.out.println(toString());
		System.out.println("*************************\n");
	}
		
	public String toString(){
		String tree=root.toString()+"\n";
			
		if( root instanceof LeafNode){
			
		}
		else //split node
		{
			for(int i=0;i<childTree.length;i++)
				tree +=childTree[i];
			}
			return tree;
	}
		
}
		
