import java.io.PrintStream;
import java.util.*;
/** This class may be used to contain the semantic information such as
 * the inheritance graph.  You may use it or not as you like: it is only
 * here to provide a container for the supplied methods.  */
class ClassTable {
  private int semantErrors;
  private PrintStream errorStream;
  
  private HashMap<AbstractSymbol, class_c> classes;
  
  /** Creates data structures representing basic Cool classes (Object,
     * IO, Int, Bool, String).  Please note: as is this method does not
     * do anything useful; you will need to edit it to make if do what
     * you want.
     * */
      private void installBasicClasses() {
       AbstractSymbol filename 
       = AbstractTable.stringtable.addString("<basic class>");
       
	// The following demonstrates how to create dummy parse trees to
	// refer to basic Cool classes.  There's no need for method
	// bodies -- these are already built into the runtime system.

	// IMPORTANT: The results of the following expressions are
	// stored in local variables.  You will want to do something
	// with those variables at the end of this method to make this
	// code meaningful.

	// The Object class has no parent class. Its methods are
	//        cool_abort() : Object    aborts the program
	//        type_name() : Str        returns a string representation 
	//                                 of class name
	//        copy() : SELF_TYPE       returns a copy of the object

       class_c Object_class = 
       new class_c(0, 
         TreeConstants.Object_, 
         TreeConstants.No_class,
         new Features(0)
         .appendElement(new method(0, 
           TreeConstants.cool_abort, 
           new Formals(0), 
           TreeConstants.Object_, 
           new no_expr(0)))
         .appendElement(new method(0,
           TreeConstants.type_name,
           new Formals(0),
           TreeConstants.Str,
           new no_expr(0)))
         .appendElement(new method(0,
           TreeConstants.copy,
           new Formals(0),
           TreeConstants.SELF_TYPE,
           new no_expr(0))),
         filename);
       
	// The IO class inherits from Object. Its methods are
	//        out_string(Str) : SELF_TYPE  writes a string to the output
	//        out_int(Int) : SELF_TYPE      "    an int    "  "     "
	//        in_string() : Str            reads a string from the input
	//        in_int() : Int                "   an int     "  "     "

       class_c IO_class = 
       new class_c(0,
         TreeConstants.IO,
         TreeConstants.Object_,
         new Features(0)
         .appendElement(new method(0,
           TreeConstants.out_string,
           new Formals(0)
           .appendElement(new formalc(0,
             TreeConstants.arg,
             TreeConstants.Str)),
           TreeConstants.SELF_TYPE,
           new no_expr(0)))
         .appendElement(new method(0,
           TreeConstants.out_int,
           new Formals(0)
           .appendElement(new formalc(0,
             TreeConstants.arg,
             TreeConstants.Int)),
           TreeConstants.SELF_TYPE,
           new no_expr(0)))
         .appendElement(new method(0,
           TreeConstants.in_string,
           new Formals(0),
           TreeConstants.Str,
           new no_expr(0)))
         .appendElement(new method(0,
           TreeConstants.in_int,
           new Formals(0),
           TreeConstants.Int,
           new no_expr(0))),
         filename);

	// The Int class has no methods and only a single attribute, the
	// "val" for the integer.

       class_c Int_class = 
       new class_c(0,
         TreeConstants.Int,
         TreeConstants.Object_,
         new Features(0)
         .appendElement(new attr(0,
           TreeConstants.val,
           TreeConstants.prim_slot,
           new no_expr(0))),
         filename);

	// Bool also has only the "val" slot.
       class_c Bool_class = 
       new class_c(0,
         TreeConstants.Bool,
         TreeConstants.Object_,
         new Features(0)
         .appendElement(new attr(0,
           TreeConstants.val,
           TreeConstants.prim_slot,
           new no_expr(0))),
         filename);

	// The class Str has a number of slots and operations:
	//       val                              the length of the string
	//       str_field                        the string itself
	//       length() : Int                   returns length of the string
	//       concat(arg: Str) : Str           performs string concatenation
	//       substr(arg: Int, arg2: Int): Str substring selection

       class_c Str_class =
       new class_c(0,
         TreeConstants.Str,
         TreeConstants.Object_,
         new Features(0)
         .appendElement(new attr(0,
           TreeConstants.val,
           TreeConstants.Int,
           new no_expr(0)))
         .appendElement(new attr(0,
           TreeConstants.str_field,
           TreeConstants.prim_slot,
           new no_expr(0)))
         .appendElement(new method(0,
           TreeConstants.length,
           new Formals(0),
           TreeConstants.Int,
           new no_expr(0)))
         .appendElement(new method(0,
           TreeConstants.concat,
           new Formals(0)
           .appendElement(new formalc(0,
             TreeConstants.arg, 
             TreeConstants.Str)),
           TreeConstants.Str,
           new no_expr(0)))
         .appendElement(new method(0,
           TreeConstants.substr,
           new Formals(0)
           .appendElement(new formalc(0,
             TreeConstants.arg,
             TreeConstants.Int))
           .appendElement(new formalc(0,
             TreeConstants.arg2,
             TreeConstants.Int)),
           TreeConstants.Str,
           new no_expr(0))),
         filename);

	/* Do somethind with Object_class, IO_class, Int_class,
 Bool_class, and Str_class here */
	   
	   classes.put(Object_class.getName(), Object_class);
	   classes.put(IO_class.getName(), IO_class);
	   classes.put(Int_class.getName(), Int_class);
	   classes.put(Bool_class.getName(), Bool_class);
	   classes.put(Str_class.getName(), Str_class);
}



