import java.io.PrintStream;
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

	static final boolean DEBUG = true;

  public Context(ClassTable classes){
    variables = new VariableEnvironment(this);
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
      variables.addId(nextAttr.name, type, nextAttr);
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
      variables.addId(f.name, f.type_decl, f);
    }
  }

  public void leaveMethod(){
    popScope();
  }

  public void enterBranch(branch b){
    pushScope();
    variables.addId(b.name, b.type_decl, b);
  }

  public void leaveBranch(){
    popScope();
  }

  public void enterLet(let l){
    pushScope();
    AbstractSymbol type = l.type_decl;
    variables.addId(l.identifier, type, l);
  }

  public void leaveLet(){
    popScope();
  }

  public AbstractSymbol validateType(AbstractSymbol type) {
	  return type;
  }

  public AbstractSymbol getTypeFromName(AbstractSymbol name){
    if(name == TreeConstants.self) return TreeConstants.SELF_TYPE;
    return (AbstractSymbol) variables.lookup(name);
  }

  public boolean varDefined(AbstractSymbol name){
    if(name == TreeConstants.self) return true;
    return getTypeFromName(name) != null;
  }

  public AbstractSymbol currentClass() {
	return currentClass;
  }

	public boolean classDefined(AbstractSymbol className) {
		return className == TreeConstants.SELF_TYPE || classes.getClass(className) != null;
	}

  public Formals getParameters(AbstractSymbol methodName){
    return getParameters(currentClass, methodName);
  }

  public Formals getParameters(AbstractSymbol className, AbstractSymbol methodName){
    assert className != null;
    Features classMethods = classes.getMethods(className);
    for(int i = 0; i < classMethods.getLength(); i++){
      method met = (method) classMethods.getNth(i);
      if(met.name == methodName) return met.formals;
    }
    return new Formals(classMethods.getLineNumber());
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
    if(returnType == null || returnType == TreeConstants.No_type) return null;
    return returnType;
  }

	public void verifyWellFormedClass(class_c curr) {
		/* Call getAllFeatures, which checks for inheritance errors and
		   duplicated attr/method names. Values aren't needed here, so no
		   return. */
		
		classes.verifyClass(curr);
	}
  /* Hooks into ClassTable */

  public boolean isSubclassOf(AbstractSymbol child, AbstractSymbol parent){
    //special cases for SELF_TYPE subclassing
    if(child == TreeConstants.SELF_TYPE && parent == TreeConstants.SELF_TYPE) return true;
    if(parent == TreeConstants.SELF_TYPE) return false;
    
    //fix expr_type to allow for SELF_TYPEc <= T
    if(child == TreeConstants.SELF_TYPE && parent != TreeConstants.SELF_TYPE) child = currentClass;
    return classes.isSubClassOf(child, parent);
  }

  public AbstractSymbol leastUpperBound(AbstractSymbol one, AbstractSymbol two){
    //special case: both SELF_TYPE
    if(one == TreeConstants.SELF_TYPE && two == TreeConstants.SELF_TYPE) return TreeConstants.SELF_TYPE;
    if(one == TreeConstants.SELF_TYPE) one = currentClass;
    if(two == TreeConstants.SELF_TYPE) two = currentClass;
    return classes.leastUpperBound(one, two).name;
  }

  public Features getAttrs(AbstractSymbol name){
    return classes.getAttrs(name);
  }

  public Features getMethods(AbstractSymbol name){
    return classes.getMethods(name);
  }

  public PrintStream semantError(TreeNode t){
    return classes.semantError(classes.getClass(currentClass).getFilename(), t);
  }

  public boolean errors(){
    return classes.errors();
  }

  /* Begin helper methods */

  private void pushScope(){
    variables.enterScope();
  }

  private void popScope(){
    variables.exitScope();
  }
}