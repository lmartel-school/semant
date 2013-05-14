/**
 * A simple extension of SymbolTable that allows us to 
 * track the number of active scopes for debugging purposes. 
 */
class VariableEnvironment extends SymbolTable {
  private int scopeCount;

  public VariableEnvironment(){
    super();
    scopeCount = 0;
  }

  @Override
  public void enterScope() {
    super.enterScope();
    scopeCount++;
  }

  @Override
  public void exitScope() {
    super.exitScope();
    scopeCount--;
    assert scopeCount >= 0 : "scope count negative, abandon ship";
  }
}