/**
 * A simple extension of SymbolTable that allows us to 
 * track the number of active scopes for debugging purposes. 
 */
class VariableEnvironment extends SymbolTable {
  private int scopeCount;
  private Context context;

  public VariableEnvironment(Context context){
    super();
    this.context = context;
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

  
  public void addId(AbstractSymbol id, Object info, TreeNode t) {
    if(id == TreeConstants.self){
      context.semantError(t).println("'self' cannot be the name of an attribute.");
      return;
    }
    super.addId(id, info);
  }

  public int getScopeCount(){
    return scopeCount;
  }
}