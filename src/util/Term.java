package util;

public abstract class Term {
    private TokenM tipe = TokenM.ADDER;
    //private String clue = "";
    @Override
    abstract public String toString();

    abstract public String printTree(int lvl);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // result = prime * result + ((Clue == null) ? 0 : Clue.hashCode());
        result = prime * result + ((tipe == null) ? 0 : tipe.hashCode());
        return result;
    }

    public TokenM getToken() {
        return tipe;
    }
    
    public String getStringifiedToken() {
    	if(tipe==TokenM.ADD){
    		return "+";	
    	}
    	if(tipe==TokenM.ADDER){
    		return "+";	
    	}
    	
    	else if(tipe==TokenM.SUBTRACT){
    		return "-";	
    	}
    	else if(tipe==TokenM.SUBTRACTOR){
    		return "-";	
    	}    	
    	else if(tipe==TokenM.FACTOR){
    		return "x";	
    	}    	
    	else if(tipe==TokenM.DENUMERATOR){
    		return ":";	
    	}
    	
    	else
        return "";
    }

    public void setToken(TokenM to) {
        this.tipe = to;
    }

    @Override
    public abstract boolean equals(Object obj);

    public abstract Term addParenthesizedExpr(Term t, TokenM k);

    //public abstract PExpr promoteMe();
    public abstract boolean cekKurungBerlebih();

    public abstract void setClue(String clue);
}


class SimpleTerm extends Term  {
    private int value;
    //original location
    private int operatorIndex = 0;
    private int operandStart = 0, operandEnd = 0;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (this.getToken() == TokenM.ADDER)
            return "+" + this.getValue();
        else if (this.getToken() == TokenM.SUBTRACTOR)
            return "-" + this.getValue();
        else
            return String.valueOf(this.getValue());
    }

    @Override
    public String printTree(int lvl) {
        return 
        		MantikProcessor.printBranch(lvl)+
        		Integer.toString(this.value)+" "+this.getToken().name()+"\n";
    }



    @Override
    public void setClue(String clue) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        //simple is always trying to be simple
        if (obj instanceof SimpleTerm
                && this.getValue() == ((SimpleTerm) obj).getValue()
                && this.getToken() == ((SimpleTerm) obj).getToken())
            return true;
        else
            return false;
    }



    public int getOperatorIndex() {
        return operatorIndex;
    }

    public void setOperatorIndex(int operatorIndex) {
        this.operatorIndex = operatorIndex;
    }

    public int getOperandStart() {
        return operandStart;
    }

    public void setOperandStart(int operandStart) {
        this.operandStart = operandStart;
    }

    public int getOperandEnd() {
        return operandEnd;
    }

    
    public void setOperandEnd(int operandEnd) {
        this.operandEnd = operandEnd;
    }





    @Override
    public Term addParenthesizedExpr(Term t, TokenM k) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cekKurungBerlebih() {
        return true;
    }
}
