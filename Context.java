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

  public Context(ClassTable classes){
    variables = new VariableEnvironment();
    this.classes = classes;
    currentClass = null;
  }

  public void enterClass(class_c cur){
    pushScope();
    currentClass = cur.getName();
    Features attrs = classes.getAttrs(cur.getName());
    for(int i = 0; i < attrs.getLength(); i++){
      attr nextAttr = (attr) attrs.getNth(i);
      AbstractSymbol type = nextAttr.type_decl;
      if(type == TreeConstants.SELF_TYPE) type = cur.getName();
      variables.addId(nextAttr.name, type);
    }
  }

  public void leaveClass(){
    popScope();
    currentClass = null;
  }

  public void enterMethod(method met){
    pushScope();
    Formals params = getParameters(met.name);
    assert params != null : "tried to enter method that does not exist in this class";
    for(int i = 0; i < params.getLength(); i++){
      formalc f = (formalc) params.getNth(i);
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
    return (AbstractSymbol) variables.lookup(name);
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
    for(int i = 0; i < classMethods.getLength(); i++){
      method met = (method) classMethods.getNth(i);
      if(met.name.equals(methodName)) return met.formals;
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
    for(int i = 0; i < classMethods.getLength(); i++){
      method met = (method) classMethods.getNth(i);
      if(met.name.equals(methodName)){
        returnType = met.return_type;
        break;
      }
    }
    if(returnType == null) return null;
    if(returnType == TreeConstants.SELF_TYPE) return className;
    return returnType;
  }

  /* Begin helper methods */

  private void pushScope(){
    variables.enterScope();
  }

  private void popScope(){
    variables.exitScope();
  }
}