	public ClassTable(Classes cls) {
		semantErrors = 0;
		errorStream = System.err;
		
		classes = new HashMap<AbstractSymbol, class_c>();
		installBasicClasses();
		for (int i = 0; i < cls.getLength(); i++) {
			class_c currClass = (class_c)cls.getNth(i);
			if (classes.containsKey(currClass.getName())) {
				semantError(currClass).println(
				  "Class " + currClass.getName() + " is double defined.");
			}
			classes.put(currClass.getName(), currClass);
		}
		
		for (Map.Entry<AbstractSymbol, class_c> kvPair :
				 classes.entrySet()) {
			verifyInheritanceGraph(kvPair); 
			/*if class heirarchy is malformed, that error is found here
				 and we exit in semant(program)*/ 
		}
	}

	public class_c getClass(AbstractSymbol sym) {
		class_c rVal = classes.get(sym);
		return rVal;
	}
	
	public boolean isSubClassOf(AbstractSymbol ch, AbstractSymbol par) {
		class_c parentClass = classes.get(par);
		
		if (parentClass == null) {
			semantError().println("class " + par + " does not exist.");
		}
		while (ch != par) {
			if (ch == TreeConstants.Object_) {
				return false;
			} else {
				class_c currClass = classes.get(ch);
				if (currClass == null) {
					semantError().println("class " + ch + " does not exist.");
				}
				ch = currClass.getParent();
			}
		}
		return true;
	}

	public class_c leastUpperBound(AbstractSymbol one, AbstractSymbol two)
	{
		Set<AbstractSymbol> found = new TreeSet<AbstractSymbol>();
		while (one != TreeConstants.Object_) {
			found.add(one);
			class_c currClass = classes.get(one);
			if (currClass == null) {
				semantError().println("class " + one + " does not exist.");
			}
			one = currClass.getParent();
		}
		found.add(TreeConstants.Object_);
		
		while (!found.contains(two)) {
			class_c currClass = classes.get(two);
			if (currClass == null) {
				semantError().println("class " + two + " does not exist.");
			}
			two = currClass.getParent();
		}
		
		return classes.get(two);
		
	}

	public void verifyClass(class_c curr) {
		getAllFeatures(curr, true);
	}
		
  //gets the features of the current class and all its parents
	private Features getAllFeatures(class_c cur, boolean printUnique){
    Features curFeats = cur.getFeatures();
    Features allFeats = new Features(curFeats.getLineNumber());
	TreeSet<AbstractSymbol> attrs = new TreeSet<AbstractSymbol>();
	TreeMap<AbstractSymbol, method> methods = new TreeMap<AbstractSymbol, method>();
	//use to verify attrs are not redefined, and inherited methods have
	//the same signatures


	
	/* This has to add ALL features from the immediate level, but then
	   prune out duplicates at inherited levels, so this happens in 2
	   stages. The checkUnique flag is used to print error messages during
	   the one pass intended to ensure well formed classes (get, but keeps it
	   from re-printing error in other calls.*/
	addImmediateFeatures(cur, curFeats, allFeats, attrs, methods, printUnique);
	if (cur.getName() != TreeConstants.Object_) {
		addInheritedFeatures(getClass(cur.getParent()), 
							 getClass(cur.getParent()).getFeatures(),
							allFeats, attrs, methods, printUnique);
	}
	return allFeats;
  }
	private void addImmediateFeatures(class_c cur, Features curFeats,
									 Features allFeats, Set<AbstractSymbol> attrs,
	   Map<AbstractSymbol, method> methods, boolean printErr){
		for (int i = 0; i < curFeats.getLength(); i++) {
			if (curFeats.getNth(i) instanceof attr) {
				if (attrs.contains(((attr)curFeats.getNth(i)).name)) {
					if (printErr) {
						semantError(cur).println(
						  	 "Attribute " + ((attr)curFeats.getNth(i)).name
							 + " in class " + cur + "illegally double defined.");
					}
				} else { //correctly defined new attribute
					allFeats.appendElement(curFeats.getNth(i));
				}
			} else { // is method, check if overloaded signature matches
				method curMethod = (method)curFeats.getNth(i);
				if (methods.containsKey(curMethod.name)) {
					if (printErr) {
						semantError(cur).println("Method " + curMethod + 
						 " double defined in class " + cur);
					}
				} else { // new method name
					methods.put(curMethod.name, curMethod);
					allFeats.appendElement(curFeats.getNth(i));
					//don't include duplicates of method names
				}
			} //end if attr/methos
		} // end for each formal
	} // end function
	
