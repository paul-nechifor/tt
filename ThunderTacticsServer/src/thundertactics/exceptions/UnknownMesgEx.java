package thundertactics.exceptions;

public class UnknownMesgEx extends Exception {
    private static final long serialVersionUID = 3332421891271215145L;
    
    public UnknownMesgEx(String message) {
        super(message);
    }
}
