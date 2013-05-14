import java.util.*;

class Context {
  private SymbolTable variables;
  private ClassTable classes;
  private AbstractSymbol currentClass;
  private int scopeCount; //TODO: move to symbol table

  public Context(ClassTable classes){
    variables = new SymbolTable();
    this.classes = classes;
    currentClass = null;
    scopeCount = 0;
  }

  public void enterClass(class_c c){
    pushScope();
    currentClass = c.getName();
    Features attrs = classes.getAttrs(c.getName());
    for(attr a : attrs.getElements()){
      variables.addId(a.name, a.type_decl); //TODO: check if we need to add getters instead
    }
  }

  public void leaveClass(){
    popScope();
    currentClass = null;
  }

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