	private void addInheritedFeatures(class_c cur, Features curFeats, Features
				allFeats, Set<AbstractSymbol> attrs, Map<AbstractSymbol, method> methods,
				boolean printErr){
		while(true){
			for (int i = 0; i < curFeats.getLength(); i++) {
				if (curFeats.getNth(i) instanceof attr) {
					if (attrs.contains(((attr)curFeats.getNth(i)).name)) {
						if (printErr) {
							semantError(cur).println(
						    "Attribute " + ((attr)curFeats.getNth(i)).name
						  + " in class " + cur + "illegally redefined in subclass.");
						}
					} else { //correctly defined new attribute
						allFeats.appendElement(curFeats.getNth(i));
					}
				} else { // is method, check if overloaded signature matches
					method curMethod = (method)curFeats.getNth(i);
					if (methods.containsKey(curMethod.name)) {
						if (!hasIdenticalSignature(curMethod,
												   methods.get(curMethod.name)))
							{
								if (printErr) {
									semantError(cur).println("Non-identical	methodsignature in overloaded method call " + curMethod + " in class " + cur);
								}
							} //end if !hasIdenticalSignature
					} else { // new method name
						methods.put(curMethod.name, curMethod);
						allFeats.appendElement(curFeats.getNth(i));
						//don't include duplicates of method names
					}
				}
			}
			if(cur.name == TreeConstants.Object_) return;
			cur = getClass(cur.getParent());
			curFeats = cur.getFeatures();
		}
	}
	private boolean hasIdenticalSignature(method one, method two) {
		Formals oneF = one.formals;
		Formals twoF = two.formals;
		if (one.return_type != two.return_type ||
			oneF.getLength() != twoF.getLength()) return false;

		for (int i = 0; i < oneF.getLength(); i++) {
			if (  ((formalc)oneF.getNth(i)).type_decl != 
				  ((formalc)twoF.getNth(i)).type_decl )
				return false;
			
		}
		return true;
	}

  //TODO: make this gather elements of all parent classes as well.
  //This should return all attrs/methods, including inherited ones.
  private Features getElements(AbstractSymbol className, Class c){
    class_c currClass = classes.get(className);
    if (currClass == null) {
      semantError().println("class " + className + " does not exist.");
    }

    Features allFeats = getAllFeatures(currClass, false);
	//false to avoid double printing error messages

	/* We don't need this right? Am I missing something? -T
	  Features elems = new Features(allFeats.getLineNumber());
    for (int i = 0; i < allFeats.getLength(); i++) {
      Feature feat = (Feature) allFeats.getNth(i);
      if (feat instanceof attr) {
        elems.appendElement(feat);
      }
	  } */
    return allFeats;
  }
	
	public Features getAttrs(AbstractSymbol className){
		return getElements(className, attr.class);
	}
	
	public Features getMethods(AbstractSymbol className) {
		return getElements(className, method.class);
	}
  
  //Do classes with no parent have No_class as their parent field or Object?	
	private void verifyInheritanceGraph(Map.Entry<AbstractSymbol,
										class_c> kvPair) {
		AbstractSymbol currClassSym = kvPair.getKey();
		class_c currClass = kvPair.getValue();
		Set<AbstractSymbol> found = new TreeSet<AbstractSymbol>();
		while (currClassSym != TreeConstants.Object_) {
			if (found.contains(currClassSym)) {
				semantError(currClass).println(
					"Inheritance cycle detected at class " + currClassSym);
			}
			found.add(currClassSym);
			
			AbstractSymbol parentClassSym  = currClass.getParent();
			class_c parentClass = classes.get(parentClassSym);
			if (parentClass == null) {
				semantError(currClass).println("subclass " + currClass
					 + "attempting to inherit from undefined super-class " + parentClass);
			}
			currClassSym = parentClassSym;
			currClass = parentClass;
		} //while
	} // verifyInheritanceGraph
	
    /** Prints line number and file name of the given class.
     *
     * Also increments semantic error count.
     *
     * @param c the class
     * @return a print stream to which the rest of the error message is
     * to be printed.
     *
     * */
    public PrintStream semantError(class_c c) {
     return semantError(c.getFilename(), c);
   }

    /** Prints the file name and the line number of the given tree node.
     *
     * Also increments semantic error count.
     *
     * @param filename the file name
     * @param t the tree node
     * @return a print stream to which the rest of the error message is
     * to be printed.
     *
     * */
    public PrintStream semantError(AbstractSymbol filename, TreeNode t) {
     errorStream.print(filename + ":" + t.getLineNumber() + ": ");
     return semantError();
   }

    /** Increments semantic error count and returns the print stream for
     * error messages.
     *
     * @return a print stream to which the error message is
     * to be printed.
     *
     * */
    public PrintStream semantError() {
     semantErrors++;
     return errorStream;
   }

   /** Returns true if there are any static semantic errors. */
   public boolean errors() {
     return semantErrors != 0;
   }
 }
 
 
