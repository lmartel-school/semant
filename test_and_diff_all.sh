make semant
rm ./test/*.out -f
rm ./test/*.errors -f
#FILES="./test/good.cl"
FILES="./test/*.cl"
for f in $FILES
do
    echo "reference semant: '$f'"
    /usr/class/cs143/bin/coolc -k "$f" > "$f.ref.errors" 2>&1
    rm "lexer.out" -f
    rm "parser.out" -f
    mv "semant.out" "$f.ref.out"
    echo "my-semant: '$f'"
    ./mycoolc -k "$f" > "$f.my.errors" 2>&1
    rm "lexer.out" -f
    rm "parser.out" -f
    mv "semant.out" "$f.my.out"
done
for f in $FILES
do
    out=`diff "$f.ref.out" "$f.my.out"`
    errors=`diff "$f.ref.errors" "$f.my.errors"`
    # out = errors only when both equal
    if [ "$out" == "$errors" ]
    then
	echo "'$f' passes!"
    else
	echo "begin AST diff: '$f' (ours on right)"
	diff "$f.ref.out" "$f.my.out"
	echo "end AST diff"
	echo "begin errors diff: '$f' (ours on right)"
	diff "$f.ref.errors" "$f.my.errors"
	echo "end errors diff"
    fi
done
rm *.s -f
rm ./test/*.out -f
rm ./test/*.errors -f