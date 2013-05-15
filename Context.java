import java.util.*;

/**
 * This class handles the different environments (current class,
 * variable environment, and method signatures).
 * All scoping and evaluation of SELF_TYPE is handled by this class.
 *
 * Four situations in which SELF_TYPE can be used:
 * 1. new SELF_TYPE
 * 2. return type of method
 * 3. declared type of let variable
 * 4. declared type of attribute
 */

//TODO: change asserts to semanterrors or handle them elsewhere
class Context {
  private VariableEnvironment variables;
  private ClassTable classes;
  private AbstractSymbol currentClass;
  private int scopeCount; //TODO: move to symbol table

	static final boolean DEBUG = true;

  public Context(ClassTable classes){
	  //variables = new SymbolTable(); got a compiler error here
	  variables = new VariableEnvironment();
    this.classes = classes;
    currentClass = null;
    scopeCount = 0;
  }

  public void enterClass(class_c c){
    pushScope();
    currentClass = c.getName();
    Features attrs = classes.getAttrs(c.getName());
    for(attr a : attrs.getElements()){
      AbstractSymbol type = a.type_decl;
      if(type == TreeConstants.SELF_TYPE) type = c.getName();
      variables.addId(a.name, type);
    }
  }

  public void leaveClass(){
    popScope();
    currentClass = null;
  }

  public void enterMethod(method m){
    pushScope();
    Formals params = getParameters(m.name);
    assert m != null : "tried to enter method that does not exist in this class";
    for(formalc f : params.getElements()){
      variables.addId(f.name, f.type_decl);
    }
  }

  public void leaveMethod(){
    popScope();
  }

  public void enterBranch(branch b){
    pushScope();
    variables.addId(b.name, b.type_decl);
  }

  public void leaveBranch(){
    popScope();
  }

  public void enterLet(let l){
    pushScope();
    AbstractSymbol type = l.type_decl;
    if(type == TreeConstants.SELF_TYPE) type = currentClass;
    variables.addId(l.identifier, type);
  }

  public void leaveLet(){
    popScope();
  }

  public AbstractSymbol newKeywordType(new_ n){
    if(n.type_name == TreeConstants.SELF_TYPE) return currentClass;
    return n.type_name;
  }

  public AbstractSymbol getVarType(AbstractSymbol name){
    return variables.lookup(name);
  }

  public boolean varDefined(AbstractSymbol name){
    return getVarType(name) != null;
  }

  public Formals getParameters(AbstractSymbol methodName){
    return getParameters(currentClass, methodName);
  }

  public Formals getParameters(AbstractSymbol className, AbstractSymbol methodName){
    assert className != null;
    Features classMethods = classes.getMethods(className);
    for(Feature f : classMethods.getElements()){
      if(f.name.equals(methodName)) return f.formals;
    }
    return null;
  }

  public AbstractSymbol getReturnType(AbstractSymbol methodName){
    return getReturnType(currentClass, methodName);
  }

  public AbstractSymbol getReturnType(AbstractSymbol className, AbstractSymbol methodName){
    assert className != null;
    Features classMethods = classes.getMethods(className);
    AbstractSymbol returnType = null;
    for(Feature f : classMethods.getElements()){
      if(f.name.equals(methodName)){
        returnType = f.return_type;
        break;
      }
    }
    if(returnType == null) return null;
    if(returnType == TreeConstants.SELF_TYPE) return className;
    return returnType;
  }

	public void verifyWellFormedClass(class_c cl) {
		/* Call getAllFeatures, which checks for inheritance errors and
		   duplicated attr/method names. Values aren't needed here, so no
		   return. */
		classes.getAllFeatures(cl, true);
	}

  /* Begin helper methods */

  private void pushScope(){
    variables.enterScope();
    scopeCount++;
  }

  private void popScope(){
    variables.exitScope();
    scopeCount--;
    assert scopeCount >= 0 : "scope count negative, abandon ship";
  }
}